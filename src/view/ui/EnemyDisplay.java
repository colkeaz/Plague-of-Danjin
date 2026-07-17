package view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import model.Enemy;
import view.assets.AssetLoader;
import view.sprites.AnimationState;

/**
 * Enemy sprite area + HP bar + telegraph indicator.
 * Renders placeholder colored rectangle for enemy.
 * Shows HP bar with numerical values.
 * Shows telegraph warning text when enemy.isWindingUp().
 */
public class EnemyDisplay {
    private static final float HP_BAR_WIDTH = 80f;
    private static final float HP_BAR_HEIGHT = 6f;
    private static final Color HP_BAR_BG = new Color(0.2f, 0.2f, 0.2f, 1f);
    private static final Color HP_BAR_FG = Color.RED;
    private static final Color TELEGRAPH_COLOR = Color.YELLOW;

    private boolean damageFlash;
    private float damageFlashTimer;
    private static final float DAMAGE_FLASH_DURATION = 0.15f;

    public EnemyDisplay() {
        this.damageFlash = false;
        this.damageFlashTimer = 0f;
    }

    /**
     * Triggers a damage flash effect.
     */
    public void triggerDamageFlash() {
        damageFlash = true;
        damageFlashTimer = DAMAGE_FLASH_DURATION;
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
    }

    /**
     * Renders the enemy display at the given position.
     */
    public void render(SpriteBatch batch, BitmapFont font, AssetLoader assets, Enemy enemy, float x, float y) {
        if (enemy == null) return;

        // Render enemy sprite using generated sprite frames
        String entityName = getEntityName(enemy);
        TextureRegion[] frames = assets.getEntityFrames(entityName, AnimationState.IDLE);
        if (frames != null && frames.length > 0) {
            TextureRegion frame = frames[0];
            Color tint = damageFlash ? Color.WHITE : Color.RED;
            batch.setColor(tint);
            float spriteX = x + (HP_BAR_WIDTH - frame.getRegionWidth()) / 2f;
            batch.draw(frame, spriteX, y + 20f);
            batch.setColor(Color.WHITE);
        }

        // Render enemy name
        font.setColor(Color.WHITE);
        font.draw(batch, enemy.getName(), x, y + 16f);

        // Render HP bar background
        // Draw using a 1x1 white pixel from the batch
        renderHPBar(batch, font, enemy, x, y);

        // Render telegraph warning
        if (enemy.isWindingUp() && enemy.getNextAbility() != null) {
            font.setColor(TELEGRAPH_COLOR);
            String warning = "! " + enemy.getNextAbility().getName() + " !";
            font.draw(batch, warning, x, y - 10f);
            font.setColor(Color.WHITE);
        }
    }

    private void renderHPBar(SpriteBatch batch, BitmapFont font, Enemy enemy, float x, float y) {
        float hpPercent = (float) enemy.getHp() / (float) enemy.getMaxHp();
        hpPercent = Math.max(0f, Math.min(1f, hpPercent));

        // HP text
        font.setColor(Color.WHITE);
        String hpText = enemy.getHp() + "/" + enemy.getMaxHp();
        font.draw(batch, hpText, x, y + 6f);
    }

    /**
     * Renders enemy HP bar using ShapeRenderer (called outside batch begin/end).
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
    private String getEntityName(Enemy enemy) {
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
}
