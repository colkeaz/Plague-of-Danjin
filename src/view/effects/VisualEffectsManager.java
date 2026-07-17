package view.effects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import model.events.GameEvent;
import model.events.GameEventType;
import view.assets.AssetLoader;

/**
 * Manages on-screen particle effects, damage numbers, and screen flash animations.
 * Effects are triggered by GameEvents during combat animations.
 */
public class VisualEffectsManager {
    private static final int MAX_PARTICLES = 200;
    private static final int MAX_DAMAGE_NUMBERS = 20;

    private final List<Particle> particles;
    private final List<DamageNumber> damageNumbers;

    // Screen flash state
    private Color screenFlashColor;
    private float screenFlashDuration;
    private float screenFlashElapsed;
    private boolean screenFlashing;

    public VisualEffectsManager() {
        particles = new ArrayList<>();
        damageNumbers = new ArrayList<>();
        screenFlashColor = new Color(Color.WHITE);
        screenFlashDuration = 0f;
        screenFlashElapsed = 0f;
        screenFlashing = false;
    }

    /**
     * Updates all active particles and damage numbers, removing dead ones.
     */
    public void update(float delta) {
        // Update particles
        Iterator<Particle> particleIt = particles.iterator();
        while (particleIt.hasNext()) {
            Particle p = particleIt.next();
            p.update(delta);
            if (!p.isAlive()) {
                particleIt.remove();
            }
        }

        // Update damage numbers
        Iterator<DamageNumber> numberIt = damageNumbers.iterator();
        while (numberIt.hasNext()) {
            DamageNumber dn = numberIt.next();
            dn.update(delta);
            if (!dn.isAlive()) {
                numberIt.remove();
            }
        }

        // Update screen flash
        if (screenFlashing) {
            screenFlashElapsed += delta;
            if (screenFlashElapsed >= screenFlashDuration) {
                screenFlashing = false;
            }
        }
    }

    /**
     * Renders all active effects (particles and damage numbers).
     */
    public void render(SpriteBatch batch, BitmapFont font, AssetLoader assets) {
        // Render particles
        TextureRegion particleTex = assets.getParticleTexture("physical");
        if (particleTex != null) {
            for (Particle p : particles) {
                p.render(batch, particleTex);
            }
        }

        // Reset batch color after particles
        batch.setColor(Color.WHITE);

        // Render damage numbers
        for (DamageNumber dn : damageNumbers) {
            dn.render(batch, font);
        }

        // Reset font color
        font.setColor(Color.WHITE);
    }

    /**
     * Spawns a floating damage number at the given position.
     */
    public void spawnDamageNumber(String text, float x, float y, Color color) {
        if (damageNumbers.size() < MAX_DAMAGE_NUMBERS) {
            damageNumbers.add(new DamageNumber(text, x, y, color));
        }
    }

    /**
     * Spawns a burst of particles at the given position.
     */
    public void spawnParticles(String type, float x, float y, int count, Color color) {
        for (int i = 0; i < count && particles.size() < MAX_PARTICLES; i++) {
            float vx = MathUtils.random(-30f, 30f);
            float vy = MathUtils.random(10f, 50f);
            float life = MathUtils.random(0.4f, 0.9f);
            int size = MathUtils.random(1, 3);
            Particle p = new Particle(x + MathUtils.random(-4f, 4f), y + MathUtils.random(-4f, 4f),
                    vx, vy, life, size, color);
            p.setGravity(-40f);
            particles.add(p);
        }
    }

    /**
     * Spawns red slash mark particles over enemy sprite.
     */
    public void spawnSlashEffect(float x, float y) {
        Color slashColor = new Color(1f, 0.2f, 0.2f, 1f);
        for (int i = 0; i < 6 && particles.size() < MAX_PARTICLES; i++) {
            float angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
            float speed = MathUtils.random(20f, 45f);
            float vx = MathUtils.cos(angle) * speed;
            float vy = MathUtils.sin(angle) * speed;
            float life = MathUtils.random(0.2f, 0.4f);
            Particle p = new Particle(x + MathUtils.random(-6f, 6f), y + MathUtils.random(-6f, 6f),
                    vx, vy, life, 2, slashColor);
            particles.add(p);
        }
    }

