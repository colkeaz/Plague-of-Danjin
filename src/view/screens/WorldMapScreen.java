package view.screens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import model.world.Area;
import model.world.AreaData;
import model.world.WorldState;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.effects.Particle;
import view.rendering.PixelRenderer;
import view.sprites.ColorPalette;

/**
 * World map screen showing the dungeon hub layout.
 * Displays area nodes colored by status, key count, and connections.
 * Input: number keys 1-4 to select area, R for rest shrine.
 */
public class WorldMapScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;
    private final WorldManager worldManager;

    // Ambient particles
    private final List<Particle> ambientParticles;
    private float particleSpawnTimer;

    // Blinking indicator for player position
    private float blinkTimer;
    private boolean blinkVisible;

    // Node positions (x, y) for each area
    private static final float[][] NODE_POSITIONS = {
        {152f, 70f},   // DANJINS_CORE (bottom center)
        {60f, 130f},   // GOBLIN_WARRENS (left)
        {152f, 140f},  // BONE_CATHEDRAL (center)
        {244f, 130f},  // PLAGUE_GARDENS (right)
        {152f, 195f}   // LICHS_THRONE (top center)
    };

    // Area labels for display
    private static final String[] AREA_LABELS = {
        "Core", "Warrens", "Cathedral", "Gardens", "Throne"
    };

    // Key display message
    private String keyMessage;
    private float keyMessageTimer;

    public WorldMapScreen(PlagueOfDanjinGame game, CombatEngine engine) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;
        this.worldManager = engine.getWorldManager();
        this.ambientParticles = new ArrayList<>();
        this.particleSpawnTimer = 0f;
        this.blinkTimer = 0f;
        this.blinkVisible = true;
        this.keyMessage = null;
        this.keyMessageTimer = 0f;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        // Play world map music
        game.getMusicManager().play("world_map_theme");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(ColorPalette.BACKGROUND.r, ColorPalette.BACKGROUND.g,
                ColorPalette.BACKGROUND.b, 1f);

        // Update blink
        blinkTimer += delta;
        if (blinkTimer >= 0.5f) {
            blinkVisible = !blinkVisible;
            blinkTimer = 0f;
        }

        // Update key message timer
        if (keyMessage != null) {
            keyMessageTimer += delta;
            if (keyMessageTimer > 3f) {
                keyMessage = null;
                keyMessageTimer = 0f;
            }
        }

        updateAmbientParticles(delta);
        game.getMusicManager().update(delta);

        renderer.begin();
        SpriteBatch batch = renderer.getBatch();
        BitmapFont font = assets.getFont();

        // Draw background
        drawBackground(batch);
        drawAmbientParticles(batch);

        // Draw title
        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, "THE DUNGEON OF DANJIN", 85f, 232f);

        // Draw separator line
        TextureRegion particleTex = assets.getParticleTexture("physical");
        if (particleTex != null) {
            batch.setColor(ColorPalette.UI_BORDER);
            batch.draw(particleTex, 20f, 220f, 280f, 1f);
            batch.setColor(Color.WHITE);
        }

        // Draw connections between nodes
        drawConnections(batch, particleTex);

        // Draw area nodes
        WorldState state = worldManager.getWorldState();
        drawAreaNodes(batch, font, state);

        // Draw player position indicator
        drawPlayerPosition(batch, particleTex, state);

        // Draw bottom info bar
        drawInfoBar(batch, font, state, particleTex);

        // Draw key message if present
        if (keyMessage != null) {
            font.setColor(ColorPalette.CRIT_YELLOW);
            font.draw(batch, keyMessage, 80f, 55f);
        }

        // Draw input help
        font.setColor(ColorPalette.HEAL_GREEN);
        font.draw(batch, "1:Warrens 2:Cathedral 3:Gardens 4:Throne R:Rest", 15f, 10f);

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
        renderer.end();
    }

    private void drawConnections(SpriteBatch batch, TextureRegion particleTex) {
        if (particleTex == null) return;

        batch.setColor(ColorPalette.DUNGEON_WALL);

        // Core to Warrens
        drawDottedLine(batch, particleTex, NODE_POSITIONS[0][0], NODE_POSITIONS[0][1],
                NODE_POSITIONS[1][0], NODE_POSITIONS[1][1]);
        // Core to Cathedral
        drawDottedLine(batch, particleTex, NODE_POSITIONS[0][0], NODE_POSITIONS[0][1],
                NODE_POSITIONS[2][0], NODE_POSITIONS[2][1]);
        // Core to Gardens
        drawDottedLine(batch, particleTex, NODE_POSITIONS[0][0], NODE_POSITIONS[0][1],
                NODE_POSITIONS[3][0], NODE_POSITIONS[3][1]);
        // Warrens to Throne
        drawDottedLine(batch, particleTex, NODE_POSITIONS[1][0], NODE_POSITIONS[1][1],
                NODE_POSITIONS[4][0], NODE_POSITIONS[4][1]);
        // Cathedral to Throne
        drawDottedLine(batch, particleTex, NODE_POSITIONS[2][0], NODE_POSITIONS[2][1],
                NODE_POSITIONS[4][0], NODE_POSITIONS[4][1]);
        // Gardens to Throne
        drawDottedLine(batch, particleTex, NODE_POSITIONS[3][0], NODE_POSITIONS[3][1],
                NODE_POSITIONS[4][0], NODE_POSITIONS[4][1]);

        batch.setColor(Color.WHITE);
    }

    private void drawDottedLine(SpriteBatch batch, TextureRegion tex,
                                float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        int dots = (int) (len / 6f);
        for (int i = 0; i < dots; i++) {
            float t = (float) i / dots;
            float px = x1 + dx * t;
            float py = y1 + dy * t;
            batch.draw(tex, px, py, 2f, 2f);
        }
    }

    private void drawAreaNodes(SpriteBatch batch, BitmapFont font, WorldState state) {
        Area[] areas = Area.values();
        for (int i = 0; i < areas.length; i++) {
            Area area = areas[i];
            float nx = NODE_POSITIONS[i][0];
            float ny = NODE_POSITIONS[i][1];

            Color nodeColor = getNodeColor(area, state);

            // Draw node circle
            TextureRegion particleTex = assets.getParticleTexture("physical");
            if (particleTex != null) {
                batch.setColor(nodeColor);
                batch.draw(particleTex, nx - 8f, ny - 8f, 16f, 16f);
                // Draw inner dot
                batch.setColor(ColorPalette.BACKGROUND);
                batch.draw(particleTex, nx - 4f, ny - 4f, 8f, 8f);
                batch.setColor(nodeColor);
                batch.draw(particleTex, nx - 3f, ny - 3f, 6f, 6f);
                batch.setColor(Color.WHITE);
            }

            // Draw label below node
            font.setColor(nodeColor);
            float labelX = nx - AREA_LABELS[i].length() * 2.5f;
            font.draw(batch, AREA_LABELS[i], labelX, ny - 12f);

            // Draw status indicator
            String status = getStatusText(area, state);
            font.setColor(nodeColor.cpy().mul(0.8f, 0.8f, 0.8f, 1f));
            font.draw(batch, status, labelX, ny - 22f);
        }
    }

    private Color getNodeColor(Area area, WorldState state) {
        if (state.isAreaCompleted(area)) {
            return ColorPalette.HEAL_GREEN;
        }
        if (!state.isAreaUnlocked(area)) {
            return ColorPalette.DAMAGE_RED;
        }
        int progress = state.getCurrentEncounterIndex(area);
        if (progress > 0) {
            return ColorPalette.CRIT_YELLOW;
        }
        return ColorPalette.TEXT_WHITE;
    }

    private String getStatusText(Area area, WorldState state) {
        if (state.isAreaCompleted(area)) {
            return "[DONE]";
        }
        if (!state.isAreaUnlocked(area)) {
            return "[LOCKED]";
        }
        AreaData data = worldManager.getAreaData(area);
        if (data != null) {
            int total = data.getEncounters().size();
            int progress = state.getCurrentEncounterIndex(area);
            if (progress > 0) {
                return progress + "/" + total;
            }
        }
        return "";
    }

    private void drawPlayerPosition(SpriteBatch batch, TextureRegion particleTex, WorldState state) {
        if (particleTex == null || !blinkVisible) return;

        Area current = state.getCurrentArea();
        int idx = current.ordinal();
        float px = NODE_POSITIONS[idx][0];
        float py = NODE_POSITIONS[idx][1] + 12f;

        batch.setColor(ColorPalette.HOLY_GOLD);
        batch.draw(particleTex, px - 2f, py, 4f, 4f);
        batch.setColor(Color.WHITE);
    }

    private void drawInfoBar(SpriteBatch batch, BitmapFont font, WorldState state,
                             TextureRegion particleTex) {
        // Draw separator
        if (particleTex != null) {
            batch.setColor(ColorPalette.UI_BORDER);
            batch.draw(particleTex, 20f, 38f, 280f, 1f);
            batch.setColor(Color.WHITE);
        }

        // Key count
        Set<String> keys = state.getKeysCollected();
        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, "Keys: " + keys.size() + "/3", 30f, 32f);

        // Class display
        if (engine.getCharacterClass() != null) {
            font.setColor(ColorPalette.TEXT_WHITE);
            font.draw(batch, "Class: " + engine.getCharacterClass().getDisplayName(), 130f, 32f);
        }

        // Player name
        if (engine.getPlayer() != null) {
            font.setColor(ColorPalette.TEXT_WHITE);
            font.draw(batch, engine.getPlayer().getName(), 230f, 32f);
        }
    }

    private void drawBackground(SpriteBatch batch) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        batch.setColor(0.2f, 0.15f, 0.25f, 0.5f);
        for (int x = 0; x < PixelRenderer.VIRTUAL_WIDTH; x += 16) {
            for (int y = 0; y < PixelRenderer.VIRTUAL_HEIGHT; y += 16) {
                batch.draw(bgTile, x, y);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void updateAmbientParticles(float delta) {
        particleSpawnTimer += delta;
        if (particleSpawnTimer >= 0.2f) {
            particleSpawnTimer = 0f;
            float px = MathUtils.random(0f, PixelRenderer.VIRTUAL_WIDTH);
            float py = MathUtils.random(0f, PixelRenderer.VIRTUAL_HEIGHT);
            float vx = MathUtils.random(-3f, 3f);
            float vy = MathUtils.random(2f, 8f);
            float life = MathUtils.random(1.5f, 3f);
            Color color = MathUtils.randomBoolean() ? ColorPalette.DARK_PURPLE : ColorPalette.LICH_EYES;
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
        WorldState state = worldManager.getWorldState();

        switch (keycode) {
            case Input.Keys.NUM_1:
                tryEnterArea(Area.GOBLIN_WARRENS, state);
                return true;
            case Input.Keys.NUM_2:
                tryEnterArea(Area.BONE_CATHEDRAL, state);
                return true;
            case Input.Keys.NUM_3:
                tryEnterArea(Area.PLAGUE_GARDENS, state);
                return true;
            case Input.Keys.NUM_4:
                tryEnterArea(Area.LICHS_THRONE, state);
                return true;
            case Input.Keys.R:
                game.setScreen(new RestShrineScreen(game, engine));
                return true;
        }
        return false;
    }

    private void tryEnterArea(Area area, WorldState state) {
        if (!state.isAreaUnlocked(area)) {
            if (area == Area.LICHS_THRONE) {
                keyMessage = "Requires 3 keys to enter!";
                keyMessageTimer = 0f;
            }
            return;
        }
        if (state.isAreaCompleted(area)) {
            keyMessage = "Area already completed!";
            keyMessageTimer = 0f;
            return;
        }
        worldManager.enterArea(area);
        game.setScreen(new AreaScreen(game, engine));
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
