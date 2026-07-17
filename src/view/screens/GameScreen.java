package view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import controller.CombatEngine;
import controller.GameState;
import controller.MetaProgression;
import controller.SaveManager;
import model.PlayerAction;
import model.events.GameEvent;
import model.events.GameEventListener;
import model.events.GameEventType;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.audio.MusicManager;
import view.audio.SFXManager;
import view.effects.VisualEffectsManager;
import view.rendering.AnimationManager;
import view.rendering.PixelRenderer;
import view.rendering.ScreenShake;
import view.sprites.ColorPalette;
import view.ui.CombatMenu;
import view.ui.EnemyDisplay;
import view.ui.HUD;
import view.ui.MessageLog;

/**
 * Main battle screen containing HUD, CombatMenu, EnemyDisplay, MessageLog.
 * Integrates with CombatEngine state machine: renders based on currentState.
 * Implements InputProcessor for keyboard and mouse input.
 * Uses AnimationManager to prevent game from racing ahead of visual display.
 * Integrates VisualEffectsManager for particles, damage numbers, and screen flash.
 * Renders tiled dungeon background with subtle torchlight flicker effect.
 */
public class GameScreen implements Screen, InputProcessor, GameEventListener {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;

    private final HUD hud;
    private final CombatMenu combatMenu;
    private final MessageLog messageLog;
    private final AnimationManager animationManager;
    private final ScreenShake screenShake;
    private final VisualEffectsManager visualEffects;
    private final SFXManager sfxManager;
    private final MusicManager musicManager;

    // Enemy display position constants (matching HUD layout)
    private static final float ENEMY_CENTER_X = 160f;
    private static final float ENEMY_CENTER_Y = 170f;

    // Player position for damage numbers
    private static final float PLAYER_X = 50f;
    private static final float PLAYER_Y = 45f;

    // State tracking for animation-gated transitions
    private boolean waitingForAnimation;
    private GameState pendingState;
    private boolean processingEnemyTurn;

    // Wave transition display
    private float waveTransitionTimer;
    private static final float WAVE_TRANSITION_DURATION = 1.5f;
    private boolean showingWaveTransition;

    // Game over / chest state
    private boolean showingGameOver;
    private boolean showingChestResult;
    private String chestResultText;

    // Torchlight flicker
    private float torchlightTimer;
    private static final float TORCHLIGHT_FLICKER_SPEED = 2.5f;
    private static final float TORCHLIGHT_MIN_BRIGHTNESS = 0.85f;

    public GameScreen(PlagueOfDanjinGame game, CombatEngine engine) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;

        this.combatMenu = new CombatMenu();
        this.messageLog = new MessageLog();
        this.hud = new HUD(combatMenu, messageLog);
        this.animationManager = new AnimationManager();
        this.screenShake = new ScreenShake();
        this.visualEffects = new VisualEffectsManager();
        this.sfxManager = game.getSfxManager();
        this.musicManager = game.getMusicManager();

        this.waitingForAnimation = false;
        this.pendingState = null;
        this.processingEnemyTurn = false;
        this.showingWaveTransition = false;
        this.waveTransitionTimer = 0f;
        this.showingGameOver = false;
        this.showingChestResult = false;
        this.chestResultText = "";
        this.torchlightTimer = 0f;

        // Set up UI
        combatMenu.setPlayer(engine.getPlayer());

        // Register message log as event listener on the engine
        engine.addListener(messageLog);

        // Register this screen as a listener for animation triggers
        engine.addListener(this);

        // Register SFX manager for audio feedback on game events
        engine.addListener(sfxManager);

        // Ensure SaveManager is set on engine for auto-save at wave transitions
        engine.setSaveManager(game.getSaveManager());

        // Register MetaProgression as listener if not already registered
        MetaProgression meta = game.getMetaProgression();
        if (meta != null) {
            engine.addListener(meta);
        }

