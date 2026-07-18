package view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import controller.CombatEngine;
import controller.QTEManager;
import controller.QTEPattern;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.rendering.PixelRenderer;
import view.sprites.ColorPalette;

/**
 * QTE (Quick-Time Event) screen overlay. Renders key prompts, timer bar,
 * and visual feedback for correct/wrong inputs.
 * Implements Screen and InputProcessor (via InputAdapter).
 * On QTE completion, calls engine.resolveQTE(success) and transitions back to GameScreen.
 */
public class QTEScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;
    private final QTEManager qteManager;
    private final QTEPattern pattern;

    // Visual feedback state
    private float correctFlashTimer;
    private float wrongFlashTimer;
    private float resultDisplayTimer;
    private boolean showingResult;

    // Screen shake for failure
    private float shakeTimer;
    private float shakeOffsetX;
    private float shakeOffsetY;

    // Golden burst for success
    private float goldenBurstTimer;

    // Constants
    private static final float FLASH_DURATION = 0.2f;
    private static final float RESULT_DISPLAY_DURATION = 1.5f;
    private static final float SHAKE_DURATION = 0.4f;
    private static final float GOLDEN_BURST_DURATION = 0.6f;

    // Timer bar dimensions
    private static final float TIMER_BAR_X = 40f;
    private static final float TIMER_BAR_Y = 30f;
    private static final float TIMER_BAR_WIDTH = 240f;
    private static final float TIMER_BAR_HEIGHT = 10f;

    public QTEScreen(PlagueOfDanjinGame game, CombatEngine engine) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;
        this.qteManager = engine.getCurrentQTEManager();
        this.pattern = qteManager.getPattern();

        this.correctFlashTimer = 0f;
        this.wrongFlashTimer = 0f;
        this.resultDisplayTimer = 0f;
        this.showingResult = false;
        this.shakeTimer = 0f;
        this.shakeOffsetX = 0f;
        this.shakeOffsetY = 0f;
        this.goldenBurstTimer = 0f;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        qteManager.start();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.02f, 0.08f, 1f);

        // Update QTE logic
        if (!showingResult) {
            qteManager.update(delta);

            // Check for completion
            if (qteManager.isComplete()) {
                showingResult = true;
                resultDisplayTimer = 0f;
                if (qteManager.isSuccess()) {
                    goldenBurstTimer = GOLDEN_BURST_DURATION;
                } else {
                    shakeTimer = SHAKE_DURATION;
                }
            }
        } else {
            resultDisplayTimer += delta;
            if (resultDisplayTimer >= RESULT_DISPLAY_DURATION) {
                // QTE complete, resolve and return to game
                engine.resolveQTE(qteManager.isSuccess());
                game.setScreen(new GameScreen(game, engine));
                return;
            }
        }

        // Update visual timers
        if (correctFlashTimer > 0f) correctFlashTimer -= delta;
        if (wrongFlashTimer > 0f) wrongFlashTimer -= delta;
        if (goldenBurstTimer > 0f) goldenBurstTimer -= delta;

        // Update screen shake
        if (shakeTimer > 0f) {
            shakeTimer -= delta;
            shakeOffsetX = MathUtils.random(-3f, 3f);
            shakeOffsetY = MathUtils.random(-3f, 3f);
        } else {
            shakeOffsetX = 0f;
            shakeOffsetY = 0f;
        }

        // Apply screen shake
        renderer.applyCameraOffset(shakeOffsetX, shakeOffsetY);

        renderer.begin();
        BitmapFont font = assets.getFont();
        SpriteBatch batch = renderer.getBatch();

        // Draw background overlay
        drawBackground(batch);

        // Draw golden burst on success
        if (goldenBurstTimer > 0f) {
            drawGoldenBurst(batch);
        }

        // Draw dark flash on failure
        if (showingResult && !qteManager.isSuccess() && shakeTimer > 0f) {
            drawDarkFlash(batch);
        }

        // Draw correct flash (green)
        if (correctFlashTimer > 0f) {
            drawFlash(batch, ColorPalette.HEAL_GREEN);
        }

        // Draw wrong flash (red)
        if (wrongFlashTimer > 0f) {
            drawFlash(batch, ColorPalette.DAMAGE_RED);
        }

        // Draw boss name and QTE title
        font.setColor(ColorPalette.CRIT_YELLOW);
        font.draw(batch, "QUICK TIME EVENT!", 100f, 225f);
        font.setColor(ColorPalette.TEXT_WHITE);
        font.draw(batch, pattern.getBossName(), 110f, 210f);

        // Draw key prompts
        if (!showingResult) {
            drawKeyPrompts(batch, font);
            drawTimerBar(batch);
            drawProgressInfo(batch, font);
        } else {
            drawResult(batch, font);
        }

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
        renderer.end();

        // Reset camera
        renderer.resetCamera();
    }

    private void drawBackground(SpriteBatch batch) {
        TextureRegion tex = assets.getParticleTexture("physical");
        if (tex != null) {
            batch.setColor(0.1f, 0.05f, 0.15f, 0.9f);
            batch.draw(tex, 0, 0, 320, 240);
            batch.setColor(Color.WHITE);
        }
    }

    private void drawGoldenBurst(SpriteBatch batch) {
        TextureRegion tex = assets.getParticleTexture("physical");
        if (tex != null) {
            float alpha = goldenBurstTimer / GOLDEN_BURST_DURATION;
            batch.setColor(1f, 0.84f, 0f, alpha * 0.5f);
            batch.draw(tex, 0, 0, 320, 240);
            batch.setColor(Color.WHITE);
        }
    }

    private void drawDarkFlash(SpriteBatch batch) {
        TextureRegion tex = assets.getParticleTexture("physical");
        if (tex != null) {
            float alpha = shakeTimer / SHAKE_DURATION;
            batch.setColor(0.2f, 0f, 0f, alpha * 0.6f);
            batch.draw(tex, 0, 0, 320, 240);
            batch.setColor(Color.WHITE);
        }
    }

    private void drawFlash(SpriteBatch batch, Color color) {
        TextureRegion tex = assets.getParticleTexture("physical");
        if (tex != null) {
            float alpha = Math.max(correctFlashTimer, wrongFlashTimer) / FLASH_DURATION;
            batch.setColor(color.r, color.g, color.b, alpha * 0.3f);
            batch.draw(tex, 0, 0, 320, 240);
            batch.setColor(Color.WHITE);
        }
    }

    private void drawKeyPrompts(SpriteBatch batch, BitmapFont font) {
        if (pattern.getType() == QTEPattern.QTEType.MASH) {
            drawMashPrompt(batch, font);
        } else {
            drawSequencePrompt(batch, font);
        }
    }

    private void drawMashPrompt(SpriteBatch batch, BitmapFont font) {
        int[] keys = pattern.getKeys();
        String keyName = QTEPattern.getKeyName(keys[0]);

        // Large key display in center
        font.setColor(ColorPalette.CRIT_YELLOW);
        font.draw(batch, "MASH!", 140f, 170f);

        font.setColor(Color.WHITE);
        font.draw(batch, "[" + keyName + "]", 130f, 145f);

        // Progress counter
        font.setColor(ColorPalette.HEAL_GREEN);
        font.draw(batch, qteManager.getMashCount() + " / " + pattern.getRequiredMashCount(),
                130f, 115f);

        // Draw progress bar
        drawProgressBar(batch, qteManager.getProgress());
    }

    private void drawSequencePrompt(SpriteBatch batch, BitmapFont font) {
        int[] keys = pattern.getKeys();
        int currentIndex = qteManager.getCurrentKeyIndex();

        float startX = 160f - (keys.length * 20f) / 2f;
        float y = 140f;

        for (int i = 0; i < keys.length; i++) {
            String keyName = QTEPattern.getKeyName(keys[i]);

            if (i < currentIndex) {
                // Completed key: green
                font.setColor(ColorPalette.HEAL_GREEN);
            } else if (i == currentIndex) {
                // Current key: bright yellow, larger appearance via brackets
                font.setColor(ColorPalette.CRIT_YELLOW);
            } else {
                // Upcoming key: dimmed
                font.setColor(ColorPalette.UI_BORDER);
            }

            float xPos = startX + i * 20f;

            if (i == currentIndex) {
                font.draw(batch, "[" + keyName + "]", xPos - 4f, y + 15f);
            } else {
                font.draw(batch, keyName, xPos, y);
            }
        }

        // Label
        font.setColor(ColorPalette.TEXT_WHITE);
        font.draw(batch, "Input the sequence!", 100f, 175f);
    }

    private void drawProgressBar(SpriteBatch batch, float progress) {
        TextureRegion tex = assets.getParticleTexture("physical");
        if (tex == null) return;

        float barX = 80f;
        float barY = 90f;
        float barWidth = 160f;
        float barHeight = 6f;

        // Background
        batch.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        batch.draw(tex, barX, barY, barWidth, barHeight);

        // Fill
        batch.setColor(ColorPalette.HEAL_GREEN);
        batch.draw(tex, barX, barY, barWidth * progress, barHeight);

        batch.setColor(Color.WHITE);
    }

    private void drawTimerBar(SpriteBatch batch) {
        TextureRegion tex = assets.getParticleTexture("physical");
        if (tex == null) return;

        float timeFraction = qteManager.getTimeFraction();

        // Background
        batch.setColor(0.3f, 0.1f, 0.1f, 0.8f);
        batch.draw(tex, TIMER_BAR_X, TIMER_BAR_Y, TIMER_BAR_WIDTH, TIMER_BAR_HEIGHT);

        // Timer fill - changes color as time runs out
        if (timeFraction > 0.5f) {
            batch.setColor(ColorPalette.HEAL_GREEN);
        } else if (timeFraction > 0.25f) {
            batch.setColor(ColorPalette.CRIT_YELLOW);
        } else {
            batch.setColor(ColorPalette.DAMAGE_RED);
        }
        batch.draw(tex, TIMER_BAR_X, TIMER_BAR_Y, TIMER_BAR_WIDTH * timeFraction, TIMER_BAR_HEIGHT);

        batch.setColor(Color.WHITE);
    }

    private void drawProgressInfo(SpriteBatch batch, BitmapFont font) {
        float timeRemaining = qteManager.getTimeRemaining();
        font.setColor(ColorPalette.TEXT_WHITE);
        font.draw(batch, String.format("Time: %.1fs", timeRemaining), 130f, 55f);
    }

    private void drawResult(SpriteBatch batch, BitmapFont font) {
        if (qteManager.isSuccess()) {
            font.setColor(ColorPalette.HOLY_GOLD);
            font.draw(batch, "SUCCESS!", 125f, 160f);
            font.setColor(ColorPalette.HEAL_GREEN);
            font.draw(batch, pattern.getSuccessEffect(), 30f, 130f, 260f, -1, true);
        } else {
            font.setColor(ColorPalette.DAMAGE_RED);
            font.draw(batch, "FAILED!", 130f, 160f);
            font.setColor(ColorPalette.DAMAGE_RED);
            font.draw(batch, pattern.getFailureEffect(), 30f, 130f, 260f, -1, true);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (showingResult) return false;
        if (qteManager.isComplete()) return false;

        boolean correct = qteManager.processKeyPress(keycode);
        if (correct) {
            correctFlashTimer = FLASH_DURATION;
        } else {
            wrongFlashTimer = FLASH_DURATION;
        }
        return true;
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
