package view.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Wraps SpriteBatch + OrthographicCamera at 320x240 internal resolution.
 * Uses FitViewport for pixel-perfect scaling. Provides begin()/end() methods.
 * Uses TextureFilter.Nearest everywhere.
 */
public class PixelRenderer implements Disposable {
    public static final int VIRTUAL_WIDTH = 320;
    public static final int VIRTUAL_HEIGHT = 240;

    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final FitViewport viewport;

    public PixelRenderer() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
    }

    public void begin() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
    }

    public void end() {
        batch.end();
    }

    /**
     * Handles viewport resize to maintain pixel-perfect scaling.
     */
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    /**
     * Applies camera offset (used for screen shake effects).
     */
    public void applyCameraOffset(float offsetX, float offsetY) {
        camera.position.set(VIRTUAL_WIDTH / 2f + offsetX, VIRTUAL_HEIGHT / 2f + offsetY, 0);
    }

    /**
     * Resets camera to default center position.
     */
    public void resetCamera() {
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
