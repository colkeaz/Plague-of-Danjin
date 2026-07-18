package model.status;

/**
 * Enum representing the types of status effects that can be applied to characters.
 */
public enum StatusType {
    /** Deals damage over time. Stackable up to 3 stacks. */
    POISON,

    /** Heals over time. */
    REGEN,

    /** Increases ATK by 50%, decreases DEF by 25%. */
    ENRAGE,

    /** Blocks the next damage instance completely, then expires. */
    SHIELD,

    /** Permanent max HP reduction (Danjin's Curse). */
    CURSE,

    /** Target skips their next turn entirely. */
    STUN,

    /** Temporary DEF buff. Potency = DEF amount added. Reverts on expiry. */
    DEF_BUFF,

    /** Temporary ATK buff. Potency = ATK amount added. Reverts on expiry. */
    ATK_BUFF
}
