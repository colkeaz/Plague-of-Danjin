package view;

import com.badlogic.gdx.Game;

import view.assets.AssetLoader;
import view.rendering.PixelRenderer;
import view.screens.IntroScreen;

/**
 * Main game class extending com.badlogic.gdx.Game (not ApplicationAdapter).
 * Manages screen transitions. On create(), initializes AssetLoader,
 * sets IntroScreen as first screen. dispose() cleans up assets.
 */
public class PlagueOfDanjinGame extends Game {
    private AssetLoader assetLoader;
    private PixelRenderer renderer;

    @Override
    public void create() {
        renderer = new PixelRenderer();
        assetLoader = new AssetLoader();
        assetLoader.load();

        setScreen(new IntroScreen(this));
    }

    /**
     * Returns the shared PixelRenderer instance.
     */
    public PixelRenderer getRenderer() {
        return renderer;
    }

    /**
     * Returns the shared AssetLoader instance.
     */
    public AssetLoader getAssetLoader() {
        return assetLoader;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (assetLoader != null) {
            assetLoader.dispose();
        }
        if (renderer != null) {
            renderer.dispose();
        }
    }
}
