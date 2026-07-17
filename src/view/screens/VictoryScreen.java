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
 * Implements Screen. Renders victory text as pixel text.
 * Shows credits scroll. Provides restart option.
 */
public class VictoryScreen extends InputAdapter implements Screen {
    private final PlagueOfDanjinGame game;
    private final PixelRenderer renderer;
    private final AssetLoader assets;
    private final CombatEngine engine;

    private float creditsScrollY;
    private static final float SCROLL_SPEED = 20f;
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
            "Thank you for playing!",
            "",
            "",
            "Press Enter to play again"
    };

    public VictoryScreen(PlagueOfDanjinGame game, CombatEngine engine) {
        this.game = game;
        this.renderer = game.getRenderer();
        this.assets = game.getAssetLoader();
        this.engine = engine;
        this.creditsScrollY = 0f;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.0f, 0.05f, 0.0f, 1f);

        creditsScrollY += delta * SCROLL_SPEED;

        renderer.begin();
        BitmapFont font = assets.getFont();
        SpriteBatch batch = renderer.getBatch();

        float startY = 20f + creditsScrollY;
        float lineHeight = 12f;

        for (int i = 0; i < CREDITS.length; i++) {
            float yPos = startY + (CREDITS.length - i) * lineHeight;

            if (yPos < -20f || yPos > 260f) continue;

            if (i == 0) {
                font.setColor(Color.GOLD);
            } else if (CREDITS[i].startsWith("---")) {
                font.setColor(Color.CYAN);
            } else if (CREDITS[i].startsWith("Press")) {
                font.setColor(Color.GREEN);
            } else {
                font.setColor(Color.WHITE);
            }

            font.draw(batch, CREDITS[i], 40f, yPos);
        }

        font.setColor(Color.WHITE);
        renderer.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER) {
            game.setScreen(new IntroScreen(game));
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
    public void dispose() {}
}
