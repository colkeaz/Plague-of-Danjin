package view.rendering;

import java.util.LinkedList;
import java.util.Queue;

import model.events.GameEvent;
import model.events.GameEventType;

/**
 * Animation queue system. Queues GameEvents as visual animations
 * (damage flash, number popup, HP bar decrease). Plays sequentially.
 * Only advances game state after all animations complete.
 */
public class AnimationManager {
    private static final float DAMAGE_ANIMATION_DURATION = 0.5f;
    private static final float HEAL_ANIMATION_DURATION = 0.4f;
    private static final float DEFAULT_ANIMATION_DURATION = 0.3f;

    private final Queue<AnimationEntry> animationQueue;
    private AnimationEntry currentAnimation;
    private float elapsed;

    public AnimationManager() {
        this.animationQueue = new LinkedList<>();
        this.currentAnimation = null;
        this.elapsed = 0f;
    }

    /**
     * Queues a game event as a visual animation.
     */
    public void queueAnimation(GameEvent event) {
        float duration = getDurationForEvent(event);
        AnimationEntry entry = new AnimationEntry(event, duration);
        animationQueue.add(entry);

        // Start playing immediately if nothing is currently playing
        if (currentAnimation == null) {
            advanceToNext();
        }
    }

    /**
     * Updates the animation system each frame.
     */
    public void update(float delta) {
        if (currentAnimation == null) {
            return;
        }

        elapsed += delta;
        if (elapsed >= currentAnimation.duration) {
            advanceToNext();
        }
    }

    /**
     * Returns true if an animation is currently playing or queued.
     */
    public boolean isPlaying() {
        return currentAnimation != null;
    }

    /**
     * Returns the current animation being played, or null if none.
     */
    public AnimationEntry getCurrentAnimation() {
        return currentAnimation;
    }

    /**
     * Returns the progress (0.0 to 1.0) of the current animation.
     */
    public float getProgress() {
        if (currentAnimation == null) return 0f;
        return Math.min(1f, elapsed / currentAnimation.duration);
    }

    /**
     * Returns the GameEventType of the currently playing animation, or null if none.
     */
    public GameEventType getActiveEventType() {
        if (currentAnimation == null) return null;
        GameEvent event = currentAnimation.getEvent();
        return event != null ? event.getType() : null;
    }

    /**
     * Returns the full GameEvent of the currently playing animation, or null if none.
     */
    public GameEvent getActiveEvent() {
        if (currentAnimation == null) return null;
        return currentAnimation.getEvent();
    }

    /**
     * Clears all queued animations.
     */
    public void clear() {
        animationQueue.clear();
        currentAnimation = null;
        elapsed = 0f;
    }

    private void advanceToNext() {
        if (animationQueue.isEmpty()) {
            currentAnimation = null;
            elapsed = 0f;
        } else {
            currentAnimation = animationQueue.poll();
            elapsed = 0f;
        }
    }

    private float getDurationForEvent(GameEvent event) {
        if (event == null) return DEFAULT_ANIMATION_DURATION;

        GameEventType type = event.getType();
        switch (type) {
            case DAMAGE_DEALT:
            case CRITICAL_HIT:
            case ENEMY_ATTACK:
            case ENEMY_ABILITY_FIRED:
                return DAMAGE_ANIMATION_DURATION;
            case HEAL:
            case MANA_REGEN:
                return HEAL_ANIMATION_DURATION;
            default:
                return DEFAULT_ANIMATION_DURATION;
        }
    }

    /**
     * Represents a single animation entry in the queue.
     */
    public static class AnimationEntry {
        private final GameEvent event;
        private final float duration;

        public AnimationEntry(GameEvent event, float duration) {
            this.event = event;
            this.duration = duration;
        }

        public GameEvent getEvent() {
            return event;
        }

        public float getDuration() {
            return duration;
        }
    }
}
