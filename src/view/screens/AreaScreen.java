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
import controller.GameState;
import controller.WorldManager;
import model.world.Area;
import model.world.AreaData;
import model.world.AreaEvent;
import model.world.Encounter;
import model.world.WorldState;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.effects.Particle;
import view.rendering.PixelRenderer;
import view.sprites.ColorPalette;

/**
 * Shows the current area with encounter list, progress, and navigation options.
 * Has typewriter intro text on first entry.
 * Input: Enter to start next encounter, Escape to retreat to hub.
 */
public class AreaScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;
    private final WorldManager worldManager;

    private final Area currentArea;
    private final AreaData areaData;
    private final List<Encounter> encounters;
    private final int currentEncounterIndex;

    // Typewriter intro
    private float typewriterTimer;
    private int typedCharCount;
    private boolean typewriterDone;
    private final String introText;
    private static final float TYPEWRITER_SPEED = 30f;

    // Shows key earned message
    private boolean showingKeyEarned;
    private String keyEarnedMessage;

    // Ambient particles
    private final List<Particle> ambientParticles;
    private float particleSpawnTimer;

    // Blink cursor
    private float blinkTimer;
    private boolean cursorVisible;

    public AreaScreen(PlagueOfDanjinGame game, CombatEngine engine) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;
        this.worldManager = engine.getWorldManager();

        WorldState state = worldManager.getWorldState();
        this.currentArea = state.getCurrentArea();
        this.areaData = worldManager.getAreaData(currentArea);
        this.encounters = areaData != null ? areaData.getEncounters() : new ArrayList<>();
        this.currentEncounterIndex = state.getCurrentEncounterIndex(currentArea);

        this.introText = areaData != null ? areaData.getStoryText() : "";
        this.typewriterTimer = 0f;
        this.typedCharCount = 0;
        this.typewriterDone = (currentEncounterIndex > 0);
        this.showingKeyEarned = false;
        this.keyEarnedMessage = "";
        this.ambientParticles = new ArrayList<>();
        this.particleSpawnTimer = 0f;
        this.blinkTimer = 0f;
        this.cursorVisible = true;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        // Play area-specific music
        if (currentArea == Area.PLAGUE_GARDENS) {
            game.getMusicManager().play("plague_gardens_theme");
        } else {
            game.getMusicManager().play("dungeon_theme");
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(ColorPalette.BACKGROUND.r, ColorPalette.BACKGROUND.g,
                ColorPalette.BACKGROUND.b, 1f);

        // Update typewriter
        if (!typewriterDone) {
            typewriterTimer += delta * TYPEWRITER_SPEED;
            typedCharCount = Math.min((int) typewriterTimer, introText.length());
            if (typedCharCount >= introText.length()) {
                typewriterDone = true;
            }
        }

        // Update blink
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

        // Draw background
        drawBackground(batch);
        drawAmbientParticles(batch);

        // Draw area name
        String areaName = areaData != null ? areaData.getName() : "Unknown Area";
        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, areaName, 10f, 232f);

        // Draw separator
        TextureRegion particleTex = assets.getParticleTexture("physical");
        if (particleTex != null) {
            batch.setColor(ColorPalette.UI_BORDER);
            batch.draw(particleTex, 10f, 222f, 300f, 1f);
            batch.setColor(Color.WHITE);
        }

        if (!typewriterDone) {
            // Show typewriter intro text
            font.setColor(ColorPalette.TEXT_WHITE);
            String displayText = introText.substring(0, typedCharCount);
            font.draw(batch, displayText, 10f, 210f, 300f, -1, true);

            if (cursorVisible) {
                font.setColor(ColorPalette.HEAL_GREEN);
                font.draw(batch, "Press Enter to skip", 90f, 20f);
            }
        } else if (showingKeyEarned) {
            // Show key earned message
            font.setColor(ColorPalette.CRIT_YELLOW);
            font.draw(batch, keyEarnedMessage, 60f, 140f);
            font.setColor(ColorPalette.HEAL_GREEN);
            font.draw(batch, "Press Enter to continue", 80f, 100f);
        } else {
            // Show encounter list and progress
            renderEncounterList(batch, font);
            renderActions(batch, font);
        }

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
        renderer.end();
    }

    private void renderEncounterList(SpriteBatch batch, BitmapFont font) {
        // Area description
        if (areaData != null) {
            font.setColor(ColorPalette.TEXT_WHITE);
            font.draw(batch, areaData.getDescription(), 10f, 210f, 300f, -1, true);
        }

        // Encounter nodes
        float startY = 185f;
        float lineH = 16f;
        int maxVisible = Math.min(encounters.size(), 8);

        for (int i = 0; i < maxVisible; i++) {
            float y = startY - i * lineH;
            Encounter enc = encounters.get(i);

            String prefix;
            Color color;
            if (i < currentEncounterIndex) {
                // Completed
                prefix = "[X] ";
                color = ColorPalette.HEAL_GREEN;
            } else if (i == currentEncounterIndex) {
                // Current/next
                prefix = ">>> ";
                color = ColorPalette.CRIT_YELLOW;
            } else {
                // Locked/future
                prefix = "[ ] ";
                color = new Color(0.4f, 0.4f, 0.4f, 1f);
            }

            font.setColor(color);
            font.draw(batch, prefix + enc.getDescription(), 15f, y);
        }

        // Show next encounter flavor text
        if (currentEncounterIndex < encounters.size()) {
            Encounter next = encounters.get(currentEncounterIndex);
            font.setColor(new Color(0.7f, 0.7f, 0.8f, 1f));
            font.draw(batch, next.getFlavorText(), 15f, 50f, 290f, -1, true);
        }
    }

    private void renderActions(SpriteBatch batch, BitmapFont font) {
        if (currentEncounterIndex >= encounters.size()) {
            // Area complete
            font.setColor(ColorPalette.HOLY_GOLD);
            font.draw(batch, "Area Complete! Press ESC to return.", 50f, 20f);
        } else {
            if (cursorVisible) {
                font.setColor(ColorPalette.HEAL_GREEN);
                font.draw(batch, "Enter: Begin Encounter  Esc: Retreat to Hub", 30f, 20f);
            }
        }
    }

    private void drawBackground(SpriteBatch batch) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        // Tint based on area
        Color tint = getAreaTint();
        batch.setColor(tint.r, tint.g, tint.b, 0.4f);
        for (int x = 0; x < PixelRenderer.VIRTUAL_WIDTH; x += 16) {
            for (int y = 0; y < PixelRenderer.VIRTUAL_HEIGHT; y += 16) {
                batch.draw(bgTile, x, y);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private Color getAreaTint() {
        switch (currentArea) {
            case GOBLIN_WARRENS: return ColorPalette.GOBLIN_GREEN;
            case BONE_CATHEDRAL: return ColorPalette.BONE_WHITE;
            case PLAGUE_GARDENS: return ColorPalette.POISON_GREEN;
            case LICHS_THRONE: return ColorPalette.LICH_PURPLE;
            default: return ColorPalette.DUNGEON_WALL;
        }
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
            Color color = getAreaTint();
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
        while (ambientParticles.size() > 50) {
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
        if (!typewriterDone) {
            if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                // Skip typewriter
                typedCharCount = introText.length();
                typewriterDone = true;
            }
            return true;
        }

        if (showingKeyEarned) {
            if (keycode == Input.Keys.ENTER) {
                game.setScreen(new WorldMapScreen(game, engine));
            }
            return true;
        }

        switch (keycode) {
            case Input.Keys.ENTER:
                startNextEncounter();
                return true;
            case Input.Keys.ESCAPE:
                worldManager.retreatToHub();
                game.setScreen(new WorldMapScreen(game, engine));
                return true;
        }
        return false;
    }

    private void startNextEncounter() {
        if (currentEncounterIndex >= encounters.size()) {
            // Area complete, go back to map
            game.setScreen(new WorldMapScreen(game, engine));
            return;
        }

        Encounter encounter = encounters.get(currentEncounterIndex);

        switch (encounter.getType()) {
            case COMBAT:
            case BOSS:
                engine.startEncounter(currentArea, currentEncounterIndex);
                game.setScreen(new GameScreen(game, engine));
                break;
            case EVENT:
                // Route to area event screen
                AreaEvent event = worldManager.getAreaEvent(currentArea);
                if (event != null) {
                    game.setScreen(new AreaEventScreen(game, engine, event));
                } else {
                    // No event defined, skip to next encounter
                    worldManager.getWorldState().advanceEncounter(currentArea);
                    game.setScreen(new AreaScreen(game, engine));
                }
                break;
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
