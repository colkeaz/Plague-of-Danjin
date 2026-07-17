package model;

/**
 * Represents available player actions in combat.
 * The original 4 actions are retained for backward compatibility.
 * USE_SKILL_1 through USE_SKILL_6 map to unlockable skill slots
 * beyond the 4 default skills.
 */
public enum PlayerAction {
    BASIC_ATTACK,
    FIREBALL,
    HOLY_LIGHT,
    IRON_WILL,
    USE_SKILL_1,
    USE_SKILL_2,
    USE_SKILL_3,
    USE_SKILL_4,
    USE_SKILL_5,
    USE_SKILL_6;

    /**
     * Returns the skill index for USE_SKILL_N actions.
     * USE_SKILL_1 maps to index 4 (first unlocked slot after 4 defaults),
     * USE_SKILL_2 to index 5, etc.
     * Returns -1 for non-skill actions.
     */
    public int getSkillIndex() {
        switch (this) {
            case BASIC_ATTACK: return 0;
            case FIREBALL: return 1;
            case HOLY_LIGHT: return 2;
            case IRON_WILL: return 3;
            case USE_SKILL_1: return 4;
            case USE_SKILL_2: return 5;
            case USE_SKILL_3: return 6;
            case USE_SKILL_4: return 7;
            case USE_SKILL_5: return 8;
            case USE_SKILL_6: return 9;
            default: return -1;
        }
    }

    /**
     * Returns true if this action maps to a skill in the SkillTree.
     */
    public boolean isSkillAction() {
        return true; // All actions in this enum map to skills
    }
}