    /**
     * Spawns orange/red fire particles that fly toward a target position.
     */
    public void spawnFireEffect(float x, float y) {
        Color fireOrange = new Color(1f, 0.4f, 0f, 1f);
        Color fireYellow = new Color(1f, 0.8f, 0f, 1f);
        for (int i = 0; i < 12 && particles.size() < MAX_PARTICLES; i++) {
            Color c = (i % 2 == 0) ? fireOrange : fireYellow;
            float vx = MathUtils.random(-20f, 20f);
            float vy = MathUtils.random(15f, 50f);
            float life = MathUtils.random(0.4f, 0.8f);
            int size = MathUtils.random(1, 3);
            Particle p = new Particle(x + MathUtils.random(-8f, 8f), y + MathUtils.random(-4f, 4f),
                    vx, vy, life, size, c);
            p.setGravity(-20f);
            particles.add(p);
        }
    }

    /**
     * Spawns golden holy sparkles that float upward from position.
     */
    public void spawnHolyEffect(float x, float y) {
        Color gold = new Color(1f, 0.84f, 0f, 1f);
        Color white = new Color(1f, 1f, 0.8f, 1f);
        for (int i = 0; i < 10 && particles.size() < MAX_PARTICLES; i++) {
            Color c = (i % 3 == 0) ? white : gold;
            float vx = MathUtils.random(-10f, 10f);
            float vy = MathUtils.random(20f, 50f);
            float life = MathUtils.random(0.5f, 1.0f);
            int size = MathUtils.random(1, 2);
            Particle p = new Particle(x + MathUtils.random(-10f, 10f), y,
                    vx, vy, life, size, c);
            particles.add(p);
        }
    }

    /**
     * Spawns green poison bubbles floating upward from target.
     */
    public void spawnPoisonEffect(float x, float y) {
        Color poisonGreen = new Color(0f, 0.8f, 0.27f, 1f);
        Color poisonDark = new Color(0f, 0.5f, 0.15f, 1f);
        for (int i = 0; i < 8 && particles.size() < MAX_PARTICLES; i++) {
            Color c = (i % 2 == 0) ? poisonGreen : poisonDark;
            float vx = MathUtils.random(-8f, 8f);
            float vy = MathUtils.random(15f, 35f);
            float life = MathUtils.random(0.5f, 0.9f);
            int size = MathUtils.random(1, 3);
            Particle p = new Particle(x + MathUtils.random(-6f, 6f), y,
                    vx, vy, life, size, c);
            particles.add(p);
        }
    }

    /**
     * Spawns dark purple/black particles for dark magic effects.
     */
    public void spawnDarkEffect(float x, float y) {
        Color darkPurple = new Color(0.55f, 0f, 1f, 1f);
        Color darkBlack = new Color(0.15f, 0f, 0.2f, 1f);
        for (int i = 0; i < 10 && particles.size() < MAX_PARTICLES; i++) {
            Color c = (i % 2 == 0) ? darkPurple : darkBlack;
            float vx = MathUtils.random(-25f, 25f);
            float vy = MathUtils.random(-15f, 25f);
            float life = MathUtils.random(0.4f, 0.8f);
            int size = MathUtils.random(1, 3);
            Particle p = new Particle(x + MathUtils.random(-8f, 8f), y + MathUtils.random(-8f, 8f),
                    vx, vy, life, size, c);
            particles.add(p);
        }
    }

    /**
     * Triggers a screen-wide color flash that fades out over the given duration.
     */
    public void triggerScreenFlash(Color color, float duration) {
        this.screenFlashColor.set(color);
        this.screenFlashDuration = duration;
        this.screenFlashElapsed = 0f;
        this.screenFlashing = true;
    }

    /**
     * Returns true if a screen flash is currently active.
     */
    public boolean isScreenFlashing() {
        return screenFlashing;
    }

