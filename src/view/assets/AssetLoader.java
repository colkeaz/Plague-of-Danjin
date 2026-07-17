package view.assets;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import model.skills.Element;
import model.status.StatusType;
import view.sprites.AnimationState;
import view.sprites.SpriteGenerator;

/**
 * Centralized asset management. Uses SpriteGenerator to create all sprites
 * programmatically at startup. Provides typed accessor methods for entity frames,
 * UI sprites, status icons, element icons, and particle textures.
 * Retains BitmapFont and ShapeRenderer for rendering.
 */
public class AssetLoader implements Disposable {
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private SpriteGenerator spriteGenerator;

    // Entity animation frames indexed by "entityName_stateName"
    private final Map<String, TextureRegion[]> entityFrameCache;

    public AssetLoader() {
        this.entityFrameCache = new HashMap<>();
    }

    /**
     * Loads all assets. Must be called after libGDX is initialized.
     * Generates all sprites via SpriteGenerator and caches them for fast access.
     */
    public void load() {
        // Create BitmapFont at pixel-appropriate size
        font = new BitmapFont();
        font.getData().setScale(0.5f); // Scale down for 320x240 resolution
        font.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        // Create shape renderer for drawing shapes (HP bars, etc.)
        shapeRenderer = new ShapeRenderer();

        // Generate all sprites
        spriteGenerator = new SpriteGenerator();
        spriteGenerator.generateAll();

        // Cache entity animation frames for fast lookup
        cacheEntityFrames();
    }

    /**
     * Pre-caches all entity animation frames in a flat map for fast access.
     */
    private void cacheEntityFrames() {
        String[] entityNames = {
            "player", "goblin", "plague_goblin", "skeleton",
            "shielded_skeleton", "goblin_king", "goblin_chieftain",
            "bone_colossus", "lich"
        };

        for (String entityName : entityNames) {
            Map<AnimationState, TextureRegion[]> frames = spriteGenerator.getAnimationFrames(entityName);
            if (frames != null) {
                for (Map.Entry<AnimationState, TextureRegion[]> entry : frames.entrySet()) {
                    String key = entityName + "_" + entry.getKey().name().toLowerCase();
                    entityFrameCache.put(key, entry.getValue());
                }
            }
        }
    }

    // --- Entity frame accessors ---

    /**
     * Returns the animation frames for an entity in a given animation state.
     *
     * @param entityName     the entity name (e.g., "player", "goblin", "lich")
     * @param state          the animation state (IDLE, ATTACKING, HURT, DYING, CASTING)
     * @return array of TextureRegions for the animation frames, or null if not found
     */
    public TextureRegion[] getEntityFrames(String entityName, AnimationState state) {
        String key = entityName + "_" + state.name().toLowerCase();
        return entityFrameCache.get(key);
    }

    // --- UI sprite accessors ---

    /**
     * Returns the status effect icon for the given status type.
     */
    public TextureRegion getStatusIcon(StatusType type) {
        if (type == null) return null;
        return spriteGenerator.getUiSprite("status_" + type.name());
    }

    /**
     * Returns the element icon for the given element.
     */
    public TextureRegion getElementIcon(Element element) {
        if (element == null) return null;
        return spriteGenerator.getUiSprite("element_" + element.name());
    }

    /**
     * Returns the menu frame border texture.
     */
    public TextureRegion getMenuFrame() {
        return spriteGenerator.getUiSprite("menu_frame");
    }

    /**
     * Returns the HP bar frame texture.
     */
    public TextureRegion getHpBarFrame() {
        return spriteGenerator.getUiSprite("hp_bar_frame");
    }

    /**
     * Returns the MP bar frame texture.
     */
    public TextureRegion getMpBarFrame() {
        return spriteGenerator.getUiSprite("mp_bar_frame");
    }

    /**
     * Returns the animated chest frames (3 frames: closed, opening, open).
     */
    public TextureRegion[] getChestFrames() {
        return spriteGenerator.getUiAnimatedSprite("chest");
    }

    /**
     * Returns the wave banner texture.
     */
    public TextureRegion getWaveBanner() {
        return spriteGenerator.getUiSprite("wave_banner");
    }

    /**
     * Returns the background tile texture for the dungeon floor.
     */
    public TextureRegion getBackgroundTile() {
        return spriteGenerator.getUiSprite("background_tile");
    }

    /**
     * Returns a particle effect texture by type.
     *
     * @param type the particle type (e.g., "fire", "holy", "dark", "poison", "physical")
     */
    public TextureRegion getParticleTexture(String type) {
        return spriteGenerator.getUiSprite("particle_" + type);
    }

    // --- Shared rendering utilities ---

    /**
     * Returns the shared BitmapFont for pixel text rendering.
     */
    public BitmapFont getFont() {
        return font;
    }

    /**
     * Returns the ShapeRenderer for drawing geometric shapes.
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
        if (spriteGenerator != null) {
            spriteGenerator.dispose();
        }
    }
}
