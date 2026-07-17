package view.ui;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import model.Enemy;
import model.status.StatusEffect;
import view.assets.AssetLoader;
import view.sprites.AnimationState;
import view.sprites.SpriteAnimator;

/**
 * Renders the enemy sprite with animation, textured HP bar, status icons,
 * telegraph pulsing warning, and death/attack/hurt animations.
 * Enemy sprite is centered horizontally in the enemy area, scaled 2-3x from base sprite size.
 */
public class EnemyDisplay {
    private static final float HP_BAR_WIDTH = 80f;
    private static final float HP_BAR_HEIGHT = 6f;
    private static final Color HP_BAR_BG = new Color(0.2f, 0.2f, 0.2f, 1f);
    private static final Color HP_BAR_FG = Color.RED;
    private static final Color TELEGRAPH_COLOR = Color.YELLOW;
    private static final float SPRITE_SCALE = 3f;

    private boolean damageFlash;
    private float damageFlashTimer;
    private static final float DAMAGE_FLASH_DURATION = 0.15f;

    // Telegraph pulse
    private float telegraphPulseTimer;

    // SpriteAnimator for the current enemy
    private SpriteAnimator animator;
    private String currentEntityName;

    public EnemyDisplay() {
        this.damageFlash = false;
        this.damageFlashTimer = 0f;
        this.telegraphPulseTimer = 0f;
        this.animator = null;
        this.currentEntityName = null;
    }

    /**
     * Sets the current enemy and initializes the SpriteAnimator for it.
     */
    public void setEnemy(Enemy enemy, AssetLoader assets) {
        if (enemy == null) {
            animator = null;
            currentEntityName = null;
            return;
        }
        String entityName = getEntityName(enemy);
        if (!entityName.equals(currentEntityName)) {
            currentEntityName = entityName;
            initAnimator(assets);
        }
    }

    /**
     * Sets the enemy name directly for switching enemies between waves.
     */
    public void setEnemyName(String entityName, AssetLoader assets) {
        if (entityName == null) {
            animator = null;
            currentEntityName = null;
            return;
        }
        if (!entityName.equals(currentEntityName)) {
            currentEntityName = entityName;
            initAnimator(assets);
        }
    }

    private void initAnimator(AssetLoader assets) {
        Map<AnimationState, TextureRegion[]> framesMap = new EnumMap<>(AnimationState.class);
        for (AnimationState state : AnimationState.values()) {
            TextureRegion[] frames = assets.getEntityFrames(currentEntityName, state);
            if (frames != null && frames.length > 0) {
                framesMap.put(state, frames);
            }
        }
        if (!framesMap.isEmpty()) {
            animator = new SpriteAnimator(framesMap);
        } else {
            animator = null;
        }
    }

    /**
     * Triggers a damage flash effect and transitions animator to HURT state.
     */
    public void triggerDamageFlash() {
        damageFlash = true;
        damageFlashTimer = DAMAGE_FLASH_DURATION;
        if (animator != null) {
            animator.setState(AnimationState.HURT);
        }
    }

    /**
     * Triggers the attack animation on the enemy.
     */
    public void triggerAttackAnimation() {
        if (animator != null) {
            animator.setState(AnimationState.ATTACKING);
        }
    }

    /**
     * Triggers the death animation on the enemy.
     */
    public void triggerDeathAnimation() {
        if (animator != null) {
            animator.setState(AnimationState.DYING);
        }
    }

    /**
     * Updates animations.
     */
    public void update(float delta) {
        if (damageFlash) {
            damageFlashTimer -= delta;
            if (damageFlashTimer <= 0f) {
                damageFlash = false;
                damageFlashTimer = 0f;
            }
        }
        telegraphPulseTimer += delta;
        if (animator != null) {
            animator.update(delta);
        }
    }

    /**
     * Renders the enemy display at the given position.
     * Position (x, y) is the bottom-left of the enemy area.
     */
    public void render(SpriteBatch batch, BitmapFont font, AssetLoader assets, Enemy enemy, float x, float y) {
        if (enemy == null) return;

        // Ensure animator is set for current enemy
        setEnemy(enemy, assets);

        // Get current frame from animator or fallback to idle frame 0
        TextureRegion frame = null;
        if (animator != null) {
            frame = animator.getCurrentFrame();
        }
        if (frame == null) {
            TextureRegion[] idleFrames = assets.getEntityFrames(getEntityName(enemy), AnimationState.IDLE);
            if (idleFrames != null && idleFrames.length > 0) {
                frame = idleFrames[0];
            }
        }

        // Render enemy sprite centered horizontally, scaled
        if (frame != null) {
            float scaledWidth = frame.getRegionWidth() * SPRITE_SCALE;
            float scaledHeight = frame.getRegionHeight() * SPRITE_SCALE;
            float spriteX = x + (HP_BAR_WIDTH - scaledWidth) / 2f;
            float spriteY = y + 20f;

            // Tint red during damage flash for visual feedback
            if (damageFlash) {
                batch.setColor(Color.RED);
            } else {
                batch.setColor(Color.WHITE);
            }
            batch.draw(frame, spriteX, spriteY, scaledWidth, scaledHeight);
            batch.setColor(Color.WHITE);
        }

        // Render textured HP bar frame above enemy
        renderTexturedHPBar(batch, font, assets, enemy, x, y);

        // Render status effect icons below HP bar
        renderStatusIcons(batch, assets, enemy, x, y - 2f);

        // Render telegraph warning: pulsing exclamation when winding up
        if (enemy.isWindingUp() && enemy.getNextAbility() != null) {
            renderTelegraph(batch, font, enemy, x, y);
        }
    }

