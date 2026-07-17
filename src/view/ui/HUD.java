package view.ui;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import controller.CombatEngine;
import model.Enemy;
import model.Player;
import model.status.StatusEffect;
import view.assets.AssetLoader;
import view.rendering.PixelRenderer;

/**
 * Renders at 320x240 resolution.
 * Top area (y:200-240): wave counter, enemy name + HP bar + telegraph warning.
 * Middle area (y:60-200): enemy display + message log.
 * Bottom area (y:0-60): player HP/MP bars, status effect icons, action menu.
 * HP bar flashes on damage. Status icons show remaining duration.
 */
public class HUD {
    private static final float TOP_Y = 235f;
    private static final float MIDDLE_Y = 190f;
    private static final float BOTTOM_Y = 55f;
    private static final float BAR_WIDTH = 60f;
    private static final float BAR_HEIGHT = 5f;

    private final EnemyDisplay enemyDisplay;
    private final CombatMenu combatMenu;
    private final MessageLog messageLog;

    private boolean hpFlash;
    private float hpFlashTimer;
    private static final float HP_FLASH_DURATION = 0.3f;

    public HUD(CombatMenu combatMenu, MessageLog messageLog) {
        this.enemyDisplay = new EnemyDisplay();
        this.combatMenu = combatMenu;
        this.messageLog = messageLog;
        this.hpFlash = false;
        this.hpFlashTimer = 0f;
    }

    /**
     * Triggers the HP bar flash effect when the player takes damage.
     */
    public void triggerHpFlash() {
        hpFlash = true;
        hpFlashTimer = HP_FLASH_DURATION;
    }

    /**
     * Triggers enemy damage flash.
     */
    public void triggerEnemyDamageFlash() {
        enemyDisplay.triggerDamageFlash();
    }

    /**
     * Updates animation timers.
     */
    public void update(float delta) {
        if (hpFlash) {
            hpFlashTimer -= delta;
            if (hpFlashTimer <= 0f) {
                hpFlash = false;
            }
        }
        enemyDisplay.update(delta);
        messageLog.update(delta);
    }

    /**
     * Renders the HUD using SpriteBatch (text and sprites).
     */
    public void render(SpriteBatch batch, BitmapFont font, AssetLoader assets,
                       CombatEngine engine, boolean showMenu) {
        Player player = engine.getPlayer();
        Enemy enemy = engine.getCurrentEnemy();
        int wave = engine.getCurrentWave();
        int totalWaves = engine.getWaveManager().getTotalWaves();

        // --- Top area: Wave counter and enemy info ---
        font.setColor(Color.WHITE);
        font.draw(batch, "Wave " + wave + "/" + totalWaves, 5f, TOP_Y);

        // --- Middle area: Enemy display + message log ---
        if (enemy != null) {
            enemyDisplay.render(batch, font, assets, enemy, 120f, MIDDLE_Y - 40f);
        }

        // Message log (left side)
        messageLog.render(batch, font, 5f, MIDDLE_Y - 60f, 9f);

        // --- Bottom area: Player stats and menu ---
        if (player != null) {
            renderPlayerStats(batch, font, player);
        }

        // Combat menu (right side of bottom area)
        if (showMenu) {
            combatMenu.render(batch, font, 170f, BOTTOM_Y);
        }
    }

    /**
     * Renders HP/MP bars and status effects using ShapeRenderer.
     * Must be called outside of SpriteBatch begin/end.
     */
    public void renderShapes(ShapeRenderer shapeRenderer, CombatEngine engine) {
        Player player = engine.getPlayer();
        Enemy enemy = engine.getCurrentEnemy();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player HP bar
        if (player != null) {
            float hpPercent = (float) player.getHp() / (float) player.getMaxHp();
            hpPercent = Math.max(0f, Math.min(1f, hpPercent));

            // HP background
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(5f, 40f, BAR_WIDTH, BAR_HEIGHT);
            // HP fill
            Color hpColor = hpFlash ? Color.WHITE : Color.GREEN;
            shapeRenderer.setColor(hpColor);
            shapeRenderer.rect(5f, 40f, BAR_WIDTH * hpPercent, BAR_HEIGHT);

            // MP bar
            float mpPercent = (float) player.getMana() / (float) player.getMaxMana();
            mpPercent = Math.max(0f, Math.min(1f, mpPercent));
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(5f, 32f, BAR_WIDTH, BAR_HEIGHT);
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.rect(5f, 32f, BAR_WIDTH * mpPercent, BAR_HEIGHT);
        }

        // Enemy HP bar
        if (enemy != null) {
            enemyDisplay.renderShapes(shapeRenderer, enemy, 120f, MIDDLE_Y - 50f);
        }

        shapeRenderer.end();
    }

    private void renderPlayerStats(SpriteBatch batch, BitmapFont font, Player player) {
        // HP and MP text
        Color hpTextColor = hpFlash ? Color.YELLOW : Color.GREEN;
        font.setColor(hpTextColor);
        font.draw(batch, "HP:" + player.getHp() + "/" + player.getMaxHp(), 5f, BOTTOM_Y);

        font.setColor(Color.CYAN);
        font.draw(batch, "MP:" + player.getMana() + "/" + player.getMaxMana(), 5f, BOTTOM_Y - 10f);

        // ATK/DEF
        font.setColor(Color.WHITE);
        font.draw(batch, "ATK:" + player.getAttackPower() + " DEF:" + player.getDefense(),
                  5f, BOTTOM_Y - 20f);

        // Status effects
        List<StatusEffect> effects = player.getStatusManager().getActiveEffects();
        if (!effects.isEmpty()) {
            float statusX = 5f;
            float statusY = BOTTOM_Y - 30f;
            for (StatusEffect effect : effects) {
                font.setColor(getStatusColor(effect));
                font.draw(batch, effect.getType().name().substring(0, 3) +
                          "(" + effect.getDuration() + ")", statusX, statusY);
                statusX += 35f;
            }
        }

        font.setColor(Color.WHITE);
    }

    private Color getStatusColor(StatusEffect effect) {
        switch (effect.getType()) {
            case POISON: return Color.GREEN;
            case REGEN: return Color.LIME;
            case ENRAGE: return Color.RED;
            case SHIELD: return Color.CYAN;
            case CURSE: return Color.PURPLE;
            case STUN: return Color.YELLOW;
            default: return Color.WHITE;
        }
    }

    public EnemyDisplay getEnemyDisplay() {
        return enemyDisplay;
    }

    public CombatMenu getCombatMenu() {
        return combatMenu;
    }

    public MessageLog getMessageLog() {
        return messageLog;
    }
}
