package view.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Floating damage/heal number that rises upward, decelerates, and fades out.
 * Color-coded: red for damage, green for heals, yellow for crits.
 */
public class DamageNumber {
    private static final float INITIAL_VELOCITY_Y = 40f;
    private static final float DECELERATION = 60f;
    private static final float MAX_LIFETIME = 1.0f;

    private float x;
    private float y;
    private float velocityY;
    private float lifetime;
    private float alpha;
    private final String text;
    private final Color color;

    /**
     * Creates a new damage number display.
     *
     * @param text  the text to display (e.g., "42", "CRITICAL!", "HEAL")
     * @param x     starting x position
     * @param y     starting y position
     * @param color the text color (red for damage, green for heal, yellow for crit)
     */
    public DamageNumber(String text, float x, float y, Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = new Color(color);
        this.velocityY = INITIAL_VELOCITY_Y;
        this.lifetime = MAX_LIFETIME;
        this.alpha = 1f;
    }

    /**
     * Updates the damage number position and fade.
     */
    public void update(float delta) {
        // Move upward, decelerating over time
        y += velocityY * delta;
        velocityY = Math.max(0f, velocityY - DECELERATION * delta);

        // Fade out over lifetime
        lifetime -= delta;
        alpha = Math.max(0f, lifetime / MAX_LIFETIME);
    }

    /**
     * Returns true if the damage number is still visible.
     */
    public boolean isAlive() {
        return lifetime > 0f;
    }

    /**
     * Renders the damage number text at its current position with fading.
     */
    public void render(SpriteBatch batch, BitmapFont font) {
        if (!isAlive()) return;
        font.setColor(color.r, color.g, color.b, alpha);
        font.draw(batch, text, x, y);
    }
}
