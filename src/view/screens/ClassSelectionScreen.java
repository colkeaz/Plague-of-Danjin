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
import model.CharacterClass;
import model.ClassSkillTree;
import model.skills.Skill;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.effects.Particle;
import view.rendering.PixelRenderer;
import view.sprites.AnimationState;
import view.sprites.ColorPalette;

/**
 * Implements Screen. Class selection screen shown after name entry.
 * Displays 3 class options (Knight, Mage, Rogue) with pixel art sprites,
 * stat preview, starting skills, and class passive description.
 * Navigate with 1/2/3 keys or arrow keys + Enter, or mouse click.
 */
public class ClassSelectionScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final String playerName;

    private int selectedIndex;
    private float bgScrollY;
    private float blinkTimer;
    private boolean cursorVisible;
    private float spriteTimer;

    // Ambient particles
    private final List<Particle> ambientParticles;
    private float particleSpawnTimer;

    private static final CharacterClass[] CLASSES = {
        CharacterClass.KNIGHT, CharacterClass.MAGE, CharacterClass.ROGUE
    };

    private static final String[] CLASS_PASSIVES = {
        "Thick Skin: 10% less damage",
        "Arcane Affinity: -3 MP cost, +5 regen",
        "Keen Edge: 25% crit, 2.5x crit dmg"
    };

    public ClassSelectionScreen(PlagueOfDanjinGame game, String playerName) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.playerName = playerName;
        this.selectedIndex = 0;
        this.bgScrollY = 0f;
        this.blinkTimer = 0f;
        this.cursorVisible = true;
        this.spriteTimer = 0f;
        this.ambientParticles = new ArrayList<>();
        this.particleSpawnTimer = 0f;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(ColorPalette.BACKGROUND.r, ColorPalette.BACKGROUND.g,
                ColorPalette.BACKGROUND.b, 1f);

        bgScrollY += delta * 5f;
        spriteTimer += delta;

        blinkTimer += delta;
        if (blinkTimer >= 0.4f) {
            cursorVisible = !cursorVisible;
            blinkTimer = 0f;
        }

        updateAmbientParticles(delta);

        renderer.begin();
        BitmapFont font = assets.getFont();
        SpriteBatch batch = renderer.getBatch();

        // Draw darker background tiles
        drawBackground(batch);

        // Draw ambient particles
        drawAmbientParticles(batch);

        // Draw decorative frame border
        drawDecorativeFrame(batch);

        // Title
        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, "Choose Your Class, " + playerName + "!", 50f, 232f);

        // Render the 3 class panels
        float panelWidth = 100f;
        float startX = 5f;
        float panelY = 215f;

        for (int i = 0; i < CLASSES.length; i++) {
            float x = startX + i * (panelWidth + 5f);
            boolean isSelected = (i == selectedIndex);
            renderClassPanel(batch, font, i, x, panelY, panelWidth, isSelected);
        }

        // Bottom prompt
        if (cursorVisible) {
            font.setColor(ColorPalette.HEAL_GREEN);
            font.draw(batch, "Press 1-3 or Enter to select", 70f, 18f);
        }

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
        renderer.end();
    }

    private void renderClassPanel(SpriteBatch batch, BitmapFont font,
                                  int classIndex, float x, float panelY,
                                  float panelWidth, boolean isSelected) {
        CharacterClass cc = CLASSES[classIndex];
        Color classColor = getClassColor(classIndex);
        Color accentColor = getClassAccentColor(classIndex);

        // Selection indicator
        if (isSelected) {
            TextureRegion particleTex = assets.getParticleTexture("physical");
            if (particleTex != null) {
                batch.setColor(classColor.r, classColor.g, classColor.b, 0.3f);
                batch.draw(particleTex, x, 25f, panelWidth, 190f);
                batch.setColor(Color.WHITE);
            }
        }

        float y = panelY;

        // Class number and name
        String prefix = isSelected ? "> " : "  ";
        font.setColor(isSelected ? ColorPalette.CRIT_YELLOW : classColor);
        font.draw(batch, prefix + (classIndex + 1) + ". " + cc.getDisplayName(), x, y);
        y -= 12f;

        // Draw class sprite
        String spriteKey = "player_" + cc.name().toLowerCase();
        TextureRegion[] frames = assets.getEntityFrames(spriteKey, AnimationState.IDLE);
        if (frames != null && frames.length > 0) {
            int frameIdx = (int) (spriteTimer / 0.5f) % frames.length;
            TextureRegion frame = frames[frameIdx];
            float scale = 2f;
            float spriteW = frame.getRegionWidth() * scale;
            float spriteH = frame.getRegionHeight() * scale;
            float spriteX = x + (panelWidth - spriteW) / 2f;
            batch.setColor(Color.WHITE);
            batch.draw(frame, spriteX, y - spriteH - 2f, spriteW, spriteH);
            y -= spriteH + 6f;
        } else {
            y -= 40f;
        }

        // Stats
        font.setColor(ColorPalette.TEXT_WHITE);
        font.draw(batch, "HP:" + cc.getStartingHp(), x + 2f, y);
        font.draw(batch, "ATK:" + cc.getStartingAtk(), x + 50f, y);
        y -= 10f;
        font.draw(batch, "DEF:" + cc.getStartingDef(), x + 2f, y);
        font.draw(batch, "MP:" + cc.getStartingMp(), x + 50f, y);
        y -= 12f;

        // Starting skills
        font.setColor(accentColor);
        font.draw(batch, "Skills:", x + 2f, y);
        y -= 10f;

        List<Skill> defaultSkills = ClassSkillTree.getDefaultSkills(cc);
        font.setColor(ColorPalette.TEXT_WHITE);
        for (Skill skill : defaultSkills) {
            font.draw(batch, "- " + skill.getName(), x + 2f, y);
            y -= 9f;
        }

        // Class passive
        y -= 3f;
        font.setColor(classColor);
        font.draw(batch, CLASS_PASSIVES[classIndex], x + 2f, y, panelWidth - 4f, -1, true);
    }

    private Color getClassColor(int index) {
        switch (index) {
            case 0: return ColorPalette.KNIGHT_BLUE;
            case 1: return ColorPalette.MAGE_PURPLE;
            case 2: return ColorPalette.ROGUE_GREEN;
            default: return ColorPalette.TEXT_WHITE;
        }
    }

    private Color getClassAccentColor(int index) {
        switch (index) {
            case 0: return ColorPalette.KNIGHT_SILVER;
            case 1: return ColorPalette.MAGE_GOLD;
            case 2: return ColorPalette.ROGUE_CRIMSON;
            default: return ColorPalette.TEXT_WHITE;
        }
    }

    private void drawBackground(SpriteBatch batch) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        int tileW = 16;
        int tileH = 16;
        float scrollOffset = bgScrollY % tileH;

        batch.setColor(0.2f, 0.15f, 0.3f, 0.5f);
        for (int x = 0; x < PixelRenderer.VIRTUAL_WIDTH; x += tileW) {
            for (int y = -tileH; y < PixelRenderer.VIRTUAL_HEIGHT + tileH; y += tileH) {
                batch.draw(bgTile, x, y + scrollOffset);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void drawDecorativeFrame(SpriteBatch batch) {
        TextureRegion menuFrame = assets.getMenuFrame();
        if (menuFrame == null) return;

        batch.draw(menuFrame, 2f, 2f, 16f, 16f);
        batch.draw(menuFrame, PixelRenderer.VIRTUAL_WIDTH - 18f, 2f, 16f, 16f);
        batch.draw(menuFrame, 2f, PixelRenderer.VIRTUAL_HEIGHT - 18f, 16f, 16f);
        batch.draw(menuFrame, PixelRenderer.VIRTUAL_WIDTH - 18f, PixelRenderer.VIRTUAL_HEIGHT - 18f, 16f, 16f);

        for (int x = 20; x < PixelRenderer.VIRTUAL_WIDTH - 20; x += 16) {
            batch.draw(menuFrame, x, PixelRenderer.VIRTUAL_HEIGHT - 18f, 16f, 16f);
            batch.draw(menuFrame, x, 2f, 16f, 16f);
        }

        for (int y = 20; y < PixelRenderer.VIRTUAL_HEIGHT - 20; y += 16) {
            batch.draw(menuFrame, 2f, y, 16f, 16f);
            batch.draw(menuFrame, PixelRenderer.VIRTUAL_WIDTH - 18f, y, 16f, 16f);
        }
    }

    private void updateAmbientParticles(float delta) {
        particleSpawnTimer += delta;
        if (particleSpawnTimer >= 0.2f) {
            particleSpawnTimer = 0f;
            float px = MathUtils.random(0f, PixelRenderer.VIRTUAL_WIDTH);
            float py = MathUtils.random(0f, PixelRenderer.VIRTUAL_HEIGHT);
            float vx = MathUtils.random(-5f, 5f);
            float vy = MathUtils.random(5f, 15f);
            float life = MathUtils.random(1.0f, 2.0f);
            Color color = getClassColor(selectedIndex);
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
        switch (keycode) {
            case Input.Keys.NUM_1:
                selectClass(0);
                return true;
            case Input.Keys.NUM_2:
                selectClass(1);
                return true;
            case Input.Keys.NUM_3:
                selectClass(2);
                return true;
            case Input.Keys.LEFT:
                selectedIndex = Math.max(0, selectedIndex - 1);
                return true;
            case Input.Keys.RIGHT:
                selectedIndex = Math.min(CLASSES.length - 1, selectedIndex + 1);
                return true;
            case Input.Keys.UP:
                selectedIndex = Math.max(0, selectedIndex - 1);
                return true;
            case Input.Keys.DOWN:
                selectedIndex = Math.min(CLASSES.length - 1, selectedIndex + 1);
                return true;
            case Input.Keys.ENTER:
                selectClass(selectedIndex);
                return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 worldCoords = renderer.getViewport().unproject(new Vector2(screenX, screenY));
        float worldX = worldCoords.x;

        float panelWidth = 100f;
        float startX = 5f;

        for (int i = 0; i < CLASSES.length; i++) {
            float panelLeft = startX + i * (panelWidth + 5f);
            float panelRight = panelLeft + panelWidth;
            if (worldX >= panelLeft && worldX <= panelRight) {
                selectClass(i);
                return true;
            }
        }
        return false;
    }

    private void selectClass(int index) {
        if (index < 0 || index >= CLASSES.length) return;
        CharacterClass selectedClass = CLASSES[index];

        CombatEngine engine = new CombatEngine();
        engine.startGame(playerName, selectedClass);

        // Apply unlockable starting bonuses from meta-progression
        controller.MetaProgression meta = game.getMetaProgression();
        controller.SaveManager saveManager = game.getSaveManager();

        engine.setSaveManager(saveManager);
        engine.applyUnlocks(meta, engine.getChestSystem());

        // Record run start
        meta.recordRunStart();

        // Save initial run state
        saveManager.saveRun(engine);

        game.setScreen(new GameScreen(game, engine));
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
