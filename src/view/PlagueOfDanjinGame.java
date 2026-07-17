package view;

import com.badlogic.gdx.Game;

import view.assets.AssetLoader;
import view.audio.MusicManager;
import view.audio.SFXManager;
import view.rendering.PixelRenderer;
import view.screens.IntroScreen;

/**
 * Main game class extending com.badlogic.gdx.Game (not ApplicationAdapter).
 * Manages screen transitions. On create(), initializes AssetLoader,
 * audio managers, and sets IntroScreen as first screen. dispose() cleans up assets.
 */
public class PlagueOfDanjinGame extends Game {
    private AssetLoader assetLoader;
    private PixelRenderer renderer;
    private SFXManager sfxManager;
    private MusicManager musicManager;

    @Override
    public void create() {
        renderer = new PixelRenderer();
        assetLoader = new AssetLoader();
        assetLoader.load();

        // Initialize audio managers
        sfxManager = new SFXManager();
        musicManager = new MusicManager();

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

    /**
     * Returns the shared SFXManager instance.
     */
    public SFXManager getSfxManager() {
        return sfxManager;
    }

    /**
     * Returns the shared MusicManager instance.
     */
    public MusicManager getMusicManager() {
        return musicManager;
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
        if (sfxManager != null) {
            sfxManager.dispose();
        }
        if (musicManager != null) {
            musicManager.dispose();
        }
    }
}
