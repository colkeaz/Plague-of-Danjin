package controller;

/**
 * Manages QTE state and timing. Tracks current pattern, key progress,
 * mash count, elapsed time, completion status, and success/failure result.
 * Non-blocking: call update(delta) each frame and processKeyPress(keycode) on input.
 */
public class QTEManager {
    private final QTEPattern pattern;
    private int currentKeyIndex;
    private int mashCount;
    private float elapsedTime;
    private boolean completed;
    private boolean success;

    // Penalty for wrong key presses (seconds subtracted from remaining time)
    private static final float WRONG_KEY_PENALTY = 0.3f;

    public QTEManager(QTEPattern pattern) {
        this.pattern = pattern;
        this.currentKeyIndex = 0;
        this.mashCount = 0;
        this.elapsedTime = 0f;
        this.completed = false;
        this.success = false;
    }

    /**
     * Starts/resets the QTE. Called when the QTE screen is shown.
     */
    public void start() {
        this.currentKeyIndex = 0;
        this.mashCount = 0;
        this.elapsedTime = 0f;
        this.completed = false;
        this.success = false;
    }

    /**
     * Updates the QTE timer. Should be called every frame.
     *
     * @param delta time elapsed since last frame in seconds
     */
    public void update(float delta) {
        if (completed) return;

        elapsedTime += delta;

        // Check if time has run out
        if (elapsedTime >= pattern.getTimeLimit()) {
            completed = true;
            success = false;
        }
    }

    /**
     * Processes a key press during the QTE.
     *
     * @param keycode the key that was pressed
     * @return true if the key was correct, false if wrong
     */
    public boolean processKeyPress(int keycode) {
        if (completed) return false;

        if (pattern.getType() == QTEPattern.QTEType.MASH) {
            return processMashKey(keycode);
        } else {
            return processSequenceKey(keycode);
        }
    }

    private boolean processMashKey(int keycode) {
        // For MASH type, any press of the required key counts
        int requiredKey = pattern.getKeys()[0];
        if (keycode == requiredKey) {
            mashCount++;
            if (mashCount >= pattern.getRequiredMashCount()) {
                completed = true;
                success = true;
            }
            return true;
        } else {
            penalizeWrongKey();
            return false;
        }
    }

    private boolean processSequenceKey(int keycode) {
        // For SEQUENCE type, must press keys in order
        int[] keys = pattern.getKeys();
        if (currentKeyIndex < keys.length && keycode == keys[currentKeyIndex]) {
            currentKeyIndex++;
            if (currentKeyIndex >= keys.length) {
                completed = true;
                success = true;
            }
            return true;
        } else {
            penalizeWrongKey();
            return false;
        }
    }

    /**
     * Penalizes a wrong key press by accelerating the timer.
     * Subtracts 0.3 seconds from remaining time.
     */
    public void penalizeWrongKey() {
        elapsedTime += WRONG_KEY_PENALTY;
        if (elapsedTime >= pattern.getTimeLimit()) {
            completed = true;
            success = false;
        }
    }

    /**
     * Returns whether the QTE is complete (either succeeded or failed).
     */
    public boolean isComplete() {
        return completed;
    }

    /**
     * Returns whether the QTE was completed successfully.
     * Only valid when isComplete() returns true.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the progress as a fraction (0.0 to 1.0).
     * For MASH: mashCount / requiredCount.
     * For SEQUENCE: currentKeyIndex / totalKeys.
     */
    public float getProgress() {
        if (pattern.getType() == QTEPattern.QTEType.MASH) {
            return (float) mashCount / pattern.getRequiredMashCount();
        } else {
            return (float) currentKeyIndex / pattern.getKeys().length;
        }
    }

    /**
     * Returns the time remaining in seconds.
     */
    public float getTimeRemaining() {
        float remaining = pattern.getTimeLimit() - elapsedTime;
        return Math.max(0f, remaining);
    }

    /**
     * Returns the time fraction remaining (1.0 = full, 0.0 = expired).
     */
    public float getTimeFraction() {
        return Math.max(0f, 1f - (elapsedTime / pattern.getTimeLimit()));
    }

    /**
     * Returns the required keys array from the pattern.
     */
    public int[] getRequiredKeys() {
        return pattern.getKeys();
    }

    /**
     * Returns the current key index for SEQUENCE type.
     */
    public int getCurrentKeyIndex() {
        return currentKeyIndex;
    }

    /**
     * Returns the current mash count for MASH type.
     */
    public int getMashCount() {
        return mashCount;
    }

    /**
     * Returns the underlying QTE pattern.
     */
    public QTEPattern getPattern() {
        return pattern;
    }
}
