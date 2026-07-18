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
    RESURRECTION,

    // --- Class-specific skill effects ---

    /** Deals damage and applies POISON status for 3 turns. */
    POISON_DAMAGE,
    /** Dodge next attack completely and guarantee next attack is a crit. */
    DODGE_NEXT,
    /** Deals 4x damage if target is POISONED, else 2x. (Backstab) */
    CONDITIONAL_DAMAGE,
    /** Heals self for a fixed amount and removes POISON from self. (Rally) */
    SELF_HEAL_CURE,
    /** Applies a temporary DEF buff via status effect. (Fortress) */
    BUFF_DEF_TEMP,
    /** Applies SHIELD status blocking 1 hit. (Arcane Shield) */
    SHIELD_SINGLE,
    /** Deals damage and stuns for 1 turn (multi-target version). */
    STUN_MULTI_TARGET,
    /** Deals 8x if target below 25% HP, else 2x. (Class Assassinate) */
    CLASS_ASSASSINATE,
    /** Drains MP from enemy and deals damage. */
    MANA_DRAIN,
    /** Heals caster for percentage of damage dealt over multiple turns. */
    DRAIN_OVER_TIME,
    /** Immunity to damage for 1 turn. */
    INVULNERABLE,
    /** Sacrifice HP to gain ATK buff. */
    SACRIFICE_BUFF,
    /** Auto-heal to 50% HP when below 20% (once per fight). */
    IMMORTAL_STAND,
    /** Costs HP instead of mana, deals massive single-target damage. */
    HP_COST_DAMAGE,
    /** Party-wide SHIELD that absorbs damage. */
    SHIELD_ALL,
    /** Take 2 turns in a row. */
    TIME_WARP,
    /** Passive: reduces all spell costs permanently. */
    PASSIVE_COST_REDUCTION,
    /** Consume all remaining MP, deal damage equal to 5x MP consumed. */
    MANA_SINGULARITY,
    /** Applies POISON to all enemies (multi-target poison). */
    POISON_MULTI_TARGET,
    /** Dodge all attacks for 1 turn. */
    SMOKE_BOMB,
    /** Deals damage over time (DoT) applied as a bleed/lacerate. */
    DAMAGE_OVER_TIME,
    /** Explodes all poison stacks on target for instant damage. */
    TOXIC_EXPLOSION,
    /** Creates a shadow clone that attacks each turn. */
    SHADOW_CLONE,
    /** Marks enemy: all damage taken +100% for duration. */
    DEATH_MARK,
    /** Become invisible: cannot be targeted, guaranteed crit on next attack. */
    VANISH,
    /** Passive: all POISON effects deal double damage permanently. */
    PLAGUE_BEARER
}
