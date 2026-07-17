package view.assets;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

/**
 * Centralized asset management. Creates placeholder colored Pixmap textures
 * (green rect player, red rect enemies). Generates BitmapFont at pixel-appropriate size.
 * Uses ShapeRenderer for placeholder graphics. Designed so Phase 4 just swaps in real textures.
 */
public class AssetLoader implements Disposable {
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private final Map<String, Texture> placeholderTextures;

    public AssetLoader() {
        this.placeholderTextures = new HashMap<>();
    }

    /**
     * Loads all placeholder assets. Must be called after libGDX is initialized.
     */
    public void load() {
        // Create BitmapFont at pixel-appropriate size
        font = new BitmapFont();
        font.getData().setScale(0.5f); // Scale down for 320x240 resolution
        font.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        // Create shape renderer for drawing placeholder graphics
        shapeRenderer = new ShapeRenderer();

        // Create placeholder textures using Pixmaps
        createPlaceholderTexture("player", Color.GREEN, 24, 32);
        createPlaceholderTexture("enemy", Color.RED, 28, 32);
        createPlaceholderTexture("enemy_boss", Color.PURPLE, 40, 48);
        createPlaceholderTexture("projectile", Color.YELLOW, 8, 8);
    }

    private void createPlaceholderTexture(String key, Color color, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        // Add a darker border
        pixmap.setColor(color.cpy().mul(0.6f));
        pixmap.drawRectangle(0, 0, width, height);

        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        pixmap.dispose();

        placeholderTextures.put(key, texture);
    }

    /**
     * Returns the shared BitmapFont for pixel text rendering.
     */
    public BitmapFont getFont() {
        return font;
    }

    /**
     * Returns a placeholder texture by key. Returns the enemy texture as fallback.
     */
    public Texture getPlaceholderTexture(String key) {
        Texture tex = placeholderTextures.get(key);
        if (tex == null) {
            tex = placeholderTextures.get("enemy");
        }
        return tex;
    }

    /**
     * Returns the ShapeRenderer for drawing placeholder shapes.
     */
    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        for (Texture texture : placeholderTextures.values()) {
            texture.dispose();
        }
        placeholderTextures.clear();
    }
}
