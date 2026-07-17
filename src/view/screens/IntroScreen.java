package view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import controller.CombatEngine;
import view.PlagueOfDanjinGame;
import view.assets.AssetLoader;
import view.rendering.PixelRenderer;

/**
 * Implements Screen. Shows game title 'Plague of Danjin'.
 * Character-by-character name entry using keyboard capture.
 * Story intro with typewriter text (delta-time based, NOT Thread.sleep).
 * 'Press Enter to begin' prompt. Creates CombatEngine and calls startGame(name) on confirmation.
 */
public class IntroScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;

    private static final String TITLE = "Plague of Danjin";
    private static final String STORY_TEXT =
            "A dark plague has spread across the land of Danjin. " +
            "As the last warrior standing, you must fight through " +
            "20 waves of corrupted creatures to reach the source " +
            "of the plague and end it forever.";
    private static final float TYPEWRITER_SPEED = 30f; // characters per second
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
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1f);

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
        if (blinkTimer >= 0.5f) {
            cursorVisible = !cursorVisible;
            blinkTimer = 0f;
        }

        renderer.begin();
        BitmapFont font = assets.getFont();
        SpriteBatch batch = renderer.getBatch();

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

        renderer.end();
    }

    private void renderTitle(BitmapFont font, SpriteBatch batch) {
        font.setColor(Color.GOLD);
        font.draw(batch, TITLE, 80f, 160f);

        font.setColor(Color.WHITE);
        font.draw(batch, "Press Enter to begin", 90f, 100f);
    }

    private void renderNameEntry(BitmapFont font, SpriteBatch batch) {
        font.setColor(Color.GOLD);
        font.draw(batch, TITLE, 80f, 200f);

        font.setColor(Color.WHITE);
        font.draw(batch, "Enter your name:", 100f, 140f);

        font.setColor(Color.CYAN);
        String nameDisplay = playerName.toString();
        if (cursorVisible) {
            nameDisplay += "_";
        }
        font.draw(batch, nameDisplay, 100f, 120f);

        font.setColor(Color.GRAY);
        font.draw(batch, "(Press Enter to confirm)", 80f, 80f);
    }

    private void renderStory(BitmapFont font, SpriteBatch batch) {
        font.setColor(Color.GOLD);
        font.draw(batch, TITLE, 80f, 220f);

        font.setColor(Color.CYAN);
        font.draw(batch, "Hero: " + playerName.toString(), 10f, 200f);

        // Typewriter text
        font.setColor(Color.WHITE);
        String displayText = STORY_TEXT.substring(0, typedCharCount);
        font.draw(batch, displayText, 10f, 170f, 300f, -1, true);
    }

    private void renderStartPrompt(BitmapFont font, SpriteBatch batch) {
        if (cursorVisible) {
            font.setColor(Color.GREEN);
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
                // Skip to end of typewriter
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
