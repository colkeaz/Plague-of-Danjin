package view.sprites;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Per-entity animation state machine. Manages transitions between animation states
 * and provides the correct frame based on elapsed time.
 *
 * Idle loops continuously. Attack/Hurt/Casting play once then return to IDLE.
 * Dying plays once then transitions to DEAD (stays on last frame).
 */
public class SpriteAnimator {
    private static final float DEFAULT_IDLE_DURATION = 0.15f;
    private static final float DEFAULT_ATTACK_DURATION = 0.1f;
    private static final float DEFAULT_HURT_DURATION = 0.12f;
    private static final float DEFAULT_DYING_DURATION = 0.2f;
    private static final float DEFAULT_CASTING_DURATION = 0.12f;

    private AnimationState currentState;
    private float stateTime;
    private final Map<AnimationState, TextureRegion[]> frames;
    private final Map<AnimationState, Float> frameDurations;

    /**
     * Creates a SpriteAnimator with the given frames for each animation state.
     *
     * @param animationFrames map of animation state to TextureRegion arrays
     */
    public SpriteAnimator(Map<AnimationState, TextureRegion[]> animationFrames) {
        this.frames = new EnumMap<>(AnimationState.class);
        this.frames.putAll(animationFrames);
        this.frameDurations = new EnumMap<>(AnimationState.class);

        // Set default durations
        frameDurations.put(AnimationState.IDLE, DEFAULT_IDLE_DURATION);
        frameDurations.put(AnimationState.ATTACKING, DEFAULT_ATTACK_DURATION);
        frameDurations.put(AnimationState.HURT, DEFAULT_HURT_DURATION);
        frameDurations.put(AnimationState.DYING, DEFAULT_DYING_DURATION);
        frameDurations.put(AnimationState.CASTING, DEFAULT_CASTING_DURATION);
        frameDurations.put(AnimationState.DEAD, 1.0f); // Single frame, duration irrelevant

        this.currentState = AnimationState.IDLE;
        this.stateTime = 0f;
    }

    /**
     * Sets a custom frame duration for the given animation state.
     */
    public void setFrameDuration(AnimationState state, float duration) {
        frameDurations.put(state, duration);
    }

    /**
     * Transitions to a new animation state and resets the state timer.
     * Transitioning to DEAD is only allowed from DYING (internal use).
     */
    public void setState(AnimationState newState) {
        if (currentState == AnimationState.DEAD && newState != AnimationState.IDLE) {
            return; // Cannot transition out of DEAD except to reset
        }
        if (newState != currentState) {
            currentState = newState;
            stateTime = 0f;
        }
    }

    /**
     * Updates the animation timer and handles state transitions.
     *
     * @param delta time elapsed since last frame in seconds
     */
    public void update(float delta) {
        stateTime += delta;

        if (currentState == AnimationState.DEAD) {
            return; // Stay on last frame permanently
        }

        TextureRegion[] currentFrames = frames.get(currentState);
        if (currentFrames == null || currentFrames.length == 0) {
            return;
        }

        float frameDuration = frameDurations.getOrDefault(currentState, DEFAULT_IDLE_DURATION);
        float totalDuration = frameDuration * currentFrames.length;

        switch (currentState) {
            case IDLE:
            case CASTING:
                // Loop: stateTime wraps around (handled in getCurrentFrame)
                break;

            case ATTACKING:
            case HURT:
                // Play once then return to IDLE
                if (stateTime >= totalDuration) {
                    currentState = AnimationState.IDLE;
                    stateTime = 0f;
                }
                break;

            case DYING:
                // Play once then transition to DEAD
                if (stateTime >= totalDuration) {
                    currentState = AnimationState.DEAD;
                    stateTime = 0f;
                }
                break;

            default:
                break;
        }
    }

    /**
     * Returns the current texture frame to render based on state and elapsed time.
     *
     * @return the TextureRegion for the current frame, or null if no frames available
     */
    public TextureRegion getCurrentFrame() {
        AnimationState stateForFrame = currentState;

        // DEAD uses the last frame of the DYING animation
        if (stateForFrame == AnimationState.DEAD) {
            TextureRegion[] dyingFrames = frames.get(AnimationState.DYING);
            if (dyingFrames != null && dyingFrames.length > 0) {
                return dyingFrames[dyingFrames.length - 1];
            }
            // Fallback to IDLE if no dying frames
            stateForFrame = AnimationState.IDLE;
        }

        TextureRegion[] currentFrames = frames.get(stateForFrame);
        if (currentFrames == null || currentFrames.length == 0) {
            // Fallback: try IDLE
            currentFrames = frames.get(AnimationState.IDLE);
            if (currentFrames == null || currentFrames.length == 0) {
                return null;
            }
        }

        float frameDuration = frameDurations.getOrDefault(stateForFrame, DEFAULT_IDLE_DURATION);
        int frameIndex;

        if (stateForFrame == AnimationState.IDLE || stateForFrame == AnimationState.CASTING) {
            // Looping animation
            float loopTime = stateTime % (frameDuration * currentFrames.length);
            frameIndex = (int) (loopTime / frameDuration);
        } else {
            // One-shot animation
            frameIndex = (int) (stateTime / frameDuration);
        }

        // Clamp to valid range
        frameIndex = Math.min(frameIndex, currentFrames.length - 1);
        frameIndex = Math.max(frameIndex, 0);

        return currentFrames[frameIndex];
    }

    /**
     * Returns true if the current one-shot animation has finished playing.
     * Always returns false for looping animations (IDLE).
     */
    public boolean isFinished() {
        if (currentState == AnimationState.DEAD) {
            return true;
        }
        if (currentState == AnimationState.IDLE || currentState == AnimationState.CASTING) {
            return false; // Looping animations never finish
        }

        TextureRegion[] currentFrames = frames.get(currentState);
        if (currentFrames == null || currentFrames.length == 0) {
            return true;
        }

        float frameDuration = frameDurations.getOrDefault(currentState, DEFAULT_IDLE_DURATION);
        float totalDuration = frameDuration * currentFrames.length;
        return stateTime >= totalDuration;
    }

    /**
     * Returns the current animation state.
     */
    public AnimationState getCurrentState() {
        return currentState;
    }

    /**
     * Returns the elapsed time in the current state.
     */
    public float getStateTime() {
        return stateTime;
    }

    /**
     * Resets the animator to IDLE state.
     */
    public void reset() {
        currentState = AnimationState.IDLE;
        stateTime = 0f;
    }
}
