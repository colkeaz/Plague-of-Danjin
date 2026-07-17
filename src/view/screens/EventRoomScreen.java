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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import controller.CombatEngine;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.effects.Particle;
import view.rendering.PixelRenderer;
import view.sprites.ColorPalette;

/**
 * Implements Screen. Shows narrative text from EventRoomManager.getEventRoomDescription()
 * with typewriter effect. Displays 2-3 choice buttons with pixel-art styling.
 * Has thematic background variation per event type and ambient particles.
 */
public class EventRoomScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;

    private final String roomDescription;
    private final List<String> choices;
    private int selectedIndex;

    private float typewriterTimer;
    private int typedCharCount;
    private boolean typewriterDone;
    private static final float TYPEWRITER_SPEED = 25f;

    // Theme based on event type
    private final boolean isDarkEvent;
    private final Color bgTint;
    private final Color ambientParticleColor;

    // Ambient particles
    private final List<Particle> ambientParticles;
    private float particleSpawnTimer;

    public EventRoomScreen(PlagueOfDanjinGame game, CombatEngine engine) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;
        this.roomDescription = engine.getEventRoomManager().getEventRoomDescription(engine.getCurrentWave());
        this.choices = engine.getEventRoomManager().getChoices(engine.getCurrentWave());
        this.selectedIndex = 0;
        this.typewriterTimer = 0f;
        this.typedCharCount = 0;
        this.typewriterDone = false;
        this.ambientParticles = new ArrayList<>();
        this.particleSpawnTimer = 0f;

        // Determine event theme based on description content or wave
        int wave = engine.getCurrentWave();
        boolean dark = false;
        if (roomDescription != null) {
            String lower = roomDescription.toLowerCase();
            dark = lower.contains("danjin") || lower.contains("dark") ||
                   lower.contains("cursed") || lower.contains("plague");
        }
        // Later waves tend to be darker
        if (wave >= 15) dark = true;
        this.isDarkEvent = dark;

        if (isDarkEvent) {
            bgTint = new Color(0.15f, 0.05f, 0.2f, 0.6f);
            ambientParticleColor = ColorPalette.DARK_PURPLE;
        } else {
            bgTint = new Color(0.1f, 0.15f, 0.12f, 0.4f);
            ambientParticleColor = ColorPalette.HOLY_GOLD;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        if (isDarkEvent) {
            ScreenUtils.clear(0.03f, 0.01f, 0.06f, 1f);
        } else {
            ScreenUtils.clear(0.06f, 0.07f, 0.05f, 1f);
        }

        // Update typewriter
        if (!typewriterDone && roomDescription != null) {
            typewriterTimer += delta * TYPEWRITER_SPEED;
            typedCharCount = Math.min((int) typewriterTimer, roomDescription.length());
            if (typedCharCount >= roomDescription.length()) {
                typewriterDone = true;
            }
        }

        // Update ambient particles
        updateAmbientParticles(delta);

        renderer.begin();
        BitmapFont font = assets.getFont();
        SpriteBatch batch = renderer.getBatch();

        // Draw themed background tiles
        drawThemedBackground(batch);

        // Draw ambient particles
        drawAmbientParticles(batch);

        // Draw decorative frame around choices area
        drawChoiceFrame(batch);

        // Title
        font.setColor(isDarkEvent ? ColorPalette.DARK_PURPLE : ColorPalette.HOLY_GOLD);
        font.draw(batch, "~ Event Room ~", 110f, 232f);

        // Room description with typewriter
        font.setColor(ColorPalette.TEXT_WHITE);
        if (roomDescription != null) {
            String displayText = roomDescription.substring(0, typedCharCount);
            font.draw(batch, displayText, 20f, 210f, 280f, -1, true);
        }

        // Choices (only shown after typewriter completes)
        if (typewriterDone && choices != null) {
            float choiceY = 130f;
            float lineHeight = 28f;

            for (int i = 0; i < choices.size(); i++) {
                float yPos = choiceY - i * lineHeight;
                boolean isSelected = (i == selectedIndex);

                // Draw small frame/indicator next to choice
                TextureRegion menuFrame = assets.getMenuFrame();
                if (menuFrame != null && isSelected) {
                    batch.setColor(ColorPalette.HOLY_GOLD.r, ColorPalette.HOLY_GOLD.g,
                            ColorPalette.HOLY_GOLD.b, 0.5f);
                    batch.draw(menuFrame, 6f, yPos - 12f, 10f, 10f);
                    batch.setColor(Color.WHITE);
                }

                font.setColor(isSelected ? ColorPalette.CRIT_YELLOW : ColorPalette.TEXT_WHITE);
                String prefix = isSelected ? "> " : "  ";
                font.draw(batch, prefix + (i + 1) + ". " + choices.get(i), 18f, yPos, 284f, -1, true);
            }

            font.setColor(ColorPalette.HEAL_GREEN);
            font.draw(batch, "Press 1-" + choices.size() + " or Enter", 95f, 20f);
        } else if (!typewriterDone) {
            font.setColor(ColorPalette.UI_BORDER);
            font.draw(batch, "(Press Enter to skip)", 95f, 20f);
        }

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
        renderer.end();
    }

    private void drawThemedBackground(SpriteBatch batch) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        int tileW = 16;
        int tileH = 16;

        batch.setColor(bgTint.r, bgTint.g, bgTint.b, bgTint.a);
        for (int x = 0; x < PixelRenderer.VIRTUAL_WIDTH; x += tileW) {
            for (int y = 0; y < PixelRenderer.VIRTUAL_HEIGHT; y += tileH) {
                batch.draw(bgTile, x, y);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void drawChoiceFrame(SpriteBatch batch) {
        if (!typewriterDone || choices == null) return;

        TextureRegion menuFrame = assets.getMenuFrame();
        if (menuFrame == null) return;

        // Frame around the choices area
        float frameX = 4f;
        float frameY = 14f;
        float frameW = PixelRenderer.VIRTUAL_WIDTH - 8f;
        float frameH = choices.size() * 28f + 20f;

        // Draw corners
        batch.setColor(ColorPalette.UI_BORDER.r, ColorPalette.UI_BORDER.g,
                ColorPalette.UI_BORDER.b, 0.7f);
        batch.draw(menuFrame, frameX, frameY, 12f, 12f);
        batch.draw(menuFrame, frameX + frameW - 12f, frameY, 12f, 12f);
        batch.draw(menuFrame, frameX, frameY + frameH - 12f, 12f, 12f);
        batch.draw(menuFrame, frameX + frameW - 12f, frameY + frameH - 12f, 12f, 12f);
        batch.setColor(Color.WHITE);
    }

    private void updateAmbientParticles(float delta) {
        particleSpawnTimer += delta;
        if (particleSpawnTimer >= 0.25f) {
            particleSpawnTimer = 0f;
            float px = MathUtils.random(10f, PixelRenderer.VIRTUAL_WIDTH - 10f);
            float py = MathUtils.random(10f, PixelRenderer.VIRTUAL_HEIGHT - 10f);
            float vx = MathUtils.random(-3f, 3f);
            float vy = MathUtils.random(3f, 10f);
            float life = MathUtils.random(1.5f, 3.0f);
            Particle p = new Particle(px, py, vx, vy, life, 1, ambientParticleColor);
            ambientParticles.add(p);
        }

        Iterator<Particle> it = ambientParticles.iterator();
        while (it.hasNext()) {
            Particle pp = it.next();
            pp.update(delta);
            if (!pp.isAlive()) {
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
        // Allow skipping typewriter
        if (!typewriterDone) {
            if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                typedCharCount = roomDescription != null ? roomDescription.length() : 0;
                typewriterDone = true;
            }
            return true;
        }

        if (choices == null || choices.isEmpty()) return false;

        switch (keycode) {
            case Input.Keys.NUM_1:
                makeChoice(0);
                return true;
            case Input.Keys.NUM_2:
                if (choices.size() > 1) makeChoice(1);
                return true;
            case Input.Keys.NUM_3:
                if (choices.size() > 2) makeChoice(2);
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

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!typewriterDone) {
            typedCharCount = roomDescription != null ? roomDescription.length() : 0;
            typewriterDone = true;
            return true;
        }

        if (choices == null || choices.isEmpty()) return false;

        Vector2 worldCoords = renderer.getViewport().unproject(new Vector2(screenX, screenY));
        float worldY = worldCoords.y;

        float choiceStartY = 130f;
        float lineHeight = 28f;

        for (int i = 0; i < choices.size(); i++) {
            float optionY = choiceStartY - i * lineHeight;
            if (worldY >= optionY - lineHeight / 2f && worldY <= optionY + lineHeight / 2f) {
                makeChoice(i);
                return true;
            }
        }
        return false;
    }

    private void makeChoice(int index) {
        if (index >= 0 && index < choices.size()) {
            engine.processEventRoomChoice(index);
            game.setScreen(new GameScreen(game, engine));
        }
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