    private void renderTexturedHPBar(SpriteBatch batch, BitmapFont font, AssetLoader assets,
                                     Enemy enemy, float x, float y) {
        float hpPercent = (float) enemy.getHp() / (float) enemy.getMaxHp();
        hpPercent = Math.max(0f, Math.min(1f, hpPercent));

        // Render HP bar frame texture
        TextureRegion hpBarFrame = assets.getHpBarFrame();
        if (hpBarFrame != null) {
            batch.setColor(Color.WHITE);
            batch.draw(hpBarFrame, x, y + 8f, HP_BAR_WIDTH, HP_BAR_HEIGHT + 2f);
        }

        // Render HP fill on top of frame (inset by 1px)
        Color barColor = damageFlash ? Color.WHITE : HP_BAR_FG;
        batch.setColor(barColor);
        TextureRegion fillTex = assets.getParticleTexture("physical");
        if (fillTex != null) {
            float fillWidth = (HP_BAR_WIDTH - 2f) * hpPercent;
            batch.draw(fillTex, x + 1f, y + 9f, fillWidth, HP_BAR_HEIGHT);
        }
        batch.setColor(Color.WHITE);

        // HP text
        font.setColor(Color.WHITE);
        String hpText = enemy.getHp() + "/" + enemy.getMaxHp();
        font.draw(batch, hpText, x, y + 6f);
    }

    private void renderStatusIcons(SpriteBatch batch, AssetLoader assets, Enemy enemy, float x, float y) {
        List<StatusEffect> effects = enemy.getStatusManager().getActiveEffects();
        if (effects.isEmpty()) return;

        float iconX = x;
        float iconSize = 8f;
        for (StatusEffect effect : effects) {
            TextureRegion icon = assets.getStatusIcon(effect.getType());
            if (icon != null) {
                batch.draw(icon, iconX, y - iconSize, iconSize, iconSize);
            }
            iconX += iconSize + 2f;
        }
    }

    private void renderTelegraph(SpriteBatch batch, BitmapFont font, Enemy enemy, float x, float y) {
        // Pulsing exclamation mark icon
        float pulse = (MathUtils.sin(telegraphPulseTimer * 8f) + 1f) / 2f;
        float alpha = 0.5f + pulse * 0.5f;
        Color pulseColor = new Color(TELEGRAPH_COLOR.r, TELEGRAPH_COLOR.g, TELEGRAPH_COLOR.b, alpha);
        font.setColor(pulseColor);

        String warning = "! " + enemy.getNextAbility().getName() + " !";
        font.draw(batch, warning, x, y - 12f);
        font.setColor(Color.WHITE);
    }

    /**
     * Renders enemy HP bar using ShapeRenderer (called outside batch begin/end).
     * Kept for backward compatibility but the textured bar is preferred.
     */
    public void renderShapes(com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer,
                             Enemy enemy, float x, float y) {
        if (enemy == null) return;

        float hpPercent = (float) enemy.getHp() / (float) enemy.getMaxHp();
        hpPercent = Math.max(0f, Math.min(1f, hpPercent));

        // Background
        shapeRenderer.setColor(HP_BAR_BG);
        shapeRenderer.rect(x, y, HP_BAR_WIDTH, HP_BAR_HEIGHT);

        // HP fill
        Color barColor = damageFlash ? Color.WHITE : HP_BAR_FG;
        shapeRenderer.setColor(barColor);
        shapeRenderer.rect(x, y, HP_BAR_WIDTH * hpPercent, HP_BAR_HEIGHT);
    }

    /**
     * Maps an Enemy's name to the sprite entity name used by SpriteGenerator.
     */
    public String getEntityName(Enemy enemy) {
        String name = enemy.getName().toLowerCase().replace(" ", "_");
        // Map common enemy names to sprite keys
        if (name.contains("plague") && name.contains("goblin")) return "plague_goblin";
        if (name.contains("goblin") && name.contains("king")) return "goblin_king";
        if (name.contains("goblin") && name.contains("chieftain")) return "goblin_chieftain";
        if (name.contains("shielded") && name.contains("skeleton")) return "shielded_skeleton";
        if (name.contains("bone") && name.contains("colossus")) return "bone_colossus";
        if (name.contains("goblin")) return "goblin";
        if (name.contains("skeleton")) return "skeleton";
        if (name.contains("lich")) return "lich";
        return name;
    }

    /**
     * Returns the SpriteAnimator for external inspection/triggering.
     */
    public SpriteAnimator getAnimator() {
        return animator;
    }

    /**
     * Returns whether the death animation has completed.
     */
    public boolean isDeathComplete() {
        return animator != null && animator.getCurrentState() == AnimationState.DEAD;
    }
}
