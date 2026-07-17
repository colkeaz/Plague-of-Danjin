package model.skills;

/**
 * Represents the type of effect a skill has when used.
 */
public enum SkillEffect {
    DAMAGE,
    HEAL,
    BUFF_DEF,
    BUFF_ATK,
    STUN,
    MULTI_TARGET,
    /** Deals damage and heals caster for 50% of damage dealt. */
    DRAIN_LIFE,
    /** Deals 6x if target below 30% HP, else 2x. */
    ASSASSINATE,
    /** Costs HP instead of mana. */
    BLOOD_PACT,
    /** Applies SHIELD status blocking next 2 attacks. */
    DIVINE_SHIELD,
    /** Applies ENRAGE: +100% ATK, -50% DEF for 3 turns. */
    BERSERKER_RAGE,
    /** Sets a passive auto-revive flag (one-time, triggers on death). */
    RESURRECTION
}
