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
 * Implements Screen. Shows pixel-art title logo, animated scrolling background,
 * character-by-character name entry with blinking pixel cursor.
 * Story intro with typewriter text. Creates CombatEngine and calls startGame(name).
 */
public class IntroScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;

    private static final String STORY_TEXT =
            "A dark plague has spread across the land of Danjin. " +
            "As the last warrior standing, you must fight through " +
            "20 waves of corrupted creatures to reach the source " +
            "of the plague and end it forever.";
    private static final float TYPEWRITER_SPEED = 30f;
    private static final int MAX_NAME_LENGTH = 16;

    private enum IntroState {
        SHOWING_TITLE,
        ENTERING_NAME,
        SHOWING_STORY,
        READY_TO_START
    }

    private IntroState state;
    private StringBuilder playerName;
    private float typewriterTimer;
    private int typedCharCount;
    private float blinkTimer;
    private boolean cursorVisible;

    // Pixel art title logo
    private Texture titleTexture;
    private TextureRegion titleRegion;

    // Animated background
    private float backgroundScrollY;
    private static final float BG_SCROLL_SPEED = 8f;

    // Ambient particles
    private final List<Particle> ambientParticles;
    private float particleSpawnTimer;

    public IntroScreen(PlagueOfDanjinGame game) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.state = IntroState.SHOWING_TITLE;
        this.playerName = new StringBuilder();
        this.typewriterTimer = 0f;
        this.typedCharCount = 0;
        this.blinkTimer = 0f;
        this.cursorVisible = true;
        this.backgroundScrollY = 0f;
        this.ambientParticles = new ArrayList<>();
        this.particleSpawnTimer = 0f;

        generateTitleLogo();
    }

    /**
     * Generates a pixel-art title logo "PLAGUE OF DANJIN" as a Pixmap.
     * Each letter is drawn in a chunky 5x7 pixel font style.
     */
    private void generateTitleLogo() {
        // Each character is 5 wide + 1 spacing = 6 pixels per char
        // "PLAGUE OF DANJIN" = 16 chars -> 16*6 - 1 = 95 pixels wide
        // We use two lines: "PLAGUE OF" and "DANJIN" for better layout
        String line1 = "PLAGUE OF";
        String line2 = "DANJIN";
        int charWidth = 6; // 5px char + 1px spacing
        int charHeight = 7;
        int lineSpacing = 3;
        int totalWidth = Math.max(line1.length(), line2.length()) * charWidth;
        int totalHeight = charHeight * 2 + lineSpacing;

        Pixmap pm = new Pixmap(totalWidth, totalHeight, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();

        // Draw line 1 centered
        int line1Width = line1.length() * charWidth;
        int line1Offset = (totalWidth - line1Width) / 2;
        drawPixelText(pm, line1, line1Offset, 0, ColorPalette.HOLY_GOLD);

        // Draw line 2 centered
        int line2Width = line2.length() * charWidth;
        int line2Offset = (totalWidth - line2Width) / 2;
        drawPixelText(pm, line2, line2Offset, charHeight + lineSpacing, ColorPalette.FIRE_ORANGE);

        titleTexture = new Texture(pm);
        titleTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        titleRegion = new TextureRegion(titleTexture);
        pm.dispose();
    }

    /**
     * Draws text using a chunky 5x7 pixel font.
     */
    private void drawPixelText(Pixmap pm, String text, int startX, int startY, Color color) {
        pm.setColor(color);
        for (int i = 0; i < text.length(); i++) {
            int cx = startX + i * 6;
            drawPixelChar(pm, text.charAt(i), cx, startY);
        }
    }

    /**
     * Draws a single character in a 5x7 pixel grid.
     */
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

    /**
     * Returns 5-bit wide, 7-row pattern for blocky pixel font characters.
     */
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
            case ' ': return null; // space - no pixels
            default: return null;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(ColorPalette.BACKGROUND.r, ColorPalette.BACKGROUND.g,
                ColorPalette.BACKGROUND.b, 1f);

        // Update background scroll
        backgroundScrollY += delta * BG_SCROLL_SPEED;

        // Update typewriter effect
        if (state == IntroState.SHOWING_STORY) {
            typewriterTimer += delta * TYPEWRITER_SPEED;
            typedCharCount = Math.min((int) typewriterTimer, STORY_TEXT.length());
            if (typedCharCount >= STORY_TEXT.length()) {
                state = IntroState.READY_TO_START;
            }
        }

        // Update cursor blink
        blinkTimer += delta;
        if (blinkTimer >= 0.4f) {
            cursorVisible = !cursorVisible;
            blinkTimer = 0f;
        }

        // Update ambient particles
        updateAmbientParticles(delta);

        renderer.begin();
        SpriteBatch batch = renderer.getBatch();
        BitmapFont font = assets.getFont();

        // Draw scrolling background tiles
        drawScrollingBackground(batch);

        // Draw ambient particles
        drawAmbientParticles(batch);

        // Draw content based on state
        switch (state) {
            case SHOWING_TITLE:
                renderTitle(font, batch);
                break;
            case ENTERING_NAME:
                renderNameEntry(font, batch);
                break;
            case SHOWING_STORY:
                renderStory(font, batch);
                break;
            case READY_TO_START:
                renderStory(font, batch);
                renderStartPrompt(font, batch);
                break;
        }

        batch.setColor(Color.WHITE);
        renderer.end();
    }

    private void drawScrollingBackground(SpriteBatch batch) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        int tileW = 16;
        int tileH = 16;
        float scrollOffset = backgroundScrollY % tileH;

        // Draw a grid of background tiles with a dark tint
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

        // Cap particle count
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

    private void renderTitle(BitmapFont font, SpriteBatch batch) {
        // Draw pixel art title logo centered
        if (titleRegion != null) {
            float scale = 3f;
            float titleWidth = titleRegion.getRegionWidth() * scale;
            float titleHeight = titleRegion.getRegionHeight() * scale;
            float titleX = (PixelRenderer.VIRTUAL_WIDTH - titleWidth) / 2f;
            float titleY = 130f;
            batch.draw(titleRegion, titleX, titleY, titleWidth, titleHeight);
        }

        // Blinking prompt
        if (cursorVisible) {
            font.setColor(ColorPalette.HEAL_GREEN);
            font.draw(batch, "Press Enter to begin", 90f, 100f);
        }
    }

    private void renderNameEntry(BitmapFont font, SpriteBatch batch) {
        // Draw pixel art title logo smaller
        if (titleRegion != null) {
            float scale = 2f;
            float titleWidth = titleRegion.getRegionWidth() * scale;
            float titleHeight = titleRegion.getRegionHeight() * scale;
            float titleX = (PixelRenderer.VIRTUAL_WIDTH - titleWidth) / 2f;
            batch.draw(titleRegion, titleX, 190f, titleWidth, titleHeight);
        }

        font.setColor(ColorPalette.TEXT_WHITE);
        font.draw(batch, "Enter your name:", 100f, 150f);

        // Name with blinking pixel cursor
        font.setColor(ColorPalette.HOLY_GOLD);
        String nameDisplay = playerName.toString();
        font.draw(batch, nameDisplay, 100f, 130f);

        // Blinking block cursor
        if (cursorVisible) {
            float cursorX = 100f + nameDisplay.length() * 5f;
            TextureRegion particleTex = assets.getParticleTexture("physical");
            if (particleTex != null) {
                batch.setColor(ColorPalette.TEXT_WHITE);
                batch.draw(particleTex, cursorX, 122f, 5f, 8f);
                batch.setColor(Color.WHITE);
            }
        }

        font.setColor(ColorPalette.HEAL_GREEN);
        font.draw(batch, "(Press Enter to confirm)", 80f, 90f);
    }

    private void renderStory(BitmapFont font, SpriteBatch batch) {
        // Small title
        if (titleRegion != null) {
            float scale = 2f;
            float titleWidth = titleRegion.getRegionWidth() * scale;
            float titleHeight = titleRegion.getRegionHeight() * scale;
            float titleX = (PixelRenderer.VIRTUAL_WIDTH - titleWidth) / 2f;
            batch.draw(titleRegion, titleX, 200f, titleWidth, titleHeight);
        }

        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, "Hero: " + playerName.toString(), 10f, 195f);

        // Typewriter text
        font.setColor(ColorPalette.TEXT_WHITE);
        String displayText = STORY_TEXT.substring(0, typedCharCount);
        font.draw(batch, displayText, 10f, 170f, 300f, -1, true);
    }

    private void renderStartPrompt(BitmapFont font, SpriteBatch batch) {
        if (cursorVisible) {
            font.setColor(ColorPalette.HEAL_GREEN);
            font.draw(batch, "Press Enter to fight!", 90f, 40f);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (state) {
            case SHOWING_TITLE:
                if (keycode == Input.Keys.ENTER) {
                    state = IntroState.ENTERING_NAME;
                }
                break;

            case ENTERING_NAME:
                if (keycode == Input.Keys.ENTER && playerName.length() > 0) {
                    state = IntroState.SHOWING_STORY;
                    typewriterTimer = 0f;
                    typedCharCount = 0;
                } else if (keycode == Input.Keys.BACKSPACE && playerName.length() > 0) {
                    playerName.deleteCharAt(playerName.length() - 1);
                }
                break;

            case SHOWING_STORY:
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    typedCharCount = STORY_TEXT.length();
                    state = IntroState.READY_TO_START;
                }
                break;

            case READY_TO_START:
                if (keycode == Input.Keys.ENTER) {
                    startGame();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        if (state == IntroState.ENTERING_NAME) {
            if (Character.isLetterOrDigit(character) || character == ' ') {
                if (playerName.length() < MAX_NAME_LENGTH) {
                    playerName.append(character);
                }
            }
        }
        return true;
    }

    private void startGame() {
        CombatEngine engine = new CombatEngine();
        engine.startGame(playerName.toString());

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
    public void dispose() {
        if (titleTexture != null) {
            titleTexture.dispose();
        }
    }
}
