package view;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * LWJGL3 entry point with Lwjgl3ApplicationConfiguration:
 * window 960x720, title 'Plague of Danjin', resizable true, vsync true.
 * Sets up nearest-neighbor filtering.
 */
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Plague of Danjin");
        config.setWindowedMode(960, 720);
        config.useVsync(true);
        config.setResizable(true);

        // Set to nearest-neighbor filtering for pixel art
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);

        new Lwjgl3Application(new PlagueOfDanjinGame(), config);
    }
}
