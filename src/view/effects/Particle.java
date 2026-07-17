package view.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A small pixel particle with position, velocity, color, lifetime, and size.
 * Updated per frame with gravity/drift and fades out as lifetime expires.
 */
public class Particle {
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float lifetime;
    private final float maxLifetime;
    private final int size;
    private final Color color;
    private float gravity;

    /**
     * Creates a new particle.
     *
     * @param x         starting x position
     * @param y         starting y position
     * @param velocityX horizontal velocity in pixels per second
     * @param velocityY vertical velocity in pixels per second
     * @param lifetime  total lifetime in seconds
     * @param size      size in pixels (1-3)
     * @param color     the particle color
     */
    public Particle(float x, float y, float velocityX, float velocityY,
                    float lifetime, int size, Color color) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
        this.size = Math.max(1, Math.min(3, size));
        this.color = new Color(color);
        this.gravity = 0f;
    }

    /**
     * Sets gravity applied to vertical velocity each frame (negative = downward).
     */
    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    /**
     * Updates the particle position and lifetime.
     */
    public void update(float delta) {
        velocityY += gravity * delta;
        x += velocityX * delta;
        y += velocityY * delta;
        lifetime -= delta;
    }

    /**
     * Returns true if the particle is still alive.
     */
    public boolean isAlive() {
        return lifetime > 0f;
    }

    /**
     * Returns the current alpha based on remaining lifetime (fades as lifetime expires).
     */
    public float getAlpha() {
        if (maxLifetime <= 0f) return 0f;
        return Math.max(0f, lifetime / maxLifetime);
    }

    /**
     * Renders the particle at its current position with color and alpha.
     */
    public void render(SpriteBatch batch, TextureRegion tex) {
        if (!isAlive()) return;
        float alpha = getAlpha();
        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(tex, x, y, size, size);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