        // Start combat music
        musicManager.play("combat_theme");
    }

    @Override
    public void onEvent(GameEvent event) {
        // Queue animations for visual events
        GameEventType type = event.getType();
        switch (type) {
            case DAMAGE_DEALT:
                animationManager.queueAnimation(event);
                screenShake.trigger(2f);
                hud.triggerHpFlash();
                // Trigger visual effects: slash + damage number at enemy position
                visualEffects.onDamageDealt(event, ENEMY_CENTER_X, ENEMY_CENTER_Y);
                break;

            case ENEMY_ATTACK:
            case ENEMY_ABILITY_FIRED:
                animationManager.queueAnimation(event);
                screenShake.trigger(2f);
                hud.triggerHpFlash();
                // Enemy attacks player: red flash + damage number at player position
                visualEffects.onPlayerDamaged(event, PLAYER_X, PLAYER_Y);
                // Trigger enemy attack animation
                hud.getEnemyDisplay().triggerAttackAnimation();
                break;

            case CRITICAL_HIT:
                animationManager.queueAnimation(event);
                screenShake.trigger(4f);
                // Larger slash + screen shake + CRITICAL! popup
                visualEffects.onCriticalHit(event, ENEMY_CENTER_X, ENEMY_CENTER_Y);
                hud.triggerEnemyDamageFlash();
                break;

            case PLAYER_BASIC_ATTACK:
                animationManager.queueAnimation(event);
                hud.triggerEnemyDamageFlash();
                // Slash effect at enemy
                visualEffects.spawnSlashEffect(ENEMY_CENTER_X, ENEMY_CENTER_Y);
                break;

            case SPELL_CAST:
                animationManager.queueAnimation(event);
                hud.triggerEnemyDamageFlash();
                // Element-appropriate particles at enemy
                visualEffects.onSpellCast(event, ENEMY_CENTER_X, ENEMY_CENTER_Y);
                break;

            case HEAL:
            case MANA_REGEN:
                animationManager.queueAnimation(event);
                // Green number + sparkles at player
                if (type == GameEventType.HEAL) {
                    visualEffects.onHeal(event, PLAYER_X, PLAYER_Y);
                }
                break;

            case ENEMY_DEFEATED:
                // Enemy death animation + dissolve particles
                hud.getEnemyDisplay().triggerDeathAnimation();
                visualEffects.onEnemyDeath(ENEMY_CENTER_X, ENEMY_CENTER_Y);
                break;

            case WAVE_COMPLETE:
                // Golden screen flash
                visualEffects.onLevelUp();
                break;

            case CHEST_FOUND:
            case CHEST_LEGENDARY:
            case CHEST_EPIC:
            case CHEST_RARE:
            case CHEST_COMMON:
                showingChestResult = true;
                chestResultText = getChestText(event);
                break;

            case CHEST_MIMIC:
                showingChestResult = true;
                chestResultText = "A Mimic! It attacks!";
                screenShake.trigger(3f);
                break;

            default:
                break;
        }
    }

    private String getChestText(GameEvent event) {
        switch (event.getType()) {
            case CHEST_LEGENDARY: return "LEGENDARY chest! Amazing loot!";
            case CHEST_EPIC: return "EPIC chest! Great find!";
            case CHEST_RARE: return "RARE chest! Good stuff.";
            case CHEST_COMMON: return "Common chest. A small reward.";
            case CHEST_FOUND: return "You found a chest!";
            default: return "Chest opened!";
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);

        // Update systems
        screenShake.update(delta);
        animationManager.update(delta);
        hud.update(delta);
        visualEffects.update(delta);
        musicManager.update(delta);
        torchlightTimer += delta;

        // Handle state machine transitions gated by animations
        handleStateTransitions();

        // Update music based on current wave (boss theme for waves 5/10/15/20)
        updateMusicTrack();

        // Apply screen shake
        renderer.applyCameraOffset(screenShake.getOffsetX(), screenShake.getOffsetY());

        // Render shapes first (HP bars, etc.)
        ShapeRenderer shapeRenderer = assets.getShapeRenderer();
        shapeRenderer.setProjectionMatrix(renderer.getCamera().combined);
        hud.renderShapes(shapeRenderer, engine);

        // Render sprites and text
        renderer.begin();
        BitmapFont font = assets.getFont();
        SpriteBatch batch = renderer.getBatch();

        // Apply torchlight flicker to background
        applyTorchlightFlicker(batch);

        GameState currentState = engine.getCurrentState();
        boolean showMenu = (currentState == GameState.AWAITING_PLAYER_ACTION)
                           && !animationManager.isPlaying();

        hud.render(batch, font, assets, engine, showMenu);

        // Reset batch color after HUD (torchlight may have tinted it)
        batch.setColor(Color.WHITE);

        // Render visual effects (particles, damage numbers) inside SpriteBatch
        visualEffects.render(batch, font, assets);

        // Render screen flash overlay
        renderScreenFlash(batch);

        // Render state-specific overlays
        renderStateOverlay(batch, font, currentState);

        renderer.end();

        // Reset camera for next frame
        renderer.resetCamera();
    }

    /**
     * Applies subtle torchlight flicker effect (periodic brightness variation).
     */
    private void applyTorchlightFlicker(SpriteBatch batch) {
        float flicker = TORCHLIGHT_MIN_BRIGHTNESS +
                (1f - TORCHLIGHT_MIN_BRIGHTNESS) *
                (0.5f + 0.5f * MathUtils.sin(torchlightTimer * TORCHLIGHT_FLICKER_SPEED));
        // Add secondary frequency for organic feel
        flicker += 0.03f * MathUtils.sin(torchlightTimer * 7.3f);
        flicker = MathUtils.clamp(flicker, TORCHLIGHT_MIN_BRIGHTNESS, 1f);
        batch.setColor(flicker, flicker * 0.95f, flicker * 0.85f, 1f);
    }

    /**
     * Renders screen flash overlay when active.
     */
    private void renderScreenFlash(SpriteBatch batch) {
        if (!visualEffects.isScreenFlashing()) return;

        Color flashColor = visualEffects.getScreenFlashColor();
        if (flashColor.a <= 0.01f) return;

        // Use a particle texture stretched over the screen as the flash overlay
        TextureRegion flashTex = assets.getParticleTexture("physical");
        if (flashTex != null) {
            batch.setColor(flashColor);
            batch.draw(flashTex, 0, 0, 320, 240);
            batch.setColor(Color.WHITE);
        }
    }

    /**
     * Switches music track based on current game state and wave number.
     * Boss theme plays on waves 5, 10, 15, 20; combat theme otherwise.
     */
    private void updateMusicTrack() {
        GameState state = engine.getCurrentState();
        switch (state) {
            case AWAITING_PLAYER_ACTION:
            case PROCESSING_ACTION:
            case ENEMY_TURN:
                int wave = engine.getCurrentWave();
                if (wave % 5 == 0) {
                    musicManager.play("boss_theme");
                } else {
                    musicManager.play("combat_theme");
                }
                break;
            case WAVE_TRANSITION:
                musicManager.play("dungeon_theme");
                break;
            case GAME_OVER:
                musicManager.stop();
                break;
            default:
                break;
        }
    }

    private void handleStateTransitions() {
        GameState currentState = engine.getCurrentState();

        // Do not advance state while animations are playing
        if (animationManager.isPlaying()) {
            return;
        }

        // Reset enemy turn guard when we leave ENEMY_TURN state
        if (currentState != GameState.ENEMY_TURN) {
            processingEnemyTurn = false;
        }

        // Handle wave transition animation
        if (showingWaveTransition) {
            return; // Wait for player input or timer
        }

        switch (currentState) {
            case PROCESSING_ACTION:
                // After player action animation completes, enemy turn happens
                // The engine has already transitioned, so just wait for animation
                break;

            case ENEMY_TURN:
                // Guard against re-entry on consecutive frames
                if (!processingEnemyTurn) {
                    processingEnemyTurn = true;
                    engine.processEnemyTurn();
                }
                break;

            case WAVE_TRANSITION:
                if (!showingWaveTransition) {
                    showingWaveTransition = true;
                    waveTransitionTimer = 0f;
                }
                break;

            case SKILL_CHOICE:
                game.setScreen(new SkillChoiceScreen(game, engine));
                break;

            case EVENT_ROOM:
                game.setScreen(new EventRoomScreen(game, engine));
                break;

            case QTE_EVENT:
                game.setScreen(new QTEScreen(game, engine));
                break;

            case VICTORY:
                handleRunEnd(true);
                game.setScreen(new VictoryScreen(game, engine));
                break;

            case GAME_OVER:
                handleRunEnd(false);
                showingGameOver = true;
                break;

            case CHEST_RESULT:
                // Handled via the event listener flag (showingChestResult)
                break;

            default:
                break;
        }
    }

    /**
     * Handles end-of-run operations: updates MetaProgression, saves it, and deletes run save.
     */
    private boolean runEndHandled = false;

    private void handleRunEnd(boolean victory) {
        if (runEndHandled) return;
        runEndHandled = true;

        MetaProgression meta = game.getMetaProgression();
        SaveManager saveManager = game.getSaveManager();

        if (meta != null) {
            meta.recordRunEnd(victory, engine.getCurrentWave(), engine.getTurnCounter());

            // Check unlock conditions
            boolean danjinAbsorbed = engine.getRunModifiers().isDanjinHeartAbsorbed();
            boolean danjinShattered = engine.getRunModifiers().isDanjinHeartShattered();
            // Victory implies Lich was defeated (wave 20 boss)
            boolean defeatedLich = victory;
            meta.checkAndAwardUnlocks(danjinAbsorbed, danjinShattered, defeatedLich);
        }

        if (saveManager != null) {
            if (meta != null) {
                saveManager.saveMetaProgression(meta);
            }
            saveManager.deleteSave();
        }
    }

    private void renderStateOverlay(SpriteBatch batch, BitmapFont font, GameState state) {
        if (showingWaveTransition) {
            waveTransitionTimer += Gdx.graphics.getDeltaTime();
            font.setColor(Color.GOLD);
            font.draw(batch, "Wave " + engine.getCurrentWave() + " Complete!", 90f, 130f);
            font.setColor(Color.WHITE);
            font.draw(batch, "Press Enter to continue", 80f, 100f);
        }

        if (showingChestResult) {
            font.setColor(Color.YELLOW);
            font.draw(batch, chestResultText, 30f, 130f, 260f, -1, true);
            font.setColor(Color.GRAY);
            font.draw(batch, "Click to continue", 100f, 80f);
        }

        if (showingGameOver) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER", 120f, 150f);
            font.setColor(Color.WHITE);
            String playerName = engine.getPlayer() != null ? engine.getPlayer().getName() : "Hero";
            font.draw(batch, playerName + " has fallen at wave " + engine.getCurrentWave(), 50f, 120f);
            font.setColor(Color.GREEN);
            font.draw(batch, "Press Enter to restart", 90f, 80f);
        }

        // Show animation indicator
        if (animationManager.isPlaying()) {
            AnimationManager.AnimationEntry anim = animationManager.getCurrentAnimation();
            if (anim != null && anim.getEvent() != null) {
                float progress = animationManager.getProgress();
                // Flash effect during damage animations
                GameEventType type = anim.getEvent().getType();
                if (type == GameEventType.DAMAGE_DEALT || type == GameEventType.ENEMY_ATTACK) {
                    if ((int)(progress * 10) % 2 == 0) {
                        font.setColor(Color.RED);
                        font.draw(batch, "!", 155f, 130f);
                    }
                }
            }
        }

        font.setColor(Color.WHITE);
    }

    // --- InputProcessor implementation ---

    @Override
    public boolean keyDown(int keycode) {
        GameState state = engine.getCurrentState();

        // Game over - restart
        if (showingGameOver && keycode == Input.Keys.ENTER) {
            game.setScreen(new MainMenuScreen(game));
            return true;
        }

        // Wave transition - proceed
        if (showingWaveTransition && keycode == Input.Keys.ENTER) {
            showingWaveTransition = false;
            engine.advanceWave();
            return true;
        }

        // Chest result - proceed
        if (showingChestResult && (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE)) {
            showingChestResult = false;
            engine.proceedAfterChest();
            return true;
        }

        // Only process combat input during AWAITING_PLAYER_ACTION and when not animating
        if (state != GameState.AWAITING_PLAYER_ACTION || animationManager.isPlaying()) {
            return false;
        }

        switch (keycode) {
            case Input.Keys.NUM_1:
            case Input.Keys.NUM_2:
            case Input.Keys.NUM_3:
            case Input.Keys.NUM_4:
            case Input.Keys.NUM_5:
            case Input.Keys.NUM_6:
            case Input.Keys.NUM_7:
            case Input.Keys.NUM_8:
            case Input.Keys.NUM_9:
                int number = keycode - Input.Keys.NUM_1 + 1;
                int result = combatMenu.selectByNumber(number);
                if (result >= 0) {
                    executePlayerAction(result);
                }
                return true;

            case Input.Keys.UP:
                combatMenu.moveUp();
                return true;

            case Input.Keys.DOWN:
                combatMenu.moveDown();
                return true;

            case Input.Keys.ENTER:
                int confirmResult = combatMenu.confirm();
                if (confirmResult >= 0) {
                    executePlayerAction(confirmResult);
                }
                return true;

            case Input.Keys.ESCAPE:
                combatMenu.back();
                return true;
        }

        return false;
    }

    private void executePlayerAction(int skillIndex) {
        PlayerAction action = getActionForSkillIndex(skillIndex);
        if (action != null) {
            engine.processPlayerAction(action);
            combatMenu.reset();
        }
    }

    private PlayerAction getActionForSkillIndex(int skillIndex) {
        // Map the skill list index to the corresponding PlayerAction.
        // PlayerAction enum values are ordered so that getSkillIndex() returns
        // the matching position in the unlocked skills list.
        for (PlayerAction action : PlayerAction.values()) {
            if (action.getSkillIndex() == skillIndex) {
                return action;
            }
        }
        return null;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Click to advance animations/text
        if (animationManager.isPlaying()) {
            return true;
        }

        // Chest result - proceed
        if (showingChestResult) {
            showingChestResult = false;
            engine.proceedAfterChest();
            return true;
        }

        // Wave transition - proceed
        if (showingWaveTransition) {
            showingWaveTransition = false;
            engine.advanceWave();
            return true;
        }

        // Game over - restart
        if (showingGameOver) {
            game.setScreen(new MainMenuScreen(game));
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // Unregister listeners when screen is hidden to avoid memory leaks.
        // libGDX's Game.setScreen() calls hide() on the old screen, not dispose().
        engine.removeListener(messageLog);
        engine.removeListener(this);
        engine.removeListener(sfxManager);
        MetaProgression meta = game.getMetaProgression();
        if (meta != null) {
            engine.removeListener(meta);
        }
    }

    @Override
    public void dispose() {
        // Also deregister here for safety if dispose() is called directly
        engine.removeListener(messageLog);
        engine.removeListener(this);
        engine.removeListener(sfxManager);
        MetaProgression meta = game.getMetaProgression();
        if (meta != null) {
            engine.removeListener(meta);
        }
    }
}