    /**
     * Returns the screen flash color with current alpha (fades over time).
     */
    public Color getScreenFlashColor() {
        if (!screenFlashing) return Color.CLEAR;
        float progress = screenFlashElapsed / screenFlashDuration;
        float alpha = Math.max(0f, 1f - progress);
        return new Color(screenFlashColor.r, screenFlashColor.g, screenFlashColor.b, alpha * 0.4f);
    }

    // --- GameEvent-driven effect triggers ---

    /**
     * Handles a damage dealt event by spawning slash particles and a damage number.
     */
    public void onDamageDealt(GameEvent event, float targetX, float targetY) {
        int amount = event.getInt("damage");
        String text = String.valueOf(amount > 0 ? amount : "");
        spawnSlashEffect(targetX, targetY);
        spawnDamageNumber(text, targetX, targetY + 10, new Color(1f, 0.2f, 0.2f, 1f));
    }

    /**
     * Handles a critical hit event with larger effects and screen shake.
     */
    public void onCriticalHit(GameEvent event, float targetX, float targetY) {
        int amount = event.getInt("damage");
        String text = String.valueOf(amount > 0 ? amount : "CRIT!");
        // Bigger slash effect
        spawnSlashEffect(targetX, targetY);
        spawnSlashEffect(targetX + 4, targetY + 4);
        // Yellow crit text
        spawnDamageNumber(text, targetX - 4, targetY + 14, new Color(1f, 1f, 0f, 1f));
        spawnDamageNumber("CRITICAL!", targetX - 8, targetY + 24, new Color(1f, 1f, 0f, 1f));
        // Screen flash
        triggerScreenFlash(new Color(1f, 1f, 1f, 1f), 0.15f);
    }

    /**
     * Handles a spell cast event by spawning element-appropriate particles.
     */
    public void onSpellCast(GameEvent event, float targetX, float targetY) {
        String element = event.getString("element");
        if (element == null) element = "PHYSICAL";

        switch (element.toUpperCase()) {
            case "FIRE":
                spawnFireEffect(targetX, targetY);
                break;
            case "HOLY":
                spawnHolyEffect(targetX, targetY);
                break;
            case "DARK":
                spawnDarkEffect(targetX, targetY);
                break;
            case "POISON":
                spawnPoisonEffect(targetX, targetY);
                break;
            default:
                spawnSlashEffect(targetX, targetY);
                break;
        }
    }

    /**
     * Handles enemy death with dissolve-like particle burst.
     */
    public void onEnemyDeath(float x, float y) {
        Color deathColor = new Color(0.6f, 0.6f, 0.6f, 1f);
        spawnParticles("physical", x, y, 15, deathColor);
    }

    /**
     * Handles level/wave up with golden screen flash.
     */
    public void onLevelUp() {
        triggerScreenFlash(new Color(1f, 0.84f, 0f, 1f), 0.4f);
    }

    /**
     * Handles player taking damage with red screen edge flash.
     */
    public void onPlayerDamaged(GameEvent event, float playerX, float playerY) {
        int amount = event.getInt("damage");
        String text = String.valueOf(amount > 0 ? amount : "");
        spawnDamageNumber(text, playerX, playerY + 10, new Color(1f, 0.2f, 0.2f, 1f));
        triggerScreenFlash(new Color(1f, 0f, 0f, 1f), 0.2f);
    }

    /**
     * Handles heal events with green number and sparkles.
     */
    public void onHeal(GameEvent event, float targetX, float targetY) {
        int amount = event.getInt("amount");
        String text = "+" + (amount > 0 ? amount : "");
        spawnDamageNumber(text, targetX, targetY + 10, new Color(0.2f, 1f, 0.2f, 1f));
        Color healGreen = new Color(0.2f, 1f, 0.4f, 1f);
        spawnParticles("physical", targetX, targetY, 5, healGreen);
    }

    /**
     * Returns the number of active particles.
     */
    public int getParticleCount() {
        return particles.size();
    }

    /**
     * Returns the number of active damage numbers.
     */
    public int getDamageNumberCount() {
        return damageNumbers.size();
    }

    /**
     * Clears all active effects.
     */
    public void clear() {
        particles.clear();
        damageNumbers.clear();
        screenFlashing = false;
    }
}
