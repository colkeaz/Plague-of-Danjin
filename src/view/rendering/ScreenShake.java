package view.rendering;

import com.badlogic.gdx.math.MathUtils;

/**
 * Camera shake effect with configurable intensity and duration (~0.2s).
 * Uses lerp for smooth return to center. All calculations delta-time based.
 */
public class ScreenShake {
    private static final float DEFAULT_DURATION = 0.2f;
    private static final float LERP_SPEED = 10f;

    private float intensity;
    private float duration;
    private float elapsed;
    private float offsetX;
    private float offsetY;
    private boolean active;

    public ScreenShake() {
        this.active = false;
        this.offsetX = 0f;
        this.offsetY = 0f;
    }

    /**
     * Triggers a camera shake with the given intensity.
     * Duration defaults to ~0.2 seconds.
     */
    public void trigger(float intensity) {
        this.intensity = intensity;
        this.duration = DEFAULT_DURATION;
        this.elapsed = 0f;
        this.active = true;
    }

    /**
     * Updates the shake effect each frame. Uses delta-time for consistency.
     */
    public void update(float delta) {
        if (!active) {
            // Lerp back to zero when inactive
            offsetX = MathUtils.lerp(offsetX, 0f, LERP_SPEED * delta);
            offsetY = MathUtils.lerp(offsetY, 0f, LERP_SPEED * delta);
            return;
        }

        elapsed += delta;
        if (elapsed >= duration) {
            active = false;
            // Lerp will bring offsets back to zero over subsequent frames
            return;
        }

        // Calculate remaining intensity with falloff
        float progress = elapsed / duration;
        float currentIntensity = intensity * (1f - progress);

        // Random offset within intensity bounds
        offsetX = MathUtils.random(-currentIntensity, currentIntensity);
        offsetY = MathUtils.random(-currentIntensity, currentIntensity);
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public boolean isActive() {
        return active;
    }
}
