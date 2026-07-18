package controller;

import com.badlogic.gdx.Input;

/**
 * Defines a QTE (Quick-Time Event) sequence configuration.
 * Each pattern has a type (MASH or SEQUENCE), key sequence, time limit,
 * required mash count (for MASH type), and descriptions of success/failure effects.
 */
public class QTEPattern {

    public enum QTEType {
        MASH,
        SEQUENCE
    }

    private final QTEType type;
    private final int[] keys;
    private final int requiredMashCount;
    private final float timeLimit;
    private final String successEffect;
    private final String failureEffect;
    private final String bossName;
    private final String thresholdId;

    /**
     * Creates a QTEPattern.
     *
     * @param type            MASH or SEQUENCE
     * @param keys            Key sequence (for SEQUENCE) or single key repeated (for MASH)
     * @param requiredMashCount Number of presses required for MASH type (ignored for SEQUENCE)
     * @param timeLimit       Time limit in seconds
     * @param successEffect   Description of success effect
     * @param failureEffect   Description of failure effect
     * @param bossName        Name of the boss this QTE is for
     * @param thresholdId     Unique identifier for this threshold (e.g., "GoblinChieftain_50")
     */
    public QTEPattern(QTEType type, int[] keys, int requiredMashCount, float timeLimit,
                      String successEffect, String failureEffect, String bossName, String thresholdId) {
        this.type = type;
        this.keys = keys;
        this.requiredMashCount = requiredMashCount;
        this.timeLimit = timeLimit;
        this.successEffect = successEffect;
        this.failureEffect = failureEffect;
        this.bossName = bossName;
        this.thresholdId = thresholdId;
    }

    public QTEType getType() {
        return type;
    }

    public int[] getKeys() {
        return keys;
    }

    public int getRequiredMashCount() {
        return requiredMashCount;
    }

    public float getTimeLimit() {
        return timeLimit;
    }

    public String getSuccessEffect() {
        return successEffect;
    }

    public String getFailureEffect() {
        return failureEffect;
    }

    public String getBossName() {
        return bossName;
    }

    public String getThresholdId() {
        return thresholdId;
    }

    /**
     * Returns a human-readable name for a key code.
     */
    public static String getKeyName(int keycode) {
        switch (keycode) {
            case Input.Keys.LEFT: return "LEFT";
            case Input.Keys.RIGHT: return "RIGHT";
            case Input.Keys.UP: return "UP";
            case Input.Keys.DOWN: return "DOWN";
            case Input.Keys.SPACE: return "SPACE";
            case Input.Keys.A: return "A";
            case Input.Keys.D: return "D";
            default: return Input.Keys.toString(keycode);
        }
    }

    // --- Factory methods for all boss QTE patterns ---

    /**
     * GoblinChieftain at 50% HP (60 HP threshold).
     * MASH 1 key 8 times in 3 seconds.
     * Success: stun 2 turns. Failure: +10 ATK.
     */
    public static QTEPattern goblinChieftain50() {
        return new QTEPattern(
                QTEType.MASH,
                new int[]{Input.Keys.SPACE},
                8,
                3.0f,
                "Boss stunned for 2 turns!",
                "Boss ATK increased by 10!",
                "Goblin Chieftain",
                "GoblinChieftain_50"
        );
    }

    /**
     * GoblinKing at 50% HP (75 HP threshold).
     * SEQUENCE [LEFT, UP, RIGHT, DOWN] in 2.5 seconds.
     * Success: deal 3x damage. Failure: heal 50.
     */
    public static QTEPattern goblinKing50() {
        return new QTEPattern(
                QTEType.SEQUENCE,
                new int[]{Input.Keys.LEFT, Input.Keys.UP, Input.Keys.RIGHT, Input.Keys.DOWN},
                0,
                2.5f,
                "Critical strike! 3x damage dealt!",
                "Boss heals 50 HP!",
                "Goblin King",
                "GoblinKing_50"
        );
    }

    /**
     * GoblinKing at 25% HP (37 HP threshold).
     * SEQUENCE [UP, RIGHT, DOWN, LEFT, UP] in 2 seconds.
     * Success: -10 ATK permanent. Failure: +20 ATK.
     */
    public static QTEPattern goblinKing25() {
        return new QTEPattern(
                QTEType.SEQUENCE,
                new int[]{Input.Keys.UP, Input.Keys.RIGHT, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.UP},
                0,
                2.0f,
                "Boss weakened! -10 ATK permanently!",
                "Boss enraged! +20 ATK!",
                "Goblin King",
                "GoblinKing_25"
        );
    }

    /**
     * BoneColossus at 50% HP (100 HP threshold).
     * SEQUENCE [A, D, A, D, A, D] in 3 seconds.
     * Success: -15 DEF. Failure: reflect 30 damage.
     */
    public static QTEPattern boneColossus50() {
        return new QTEPattern(
                QTEType.SEQUENCE,
                new int[]{Input.Keys.A, Input.Keys.D, Input.Keys.A, Input.Keys.D, Input.Keys.A, Input.Keys.D},
                0,
                3.0f,
                "Armor shattered! -15 DEF!",
                "Damage reflected! 30 damage to player!",
                "Bone Colossus",
                "BoneColossus_50"
        );
    }

    /**
     * Lich at 75% HP (225 HP threshold).
     * SEQUENCE [UP, UP, RIGHT, DOWN, LEFT] in 2 seconds.
     * Success: kill all minions. Failure: summon 3 minions.
     */
    public static QTEPattern lich75() {
        return new QTEPattern(
                QTEType.SEQUENCE,
                new int[]{Input.Keys.UP, Input.Keys.UP, Input.Keys.RIGHT, Input.Keys.DOWN, Input.Keys.LEFT},
                0,
                2.0f,
                "Minions banished!",
                "3 minions summoned!",
                "The Necromancer Lich",
                "Lich_75"
        );
    }

    /**
     * Lich at 50% HP (150 HP threshold).
     * MASH SPACE 12 times in 2.5 seconds.
     * Success: -50% ATK permanent. Failure: 80 damage to player.
     */
    public static QTEPattern lich50() {
        return new QTEPattern(
                QTEType.MASH,
                new int[]{Input.Keys.SPACE},
                12,
                2.5f,
                "Boss power drained! -50% ATK!",
                "Dark energy blast! 80 damage!",
                "The Necromancer Lich",
                "Lich_50"
        );
    }

    /**
     * Lich at 25% HP (75 HP threshold).
     * SEQUENCE [LEFT, DOWN, RIGHT, UP, UP, RIGHT] in 2 seconds.
     * Success: prevent healing for rest of fight. Failure: heal to 50%.
     */
    public static QTEPattern lich25() {
        return new QTEPattern(
                QTEType.SEQUENCE,
                new int[]{Input.Keys.LEFT, Input.Keys.DOWN, Input.Keys.RIGHT, Input.Keys.UP, Input.Keys.UP, Input.Keys.RIGHT},
                0,
                2.0f,
                "Healing sealed! No more recovery!",
                "Boss regenerates to 50% HP!",
                "The Necromancer Lich",
                "Lich_25"
        );
    }
}
