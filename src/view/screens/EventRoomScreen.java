package view.screens;

import java.util.List;

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
 * Implements Screen. Shows narrative text from EventRoomManager.getEventRoomDescription()
 * with typewriter effect. Displays 2-3 choice buttons from getChoices().
 * Player selects via click or number keys. Calls engine.processEventRoomChoice(index).
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
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.02f, 0.08f, 1f);

        // Update typewriter
        if (!typewriterDone && roomDescription != null) {
            typewriterTimer += delta * TYPEWRITER_SPEED;
            typedCharCount = Math.min((int) typewriterTimer, roomDescription.length());
            if (typedCharCount >= roomDescription.length()) {
                typewriterDone = true;
            }
        }

        renderer.begin();
        BitmapFont font = assets.getFont();
        SpriteBatch batch = renderer.getBatch();

        // Title
        font.setColor(Color.PURPLE);
        font.draw(batch, "~ Event Room ~", 110f, 230f);

        // Room description with typewriter
        font.setColor(Color.WHITE);
        if (roomDescription != null) {
            String displayText = roomDescription.substring(0, typedCharCount);
            font.draw(batch, displayText, 20f, 200f, 280f, -1, true);
        }

        // Choices (only shown after typewriter completes)
        if (typewriterDone && choices != null) {
            float choiceY = 140f;
            float lineHeight = 25f;

            for (int i = 0; i < choices.size(); i++) {
                float yPos = choiceY - i * lineHeight;
                boolean isSelected = (i == selectedIndex);

                font.setColor(isSelected ? Color.YELLOW : Color.WHITE);
                String prefix = isSelected ? "> " : "  ";
                font.draw(batch, prefix + (i + 1) + ". " + choices.get(i), 10f, yPos, 300f, -1, true);
            }

            font.setColor(Color.GRAY);
            font.draw(batch, "Press 1-" + choices.size() + " or Enter", 90f, 20f);
        } else if (!typewriterDone) {
            font.setColor(Color.GRAY);
            font.draw(batch, "(Press Enter to skip)", 90f, 20f);
        }

        font.setColor(Color.WHITE);
        renderer.end();
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

        // Simple click region detection
        float normalizedY = 1f - (float) screenY / Gdx.graphics.getHeight();
        float choiceStartY = 140f / 240f;
        float lineHeight = 25f / 240f;

        for (int i = 0; i < choices.size(); i++) {
            float optionY = choiceStartY - i * lineHeight;
            if (normalizedY >= optionY - lineHeight / 2f && normalizedY <= optionY + lineHeight / 2f) {
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
