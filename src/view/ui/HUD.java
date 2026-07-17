package view.ui;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import controller.CombatEngine;
import model.Enemy;
import model.Player;
import model.status.StatusEffect;
import view.assets.AssetLoader;
import view.sprites.AnimationState;
import view.sprites.ColorPalette;

/**
 * Renders at 320x240 resolution.
 * Top area (y:220-240): wave counter with decorative banner, enemy name.
 * Middle area (y:120-220): enemy display with sprite + telegraph.
 * Lower-middle (y:60-120): message log (4 lines).
 * Bottom (y:40-60): HP/MP bars with textured frames, status icons.
 * Bottom (y:0-40): Combat action menu with frame.
 *
 * Features animated HP/MP bar fill using lerp, textured bar sprites,
 * player mini-sprite, background tiles, and wave banner.
 */
public class HUD {
    private static final float TOP_Y = 235f;
    private static final float MIDDLE_Y = 190f;
    private static final float BOTTOM_Y = 55f;
    private static final float BAR_WIDTH = 60f;
    private static final float BAR_HEIGHT = 5f;
    private static final float LERP_SPEED = 8f;

    private final EnemyDisplay enemyDisplay;
    private final CombatMenu combatMenu;
    private final MessageLog messageLog;

    // HP flash
    private boolean hpFlash;
    private float hpFlashTimer;
    private static final float HP_FLASH_DURATION = 0.3f;

    // Animated bar fill (lerp)
    private float displayedHp;
    private float displayedMp;
    private boolean lerpInitialized;

    // Player mini-sprite animation
    private float playerSpriteTimer;

    public HUD(CombatMenu combatMenu, MessageLog messageLog) {
        this.enemyDisplay = new EnemyDisplay();
        this.combatMenu = combatMenu;
        this.messageLog = messageLog;
        this.hpFlash = false;
        this.hpFlashTimer = 0f;
        this.displayedHp = -1f;
        this.displayedMp = -1f;
        this.lerpInitialized = false;
        this.playerSpriteTimer = 0f;
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
     * Updates animation timers and lerp values.
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
        playerSpriteTimer += delta;
    }

    /**
     * Renders the HUD using SpriteBatch (text and sprites).
     * Background tiles, HP/MP textured bars, status icons, player mini-sprite,
     * wave banner, enemy sprite and name, message log, combat menu.
     */
    public void render(SpriteBatch batch, BitmapFont font, AssetLoader assets,
                       CombatEngine engine, boolean showMenu) {
        Player player = engine.getPlayer();
        Enemy enemy = engine.getCurrentEnemy();
        int wave = engine.getCurrentWave();
        int totalWaves = engine.getWaveManager().getTotalWaves();

        // --- Background tiles tiled across the 320x240 screen ---
        renderBackgroundTiles(batch, assets);

        // --- Top area: Wave banner and enemy name ---
        renderWaveBanner(batch, font, assets, wave, totalWaves);

        // Enemy name plate above enemy sprite area
        if (enemy != null) {
            font.setColor(Color.WHITE);
            float nameX = 160f - (enemy.getName().length() * 2.5f);
            font.draw(batch, enemy.getName(), nameX, TOP_Y - 10f);
        }

        // --- Middle area: Enemy display ---
        if (enemy != null) {
            enemyDisplay.render(batch, font, assets, enemy, 120f, MIDDLE_Y - 40f);
        }

        // Message log (left side, lower-middle)
        messageLog.render(batch, font, 5f, 115f, 9f);

        // --- Bottom area: Player stats with textured bars ---
        if (player != null) {
            renderPlayerStats(batch, font, assets, player);
        }

        // Combat menu (right side of bottom area)
        if (showMenu) {
            combatMenu.render(batch, font, assets, 170f, BOTTOM_Y);
        }
    }

    /**
     * Renders background tiles across the 320x240 screen.
     */
    private void renderBackgroundTiles(SpriteBatch batch, AssetLoader assets) {
        TextureRegion bgTile = assets.getBackgroundTile();
        if (bgTile == null) return;

        int tileW = bgTile.getRegionWidth();
        int tileH = bgTile.getRegionHeight();
        if (tileW <= 0 || tileH <= 0) return;

        batch.setColor(Color.WHITE);
        for (int tx = 0; tx < 320; tx += tileW) {
            for (int ty = 0; ty < 240; ty += tileH) {
                batch.draw(bgTile, tx, ty);
            }
        }
    }

    /**
     * Renders wave counter with decorative frame from getWaveBanner().
     */
    private void renderWaveBanner(SpriteBatch batch, BitmapFont font, AssetLoader assets,
                                  int wave, int totalWaves) {
        TextureRegion banner = assets.getWaveBanner();
        if (banner != null) {
            float bannerW = banner.getRegionWidth() * 2f;
            float bannerH = banner.getRegionHeight() * 2f;
            float bannerX = 160f - bannerW / 2f;
            batch.setColor(Color.WHITE);
            batch.draw(banner, bannerX, TOP_Y - 5f, bannerW, bannerH);
        }

        font.setColor(ColorPalette.HOLY_GOLD);
        font.draw(batch, "Wave " + wave + "/" + totalWaves, 130f, TOP_Y);
        font.setColor(Color.WHITE);
    }

