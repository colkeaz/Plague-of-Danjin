package view.screens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import controller.CombatEngine;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.effects.Particle;
import view.rendering.PixelRenderer;
import view.sprites.ColorPalette;

/**
 * Implements Screen. Renders victory celebration with sparkle particles,
 * player victory pose sprite, and credits scroll with pixel font styling.
 */
public class VictoryScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;

    private float creditsScrollY;
    private static final float SCROLL_SPEED = 18f;
    private static final String[] CREDITS = {
            "VICTORY!",
            "",
            "The Plague of Danjin has been vanquished!",
            "",
            "You have conquered all 20 waves",
            "and saved the land from darkness.",
            "",
            "--- Credits ---",
            "",
            "Plague of Danjin",
            "A Roguelike Dungeon Crawler",
            "",
            "Programmatic Pixel Art",
            "Generated at Runtime",
            "",
            "Thank you for playing!",
            "",
            "",
            "Press Enter to play again"
    };

    // Victory celebration particles
    private final List<Particle> sparkleParticles;
    private float sparkleTimer;
    private float burstTimer;

    // Victory player sprite (raised sword pose)
    private Texture victoryPoseTexture;
    private TextureRegion victoryPoseRegion;

    public VictoryScreen(PlagueOfDanjinGame game, CombatEngine engine) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;
        this.creditsScrollY = 0f;
        this.sparkleParticles = new ArrayList<>();
        this.sparkleTimer = 0f;
        this.burstTimer = 0f;

        generateVictoryPose();
    }

    /**
     * Generates a pixel art player sprite in a victory pose (sword raised overhead).
     */
    private void generateVictoryPose() {
        int w = 24;
        int h = 32;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();

        Color armor = ColorPalette.PLAYER_ARMOR;
        Color blue = ColorPalette.PLAYER_BLUE;
        Color gold = ColorPalette.HOLY_GOLD;
        Color skin = new Color(0.9f, 0.75f, 0.6f, 1f);

        // Sword raised above head (gold blade)
        pm.setColor(gold);
        pm.drawPixel(12, 0);
        pm.drawPixel(12, 1);
        pm.drawPixel(12, 2);
        pm.drawPixel(12, 3);
        pm.drawPixel(12, 4);
        pm.drawPixel(11, 5);
        pm.drawPixel(12, 5);
        pm.drawPixel(13, 5);

        // Head (helmet)
        pm.setColor(armor);
        for (int x = 10; x <= 14; x++) {
            pm.drawPixel(x, 6);
            pm.drawPixel(x, 7);
        }
        pm.setColor(blue);
        pm.drawPixel(10, 8);
        pm.drawPixel(14, 8);
        pm.setColor(skin);
        pm.drawPixel(11, 8);
        pm.drawPixel(12, 8);
        pm.drawPixel(13, 8);

        // Helmet visor
        pm.setColor(new Color(0.2f, 0.2f, 0.3f, 1f));
        pm.drawPixel(11, 9);
        pm.drawPixel(12, 9);
        pm.drawPixel(13, 9);

        // Body (armor)
        pm.setColor(armor);
        for (int y = 10; y <= 18; y++) {
            for (int x = 9; x <= 15; x++) {
                pm.drawPixel(x, y);
            }
        }

        // Blue tabard center
        pm.setColor(blue);
        for (int y = 12; y <= 17; y++) {
            pm.drawPixel(11, y);
            pm.drawPixel(12, y);
            pm.drawPixel(13, y);
        }

        // Arms raised (sword arm up, other arm up in V shape)
        pm.setColor(armor);
        // Right arm (holds sword, raised)
        pm.drawPixel(15, 10);
        pm.drawPixel(16, 9);
        pm.drawPixel(16, 8);
        pm.drawPixel(15, 7);

        // Left arm (raised in victory)
        pm.drawPixel(9, 10);
        pm.drawPixel(8, 9);
        pm.drawPixel(8, 8);
        pm.drawPixel(9, 7);

        // Legs
        pm.setColor(blue);
        for (int y = 19; y <= 24; y++) {
            pm.drawPixel(10, y);
            pm.drawPixel(11, y);
            pm.drawPixel(13, y);
            pm.drawPixel(14, y);
        }

        // Boots
        pm.setColor(ColorPalette.GOBLIN_BROWN);
        for (int y = 25; y <= 27; y++) {
            pm.drawPixel(9, y);
            pm.drawPixel(10, y);
            pm.drawPixel(11, y);
            pm.drawPixel(13, y);
            pm.drawPixel(14, y);
            pm.drawPixel(15, y);
        }

        victoryPoseTexture = new Texture(pm);
        victoryPoseTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        victoryPoseRegion = new TextureRegion(victoryPoseTexture);
        pm.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(ColorPalette.BACKGROUND.r, ColorPalette.BACKGROUND.g,
                ColorPalette.BACKGROUND.b, 1f);

        creditsScrollY += delta * SCROLL_SPEED;

        // Update sparkle particles
        updateSparkleParticles(delta);

        renderer.begin();
        BitmapFont font = assets.getFont();
        SpriteBatch batch = renderer.getBatch();

        // Draw background
        drawBackground(batch);

        // Draw sparkle particles
        drawSparkleParticles(batch);

        // Draw victory player sprite
        if (victoryPoseRegion != null) {
            float scale = 3f;
            float spriteW = victoryPoseRegion.getRegionWidth() * scale;
            float spriteH = victoryPoseRegion.getRegionHeight() * scale;
            float spriteX = (PixelRenderer.VIRTUAL_WIDTH - spriteW) / 2f;
            float spriteY = PixelRenderer.VIRTUAL_HEIGHT - spriteH - 20f;
            batch.draw(victoryPoseRegion, spriteX, spriteY, spriteW, spriteH);
        }

        // Draw scrolling credits
        float startY = 20f + creditsScrollY;
        float lineHeight = 14f;

        for (int i = 0; i < CREDITS.length; i++) {
            float yPos = startY + (CREDITS.length - i) * lineHeight;

            if (yPos < -20f || yPos > 260f) continue;

            if (i == 0) {
                // VICTORY! in gold
                font.setColor(ColorPalette.HOLY_GOLD);
            } else if (CREDITS[i].startsWith("---")) {
                font.setColor(ColorPalette.FIRE_ORANGE);
            } else if (CREDITS[i].startsWith("Press")) {
                font.setColor(ColorPalette.HEAL_GREEN);
            } else if (CREDITS[i].startsWith("Plague of Danjin")) {
                font.setColor(ColorPalette.HOLY_GOLD);
            } else {
                font.setColor(ColorPalette.TEXT_WHITE);
            }

            // Center text roughly
            float textWidth = CREDITS[i].length() * 4.5f;
            float textX = (PixelRenderer.VIRTUAL_WIDTH - textWidth) / 2f;
            font.draw(batch, CREDITS[i], textX, yPos);
        }

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
        renderer.end();
    }

    private void drawBackground(SpriteBatch batch) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        int tileW = 16;
        int tileH = 16;

        batch.setColor(0.1f, 0.15f, 0.1f, 0.3f);
        for (int x = 0; x < PixelRenderer.VIRTUAL_WIDTH; x += tileW) {
            for (int y = 0; y < PixelRenderer.VIRTUAL_HEIGHT; y += tileH) {
                batch.draw(bgTile, x, y);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void updateSparkleParticles(float delta) {
        // Continuous sparkles
        sparkleTimer += delta;
        if (sparkleTimer >= 0.08f) {
            sparkleTimer = 0f;
            spawnSparkle();
        }

        // Periodic golden bursts
        burstTimer += delta;
        if (burstTimer >= 2.0f) {
            burstTimer = 0f;
            spawnGoldenBurst();
        }

        // Update all particles
        Iterator<Particle> it = sparkleParticles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update(delta);
            if (!p.isAlive()) {
                it.remove();
            }
        }

        // Cap particle count
        while (sparkleParticles.size() > 100) {
            sparkleParticles.remove(0);
        }
    }

    private void spawnSparkle() {
        float px = MathUtils.random(20f, PixelRenderer.VIRTUAL_WIDTH - 20f);
        float py = MathUtils.random(20f, PixelRenderer.VIRTUAL_HEIGHT - 20f);
        float vx = MathUtils.random(-10f, 10f);
        float vy = MathUtils.random(5f, 20f);
        float life = MathUtils.random(0.8f, 1.8f);
        Color color;
        float r = MathUtils.random();
        if (r < 0.4f) {
            color = ColorPalette.HOLY_GOLD;
        } else if (r < 0.7f) {
            color = ColorPalette.HOLY_WHITE;
        } else {
            color = ColorPalette.FIRE_YELLOW;
        }
        Particle p = new Particle(px, py, vx, vy, life, MathUtils.random(1, 2), color);
        sparkleParticles.add(p);
    }

    private void spawnGoldenBurst() {
        float cx = PixelRenderer.VIRTUAL_WIDTH / 2f;
        float cy = PixelRenderer.VIRTUAL_HEIGHT / 2f + 30f;
        for (int i = 0; i < 15; i++) {
            float angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
            float speed = MathUtils.random(20f, 60f);
            float vx = MathUtils.cos(angle) * speed;
            float vy = MathUtils.sin(angle) * speed;
            float life = MathUtils.random(0.5f, 1.2f);
            Color color = MathUtils.randomBoolean() ? ColorPalette.HOLY_GOLD : ColorPalette.CRIT_YELLOW;
            Particle p = new Particle(cx + MathUtils.random(-5f, 5f), cy + MathUtils.random(-5f, 5f),
                    vx, vy, life, 2, color);
            p.setGravity(-20f);
            sparkleParticles.add(p);
        }
    }

    private void drawSparkleParticles(SpriteBatch batch) {
        TextureRegion particleTex = assets.getParticleTexture("physical");
        if (particleTex == null) return;
        for (Particle p : sparkleParticles) {
            p.render(batch, particleTex);
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER) {
            game.setScreen(new MainMenuScreen(game));
            return true;
        }
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
    public void hide() {}

    @Override
    public void dispose() {
        if (victoryPoseTexture != null) {
            victoryPoseTexture.dispose();
        }
    }
}
