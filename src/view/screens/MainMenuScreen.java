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

import controller.MetaProgression;
import controller.SaveManager;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.effects.Particle;
import view.rendering.PixelRenderer;
import view.sprites.ColorPalette;

/**
 * Main menu screen with options: New Game, Continue, Stats, Unlocks.
 * Navigate with arrow keys + Enter or number keys 1-4.
 * Continue is grayed out if no save exists.
 * Pixel art styling consistent with IntroScreen.
 */
public class MainMenuScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final SaveManager saveManager;
    private final MetaProgression metaProgression;

    private static final String[] MENU_OPTIONS = {"New Game", "Continue", "Stats", "Unlocks"};
    private int selectedIndex = 0;
    private boolean hasSaveFile;

    // Overlay states
    private boolean showingStats;
    private boolean showingUnlocks;

    // Visual elements
    private Texture titleTexture;
    private TextureRegion titleRegion;
    private float blinkTimer;
    private boolean cursorVisible;
    private float backgroundScrollY;
    private static final float BG_SCROLL_SPEED = 8f;

    // Ambient particles
    private final List<Particle> ambientParticles;
    private float particleSpawnTimer;

    public MainMenuScreen(PlagueOfDanjinGame game) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.saveManager = game.getSaveManager();
        this.metaProgression = game.getMetaProgression();
        this.hasSaveFile = saveManager.hasSave();
        this.showingStats = false;
        this.showingUnlocks = false;
        this.blinkTimer = 0f;
        this.cursorVisible = true;
        this.backgroundScrollY = 0f;
        this.ambientParticles = new ArrayList<>();
        this.particleSpawnTimer = 0f;

        generateTitleLogo();
    }

    private void generateTitleLogo() {
        String line1 = "PLAGUE OF";
        String line2 = "DANJIN";
        int charWidth = 6;
        int charHeight = 7;
        int lineSpacing = 3;
        int totalWidth = Math.max(line1.length(), line2.length()) * charWidth;
        int totalHeight = charHeight * 2 + lineSpacing;

        Pixmap pm = new Pixmap(totalWidth, totalHeight, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();

        int line1Width = line1.length() * charWidth;
        int line1Offset = (totalWidth - line1Width) / 2;
        drawPixelText(pm, line1, line1Offset, 0, ColorPalette.HOLY_GOLD);

        int line2Width = line2.length() * charWidth;
        int line2Offset = (totalWidth - line2Width) / 2;
        drawPixelText(pm, line2, line2Offset, charHeight + lineSpacing, ColorPalette.FIRE_ORANGE);

        titleTexture = new Texture(pm);
        titleTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        titleRegion = new TextureRegion(titleTexture);
        pm.dispose();
    }

    private void drawPixelText(Pixmap pm, String text, int startX, int startY, Color color) {
        pm.setColor(color);
        for (int i = 0; i < text.length(); i++) {
            int cx = startX + i * 6;
            drawPixelChar(pm, text.charAt(i), cx, startY);
        }
    }

    private void drawPixelChar(Pixmap pm, char c, int ox, int oy) {
        int[] pattern = getCharPattern(c);
        if (pattern == null) return;
        for (int row = 0; row < 7 && row < pattern.length; row++) {
            int bits = pattern[row];
            for (int col = 0; col < 5; col++) {
                if ((bits & (1 << (4 - col))) != 0) {
                    pm.drawPixel(ox + col, oy + row);
                }
            }
        }
    }

    private int[] getCharPattern(char c) {
        switch (c) {
            case 'A': return new int[]{0b01110, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001};
            case 'B': return new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10001, 0b10001, 0b11110};
            case 'C': return new int[]{0b01110, 0b10001, 0b10000, 0b10000, 0b10000, 0b10001, 0b01110};
            case 'D': return new int[]{0b11100, 0b10010, 0b10001, 0b10001, 0b10001, 0b10010, 0b11100};
            case 'E': return new int[]{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b11111};
            case 'F': return new int[]{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b10000};
            case 'G': return new int[]{0b01110, 0b10001, 0b10000, 0b10111, 0b10001, 0b10001, 0b01110};
            case 'H': return new int[]{0b10001, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001};
            case 'I': return new int[]{0b01110, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110};
            case 'J': return new int[]{0b00111, 0b00010, 0b00010, 0b00010, 0b00010, 0b10010, 0b01100};
            case 'K': return new int[]{0b10001, 0b10010, 0b10100, 0b11000, 0b10100, 0b10010, 0b10001};
            case 'L': return new int[]{0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b11111};
            case 'M': return new int[]{0b10001, 0b11011, 0b10101, 0b10101, 0b10001, 0b10001, 0b10001};
            case 'N': return new int[]{0b10001, 0b11001, 0b10101, 0b10011, 0b10001, 0b10001, 0b10001};
            case 'O': return new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110};
            case 'P': return new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10000, 0b10000, 0b10000};
            case 'Q': return new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b10101, 0b10010, 0b01101};
            case 'R': return new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10010, 0b10001};
            case 'S': return new int[]{0b01110, 0b10001, 0b10000, 0b01110, 0b00001, 0b10001, 0b01110};
            case 'T': return new int[]{0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100};
            case 'U': return new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110};
            case 'V': return new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01010, 0b00100};
            case 'W': return new int[]{0b10001, 0b10001, 0b10001, 0b10101, 0b10101, 0b11011, 0b10001};
            case 'X': return new int[]{0b10001, 0b10001, 0b01010, 0b00100, 0b01010, 0b10001, 0b10001};
            case 'Y': return new int[]{0b10001, 0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b00100};
            case 'Z': return new int[]{0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b10000, 0b11111};
            case ' ': return null;
            default: return null;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        hasSaveFile = saveManager.hasSave();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(ColorPalette.BACKGROUND.r, ColorPalette.BACKGROUND.g,
                ColorPalette.BACKGROUND.b, 1f);

        backgroundScrollY += delta * BG_SCROLL_SPEED;

        blinkTimer += delta;
        if (blinkTimer >= 0.4f) {
            cursorVisible = !cursorVisible;
            blinkTimer = 0f;
        }

        updateAmbientParticles(delta);

        renderer.begin();
        SpriteBatch batch = renderer.getBatch();
        BitmapFont font = assets.getFont();

        drawScrollingBackground(batch);
        drawAmbientParticles(batch);

        if (showingStats) {
            renderStatsOverlay(batch, font);
        } else if (showingUnlocks) {
            renderUnlocksOverlay(batch, font);
        } else {
            renderMainMenu(batch, font);
        }

        batch.setColor(Color.WHITE);
        renderer.end();
    }

    private void renderMainMenu(SpriteBatch batch, BitmapFont font) {
        // Draw title
        if (titleRegion != null) {
            float scale = 3f;
            float titleWidth = titleRegion.getRegionWidth() * scale;
            float titleHeight = titleRegion.getRegionHeight() * scale;
            float titleX = (PixelRenderer.VIRTUAL_WIDTH - titleWidth) / 2f;
            batch.draw(titleRegion, titleX, 160f, titleWidth, titleHeight);
        }

        // Draw menu options
        float startY = 130f;
        float lineHeight = 20f;
        for (int i = 0; i < MENU_OPTIONS.length; i++) {
            float y = startY - i * lineHeight;
            String prefix = (i == selectedIndex && cursorVisible) ? "> " : "  ";
            String label = prefix + (i + 1) + ". " + MENU_OPTIONS[i];

            if (i == 1 && !hasSaveFile) {
                // Continue is grayed out
                font.setColor(0.4f, 0.4f, 0.4f, 1f);
            } else if (i == selectedIndex) {
                font.setColor(ColorPalette.HOLY_GOLD);
            } else {
                font.setColor(ColorPalette.TEXT_WHITE);
            }

            font.draw(batch, label, 90f, y);
        }

        // Footer
        font.setColor(ColorPalette.HEAL_GREEN);
        font.draw(batch, "Arrow keys + Enter or 1-4", 70f, 30f);
        font.setColor(Color.WHITE);
    }

    private void renderStatsOverlay(SpriteBatch batch, BitmapFont font) {
        // Title
        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, "- STATISTICS -", 105f, 220f);

        float y = 195f;
        float lineH = 16f;

        font.setColor(ColorPalette.TEXT_WHITE);
        font.draw(batch, "Total Runs: " + metaProgression.getTotalRuns(), 40f, y);
        y -= lineH;
        font.draw(batch, "Victories: " + metaProgression.getTotalVictories(), 40f, y);
        y -= lineH;
        font.draw(batch, "Highest Wave: " + metaProgression.getHighestWave(), 40f, y);
        y -= lineH;
        font.draw(batch, "Enemies Killed: " + metaProgression.getTotalEnemiesKilled(), 40f, y);
        y -= lineH;
        font.draw(batch, "Damage Dealt: " + metaProgression.getTotalDamageDealt(), 40f, y);
        y -= lineH;
        font.draw(batch, "Damage Taken: " + metaProgression.getTotalDamageTaken(), 40f, y);
        y -= lineH;

        int fastest = metaProgression.getFastestVictoryTurns();
        String fastestStr = fastest < 0 ? "N/A" : String.valueOf(fastest);
        font.draw(batch, "Fastest Victory: " + fastestStr + " turns", 40f, y);
        y -= lineH;

        font.draw(batch, "Unlocks Earned: " + metaProgression.getActiveUnlocks().size() + "/6", 40f, y);

        font.setColor(ColorPalette.HEAL_GREEN);
        font.draw(batch, "Press ESC to return", 85f, 30f);
        font.setColor(Color.WHITE);
    }

    private void renderUnlocksOverlay(SpriteBatch batch, BitmapFont font) {
        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, "- UNLOCKABLES -", 100f, 225f);

        float y = 200f;
        float lineH = 28f;

        String[] allUnlocks = MetaProgression.getAllUnlockIds();
        for (String unlockId : allUnlocks) {
            boolean earned = metaProgression.hasUnlock(unlockId);
            String name = MetaProgression.getUnlockName(unlockId);
            String condition = MetaProgression.getUnlockCondition(unlockId);
            String reward = MetaProgression.getUnlockReward(unlockId);

            if (earned) {
                font.setColor(ColorPalette.HEAL_GREEN);
                font.draw(batch, "[*] " + name, 20f, y);
            } else {
                font.setColor(0.5f, 0.5f, 0.5f, 1f);
                font.draw(batch, "[ ] " + name, 20f, y);
            }

            font.setColor(earned ? ColorPalette.TEXT_WHITE : new Color(0.4f, 0.4f, 0.4f, 1f));
            font.draw(batch, "    " + condition + " -> " + reward, 20f, y - 10f);

            y -= lineH;
        }

        font.setColor(ColorPalette.HEAL_GREEN);
        font.draw(batch, "Press ESC to return", 85f, 15f);
        font.setColor(Color.WHITE);
    }

    private void drawScrollingBackground(SpriteBatch batch) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        int tileW = 16;
        int tileH = 16;
        float scrollOffset = backgroundScrollY % tileH;

        batch.setColor(0.3f, 0.3f, 0.4f, 0.4f);
        for (int x = 0; x < PixelRenderer.VIRTUAL_WIDTH; x += tileW) {
            for (int y = -tileH; y < PixelRenderer.VIRTUAL_HEIGHT + tileH; y += tileH) {
                batch.draw(bgTile, x, y + scrollOffset);
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
            float vy = MathUtils.random(5f, 15f);
            float life = MathUtils.random(1.0f, 2.5f);
            Color color = MathUtils.randomBoolean() ? ColorPalette.DARK_PURPLE : ColorPalette.FIRE_YELLOW;
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

        while (ambientParticles.size() > 60) {
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
        if (showingStats || showingUnlocks) {
            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.ENTER) {
                showingStats = false;
                showingUnlocks = false;
            }
            return true;
        }

        switch (keycode) {
            case Input.Keys.UP:
                selectedIndex--;
                if (selectedIndex < 0) selectedIndex = MENU_OPTIONS.length - 1;
                return true;

            case Input.Keys.DOWN:
                selectedIndex++;
                if (selectedIndex >= MENU_OPTIONS.length) selectedIndex = 0;
                return true;

            case Input.Keys.ENTER:
                selectOption(selectedIndex);
                return true;

            case Input.Keys.NUM_1:
                selectOption(0);
                return true;

            case Input.Keys.NUM_2:
                selectOption(1);
                return true;

            case Input.Keys.NUM_3:
                selectOption(2);
                return true;

            case Input.Keys.NUM_4:
                selectOption(3);
                return true;
        }

        return false;
    }

    private void selectOption(int index) {
        switch (index) {
            case 0: // New Game
                game.setScreen(new IntroScreen(game));
                break;

            case 1: // Continue
                if (hasSaveFile) {
                    loadAndContinue();
                }
                break;

            case 2: // Stats
                showingStats = true;
                break;

            case 3: // Unlocks
                showingUnlocks = true;
                break;
        }
    }

    private void loadAndContinue() {
        model.SaveData saveData = saveManager.loadRun();
        if (saveData == null) {
            hasSaveFile = false;
            return;
        }

        CombatEngineRestorer restorer = new CombatEngineRestorer();
        controller.CombatEngine engine = restorer.restoreFromSave(saveData, metaProgression);
        if (engine != null) {
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
    public void dispose() {
        if (titleTexture != null) {
            titleTexture.dispose();
        }
    }
}
