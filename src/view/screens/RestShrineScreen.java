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
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.effects.Particle;
import view.rendering.PixelRenderer;
import view.sprites.ColorPalette;

/**
 * Rest shrine screen offering rest (heal once per run) and lore viewing.
 * Options: 1-Rest, 2-Lore, 3-Return to map.
 */
public class RestShrineScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;
    private final WorldManager worldManager;

    private boolean restUsed;
    private boolean showingLore;
    private int loreIndex;
    private String statusMessage;
    private float statusTimer;

    // Lore texts about Morthga's fall
    private static final String[] LORE_TEXTS = {
        "The kingdom of Morthga once stood proud above these depths. " +
        "Its people knew peace for generations under the watchful eye " +
        "of the royal lineage. But power draws corruption like flame " +
        "draws moths, and none were immune to its call.",

        "The court wizard discovered Danjin's Heart deep beneath the " +
        "castle. An artifact of immense power, pulsing with plague " +
        "energy older than the kingdom itself. He sought to harness " +
        "it, and in doing so, unleashed the plague that consumed all.",

        "The Lich was once the king's most trusted advisor. When the " +
        "plague took hold, he embraced death to preserve his power. " +
        "Now he sits upon a throne of black iron, commanding the " +
        "dead to guard Danjin's Heart for eternity."
    };

    // Ambient particles
    private final List<Particle> ambientParticles;
    private float particleSpawnTimer;
    private float blinkTimer;
    private boolean cursorVisible;

    public RestShrineScreen(PlagueOfDanjinGame game, CombatEngine engine) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;
        this.worldManager = engine.getWorldManager();
        this.restUsed = worldManager.isRestShrineUsed();
        this.showingLore = false;
        this.loreIndex = 0;
        this.statusMessage = null;
        this.statusTimer = 0f;
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

        blinkTimer += delta;
        if (blinkTimer >= 0.4f) {
            cursorVisible = !cursorVisible;
            blinkTimer = 0f;
        }

        if (statusMessage != null) {
            statusTimer += delta;
            if (statusTimer > 3f) {
                statusMessage = null;
                statusTimer = 0f;
            }
        }

        updateAmbientParticles(delta);
        game.getMusicManager().update(delta);

        renderer.begin();
        SpriteBatch batch = renderer.getBatch();
        BitmapFont font = assets.getFont();

        drawBackground(batch);
        drawAmbientParticles(batch);

        // Title
        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, "REST SHRINE", 115f, 230f);

        TextureRegion particleTex = assets.getParticleTexture("physical");
        if (particleTex != null) {
            batch.setColor(ColorPalette.UI_BORDER);
            batch.draw(particleTex, 30f, 218f, 260f, 1f);
            batch.setColor(Color.WHITE);
        }

        if (showingLore) {
            renderLore(font, batch);
        } else {
            renderMainMenu(font, batch);
        }

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
        renderer.end();
    }

    private void renderMainMenu(BitmapFont font, SpriteBatch batch) {
        font.setColor(ColorPalette.TEXT_WHITE);
        font.draw(batch, "A faint warmth emanates from the shrine.", 50f, 200f);
        font.draw(batch, "The weary find solace here.", 80f, 185f);

        float y = 150f;
        float lineH = 25f;

        // Option 1: Rest
        if (restUsed) {
            font.setColor(0.4f, 0.4f, 0.4f, 1f);
            font.draw(batch, "1. Rest (already used)", 60f, y);
        } else {
            font.setColor(ColorPalette.HEAL_GREEN);
            font.draw(batch, "1. Rest (full heal - once per run)", 60f, y);
        }
        y -= lineH;

        // Option 2: Lore
        font.setColor(ColorPalette.MAGE_PURPLE);
        font.draw(batch, "2. Read Lore Stones", 60f, y);
        y -= lineH;

        // Option 3: Return
        font.setColor(ColorPalette.TEXT_WHITE);
        font.draw(batch, "3. Return to World Map", 60f, y);

        // Status message
        if (statusMessage != null) {
            font.setColor(ColorPalette.CRIT_YELLOW);
            font.draw(batch, statusMessage, 70f, 50f);
        }

        // Player HP display
        Player player = engine.getPlayer();
        if (player != null) {
            font.setColor(ColorPalette.HP_RED);
            font.draw(batch, "HP: " + player.getHp() + "/" + player.getMaxHp(), 30f, 30f);
        }
    }

    private void renderLore(BitmapFont font, SpriteBatch batch) {
        font.setColor(ColorPalette.MAGE_GOLD);
        font.draw(batch, "- Lore Stone " + (loreIndex + 1) + "/" + LORE_TEXTS.length + " -", 90f, 200f);

        font.setColor(ColorPalette.TEXT_WHITE);
        font.draw(batch, LORE_TEXTS[loreIndex], 15f, 175f, 290f, -1, true);

        if (cursorVisible) {
            font.setColor(ColorPalette.HEAL_GREEN);
            if (loreIndex < LORE_TEXTS.length - 1) {
                font.draw(batch, "Enter: Next   Esc: Back", 80f, 30f);
            } else {
                font.draw(batch, "Enter/Esc: Back", 100f, 30f);
            }
        }
    }

    private void drawBackground(SpriteBatch batch) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        batch.setColor(0.15f, 0.2f, 0.15f, 0.4f);
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
            float px = MathUtils.random(100f, 220f);
            float py = MathUtils.random(100f, 200f);
            float vx = MathUtils.random(-2f, 2f);
            float vy = MathUtils.random(5f, 15f);
            float life = MathUtils.random(1f, 2f);
            Color color = MathUtils.randomBoolean() ? ColorPalette.HOLY_GOLD : ColorPalette.HEAL_GREEN;
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
        while (ambientParticles.size() > 30) {
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
        if (showingLore) {
            if (keycode == Input.Keys.ENTER) {
                if (loreIndex < LORE_TEXTS.length - 1) {
                    loreIndex++;
                } else {
                    showingLore = false;
                }
            } else if (keycode == Input.Keys.ESCAPE) {
                showingLore = false;
            }
            return true;
        }

        switch (keycode) {
            case Input.Keys.NUM_1:
                if (!restUsed) {
                    doRest();
                } else {
                    statusMessage = "Already rested this run.";
                    statusTimer = 0f;
                }
                return true;
            case Input.Keys.NUM_2:
                showingLore = true;
                loreIndex = 0;
                return true;
            case Input.Keys.NUM_3:
            case Input.Keys.ESCAPE:
                game.setScreen(new WorldMapScreen(game, engine));
                return true;
        }
        return false;
    }

    private void doRest() {
        Player player = engine.getPlayer();
        if (player != null) {
            player.setHp(player.getMaxHp());
        }
        worldManager.useRestShrine();
        restUsed = true;
        statusMessage = "Fully healed!";
        statusTimer = 0f;
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