    /**
     * Renders HP/MP bars and status effects using ShapeRenderer.
     * Must be called outside of SpriteBatch begin/end.
     */
    public void renderShapes(ShapeRenderer shapeRenderer, CombatEngine engine) {
        Player player = engine.getPlayer();
        Enemy enemy = engine.getCurrentEnemy();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player HP bar fill (behind textured frame)
        if (player != null) {
            float targetHp = (float) player.getHp() / (float) player.getMaxHp();
            targetHp = Math.max(0f, Math.min(1f, targetHp));

            float targetMp = (float) player.getMana() / (float) player.getMaxMana();
            targetMp = Math.max(0f, Math.min(1f, targetMp));

            // Initialize lerp values on first frame
            if (!lerpInitialized) {
                displayedHp = targetHp;
                displayedMp = targetMp;
                lerpInitialized = true;
            }

            // Lerp displayed values toward actual values
            float delta = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
            displayedHp = MathUtils.lerp(displayedHp, targetHp, LERP_SPEED * delta);
            displayedMp = MathUtils.lerp(displayedMp, targetMp, LERP_SPEED * delta);

            // HP background
            shapeRenderer.setColor(ColorPalette.HP_BG);
            shapeRenderer.rect(5f, 40f, BAR_WIDTH, BAR_HEIGHT);
            // HP fill with lerp
            Color hpColor = hpFlash ? Color.WHITE : ColorPalette.HP_RED;
            shapeRenderer.setColor(hpColor);
            shapeRenderer.rect(5f, 40f, BAR_WIDTH * displayedHp, BAR_HEIGHT);

            // MP bar
            shapeRenderer.setColor(ColorPalette.MP_BG);
            shapeRenderer.rect(5f, 32f, BAR_WIDTH, BAR_HEIGHT);
            shapeRenderer.setColor(ColorPalette.MP_BLUE);
            shapeRenderer.rect(5f, 32f, BAR_WIDTH * displayedMp, BAR_HEIGHT);
        }

        // Enemy HP bar
        if (enemy != null) {
            enemyDisplay.renderShapes(shapeRenderer, enemy, 120f, MIDDLE_Y - 50f);
        }

        shapeRenderer.end();
    }

    private void renderPlayerStats(SpriteBatch batch, BitmapFont font, AssetLoader assets, Player player) {
        // Textured HP bar frame
        TextureRegion hpFrame = assets.getHpBarFrame();
        if (hpFrame != null) {
            batch.setColor(Color.WHITE);
            batch.draw(hpFrame, 4f, 39f, BAR_WIDTH + 2f, BAR_HEIGHT + 2f);
        }

        // Textured MP bar frame
        TextureRegion mpFrame = assets.getMpBarFrame();
        if (mpFrame != null) {
            batch.setColor(Color.WHITE);
            batch.draw(mpFrame, 4f, 31f, BAR_WIDTH + 2f, BAR_HEIGHT + 2f);
        }

        // HP and MP text
        Color hpTextColor = hpFlash ? Color.YELLOW : ColorPalette.HP_RED;
        font.setColor(hpTextColor);
        font.draw(batch, "HP:" + player.getHp() + "/" + player.getMaxHp(), 5f, BOTTOM_Y);

        font.setColor(ColorPalette.MP_BLUE);
        font.draw(batch, "MP:" + player.getMana() + "/" + player.getMaxMana(), 5f, BOTTOM_Y - 10f);

        // ATK/DEF
        font.setColor(Color.WHITE);
        font.draw(batch, "ATK:" + player.getAttackPower() + " DEF:" + player.getDefense(),
                  5f, BOTTOM_Y - 20f);

        // Player mini-sprite in HUD corner
        renderPlayerMiniSprite(batch, assets);

        // Status effects rendered as sprite icons
        renderPlayerStatusIcons(batch, font, assets, player);

        font.setColor(Color.WHITE);
    }

    /**
     * Renders a small player sprite in the bottom-left corner.
     */
    private void renderPlayerMiniSprite(SpriteBatch batch, AssetLoader assets) {
        TextureRegion[] playerFrames = assets.getEntityFrames("player", AnimationState.IDLE);
        if (playerFrames == null || playerFrames.length == 0) return;

        // Pick frame based on timer for idle animation
        int frameIdx = (int) (playerSpriteTimer / 0.5f) % playerFrames.length;
        TextureRegion frame = playerFrames[frameIdx];

        float scale = 1.5f;
        float spriteW = frame.getRegionWidth() * scale;
        float spriteH = frame.getRegionHeight() * scale;
        batch.setColor(Color.WHITE);
        batch.draw(frame, 70f, 30f, spriteW, spriteH);
    }

    /**
     * Renders player status effect icons as actual sprites.
     */
    private void renderPlayerStatusIcons(SpriteBatch batch, BitmapFont font, AssetLoader assets, Player player) {
        List<StatusEffect> effects = player.getStatusManager().getActiveEffects();
        if (effects.isEmpty()) return;

        float iconX = 5f;
        float iconY = BOTTOM_Y - 38f;
        float iconSize = 8f;

        for (StatusEffect effect : effects) {
            TextureRegion icon = assets.getStatusIcon(effect.getType());
            if (icon != null) {
                batch.setColor(Color.WHITE);
                batch.draw(icon, iconX, iconY, iconSize, iconSize);
                // Duration number next to icon
                font.setColor(Color.WHITE);
                font.draw(batch, String.valueOf(effect.getDuration()), iconX + iconSize + 1f, iconY + iconSize);
            } else {
                // Fallback: text abbreviation
                font.setColor(getStatusColor(effect));
                font.draw(batch, effect.getType().name().substring(0, 3), iconX, iconY + iconSize);
            }
            iconX += iconSize + 12f;
        }
    }

    private Color getStatusColor(StatusEffect effect) {
        switch (effect.getType()) {
            case POISON: return ColorPalette.POISON_GREEN;
            case REGEN: return ColorPalette.REGEN_GREEN;
            case ENRAGE: return ColorPalette.ENRAGE_RED;
            case SHIELD: return ColorPalette.SHIELD_BLUE;
            case CURSE: return ColorPalette.CURSE_PURPLE;
            case STUN: return ColorPalette.STUN_YELLOW;
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
