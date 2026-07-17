package view.sprites;

/**
 * Represents the current animation state of an entity sprite.
 */
public enum AnimationState {
    /** Subtle breathing/bob animation, loops continuously. */
    IDLE,

    /** Weapon swing or lunge, plays once then returns to IDLE. */
    ATTACKING,

    /** Flash white or recoil, plays once then returns to IDLE. */
    HURT,

    /** Collapse/dissolve, plays once then transitions to DEAD. */
    DYING,

    /** Spell casting animation, plays once then returns to IDLE. */
    CASTING,

    /** Terminal state after DYING completes. Entity remains on last death frame. */
    DEAD
}
