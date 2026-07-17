package view.screens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import controller.CombatEngine;
import controller.WorldManager;
import model.Player;
import model.world.Area;
import model.world.AreaEvent;
import model.world.WorldState;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.effects.Particle;
import view.rendering.PixelRenderer;
import view.sprites.ColorPalette;

/**
 * Handles area-specific narrative events with choices.
 * Shows typewriter text and choice options.
 * After choice, advances the encounter and returns to AreaScreen.
 */
public class AreaEventScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;
    private final WorldManager worldManager;
    private final AreaEvent event;

    private final String eventDescription;
    private final List<String> choices;
    private int selectedIndex;

    // Typewriter effect
    private float typewriterTimer;
    private int typedCharCount;
    private boolean typewriterDone;
    private static final float TYPEWRITER_SPEED = 25f;

    // Result display
    private boolean showingResult;
    private String resultText;

    // Ambient particles
    private final List<Particle> ambientParticles;
    private float particleSpawnTimer;
    private float blinkTimer;
    private boolean cursorVisible;

    public AreaEventScreen(PlagueOfDanjinGame game, CombatEngine engine, AreaEvent event) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;
        this.worldManager = engine.getWorldManager();
        this.event = event;
        this.eventDescription = event.getDescription();
        this.choices = event.getChoices();
        this.selectedIndex = 0;
        this.typewriterTimer = 0f;
        this.typedCharCount = 0;
        this.typewriterDone = false;
        this.showingResult = false;
        this.resultText = "";
        this.ambientParticles = new ArrayList<>();
        this.particleSpawnTimer = 0f;
        this.blinkTimer = 0f;
        this.cursorVisible = true;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        game.getMusicManager().play("event_room_theme");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(ColorPalette.BACKGROUND.r, ColorPalette.BACKGROUND.g,
                ColorPalette.BACKGROUND.b, 1f);

        // Update typewriter
        if (!typewriterDone) {
            typewriterTimer += delta * TYPEWRITER_SPEED;
            typedCharCount = Math.min((int) typewriterTimer, eventDescription.length());
            if (typedCharCount >= eventDescription.length()) {
                typewriterDone = true;
            }
        }

        blinkTimer += delta;
        if (blinkTimer >= 0.4f) {
            cursorVisible = !cursorVisible;
            blinkTimer = 0f;
        }

        updateAmbientParticles(delta);
        game.getMusicManager().update(delta);

        renderer.begin();
        SpriteBatch batch = renderer.getBatch();
        BitmapFont font = assets.getFont();

        drawBackground(batch);
        drawAmbientParticles(batch);

        // Event title
        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, event.getName(), 10f, 232f);

        // Separator
        TextureRegion particleTex = assets.getParticleTexture("physical");
        if (particleTex != null) {
            batch.setColor(ColorPalette.UI_BORDER);
            batch.draw(particleTex, 10f, 222f, 300f, 1f);
            batch.setColor(Color.WHITE);
        }

        if (showingResult) {
            // Show result
            font.setColor(ColorPalette.TEXT_WHITE);
            font.draw(batch, resultText, 15f, 160f, 290f, -1, true);
            if (cursorVisible) {
                font.setColor(ColorPalette.HEAL_GREEN);
                font.draw(batch, "Press Enter to continue", 80f, 40f);
            }
        } else {
            // Show description with typewriter
            font.setColor(ColorPalette.TEXT_WHITE);
            String displayText = eventDescription.substring(0, typedCharCount);
            font.draw(batch, displayText, 15f, 210f, 290f, -1, true);

            // Show choices after typewriter is done
            if (typewriterDone) {
                float choiceY = 120f;
                for (int i = 0; i < choices.size(); i++) {
                    String prefix = (i == selectedIndex) ? "> " : "  ";
                    Color choiceColor = (i == selectedIndex) ? ColorPalette.CRIT_YELLOW : ColorPalette.TEXT_WHITE;
                    font.setColor(choiceColor);
                    font.draw(batch, prefix + (i + 1) + ". " + choices.get(i), 20f, choiceY, 280f, -1, true);
                    choiceY -= 30f;
                }

                if (cursorVisible) {
                    font.setColor(ColorPalette.HEAL_GREEN);
                    font.draw(batch, "1-" + choices.size() + " or Enter to choose", 80f, 30f);
                }
            }
        }

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
        renderer.end();
    }

    private void drawBackground(SpriteBatch batch) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        batch.setColor(0.2f, 0.1f, 0.2f, 0.5f);
        for (int x = 0; x < PixelRenderer.VIRTUAL_WIDTH; x += 16) {
            for (int y = 0; y < PixelRenderer.VIRTUAL_HEIGHT; y += 16) {
                batch.draw(bgTile, x, y);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void updateAmbientParticles(float delta) {
        particleSpawnTimer += delta;
        if (particleSpawnTimer >= 0.15f) {
            particleSpawnTimer = 0f;
            float px = MathUtils.random(0f, PixelRenderer.VIRTUAL_WIDTH);
            float py = MathUtils.random(0f, PixelRenderer.VIRTUAL_HEIGHT);
            float vx = MathUtils.random(-5f, 5f);
            float vy = MathUtils.random(3f, 10f);
            float life = MathUtils.random(1f, 2.5f);
            Color color = MathUtils.randomBoolean() ? ColorPalette.DARK_PURPLE : ColorPalette.MAGE_PURPLE;
            Particle p = new Particle(px, py, vx, vy, life, 1, color);
            ambientParticles.add(p);
        }

        Iterator<Particle> it = ambientParticles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update(delta);
            if (!p.isAlive()) {
                it.remove();
            }
        }
        while (ambientParticles.size() > 40) {
            ambientParticles.remove(0);
        }
    }

    private void drawAmbientParticles(SpriteBatch batch) {
        TextureRegion particleTex = assets.getParticleTexture("physical");
        if (particleTex == null) return;
        for (Particle p : ambientParticles) {
            p.render(batch, particleTex);
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (showingResult) {
            if (keycode == Input.Keys.ENTER) {
                // Advance encounter and return to AreaScreen
                WorldState state = worldManager.getWorldState();
                Area current = state.getCurrentArea();
                state.advanceEncounter(current);
                game.setScreen(new AreaScreen(game, engine));
            }
            return true;
        }

        if (!typewriterDone) {
            if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                typedCharCount = eventDescription.length();
                typewriterDone = true;
            }
            return true;
        }

        // Choose option
        switch (keycode) {
            case Input.Keys.NUM_1:
                if (choices.size() >= 1) makeChoice(0);
                return true;
            case Input.Keys.NUM_2:
                if (choices.size() >= 2) makeChoice(1);
                return true;
            case Input.Keys.UP:
                selectedIndex = Math.max(0, selectedIndex - 1);
                return true;
            case Input.Keys.DOWN:
                selectedIndex = Math.min(choices.size() - 1, selectedIndex + 1);
                return true;
            case Input.Keys.ENTER:
                makeChoice(selectedIndex);
                return true;
        }
        return false;
    }

    private void makeChoice(int index) {
        // Record choice for ending determination
        worldManager.recordEventChoice(event.getName(), index);

        // Apply choice effects
        Player player = engine.getPlayer();
        Area currentArea = worldManager.getWorldState().getCurrentArea();

        switch (currentArea) {
            case GOBLIN_WARRENS:
                if (index == 0) {
                    resultText = "The prisoner reveals the King's weakness to fire. You feel prepared for the battle ahead.";
                } else {
                    resultText = "You leave the prisoner to his fate. The dungeon offers no mercy to the weak.";
                }
                break;
            case BONE_CATHEDRAL:
                if (index == 0) {
                    resultText = "The paladin's blessing washes over you. Holy energy courses through your weapons.";
                    engine.setConsecratedGroundBlessing(true);
                } else {
                    resultText = "You refuse the spirit's offer. A rare amulet materializes before you as a token of respect.";
                    if (player != null) {
                        player.upgradePower(5);
                        player.upgradeDefense(5);
                    }
                }
                break;
            case PLAGUE_GARDENS:
                if (index == 0) {
                    resultText = "You channel purifying energy into the tree. The toxic atmosphere begins to clear.";
                    engine.setToxicAtmospherePurified(true);
                } else {
                    resultText = "You absorb the tree's power. Dark energy floods your veins. You feel stronger, but tainted.";
                    if (player != null) {
                        player.upgradePower(20);
                    }
                }
                break;
            case LICHS_THRONE:
                if (index == 0) {
                    resultText = "You accept the Lich's offer. Immense power surges through you, but at what cost?";
                    if (player != null) {
                        player.upgradePower(50);
                        player.upgradeDefense(50);
                    }
                    engine.getRunModifiers().setDanjinHeartAbsorbed(true);
                } else {
                    resultText = "You refuse the Lich's temptation. Your resolve strengthens.";
                }
                break;
            default:
                resultText = "You make your choice and press onward.";
                break;
        }

        showingResult = true;
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
    public void hide() {}

    @Override
    public void dispose() {}
}
