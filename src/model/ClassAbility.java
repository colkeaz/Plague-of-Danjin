package model;

/**
 * Enum representing class-specific passive abilities that are always active.
 * Each class has exactly one passive ability.
 */
public enum ClassAbility {
    /** Knight: Takes 10% less damage from all sources (applied after defense calculation). */
    THICK_SKIN("Thick Skin",
            "Takes 10% less damage from all sources.",
            "Knight"),

    /** Mage: All spell costs reduced by 3 MP, mana regen is 15/turn instead of 10. */
    ARCANE_AFFINITY("Arcane Affinity",
            "All spell costs reduced by 3 MP. Mana regen +5/turn.",
            "Mage"),

    /** Rogue: Base crit chance is 25% instead of 15%, crit damage is 2.5x instead of 2x. */
    KEEN_EDGE("Keen Edge",
            "Base crit chance is 25%. Crit damage is 2.5x.",
            "Rogue");

    private final String displayName;
    private final String description;
    private final String className;

    ClassAbility(String displayName, String description, String className) {
        this.displayName = displayName;
        this.description = description;
        this.className = className;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getClassName() { return className; }

    /**
     * Returns the ClassAbility for the given CharacterClass, or null if class is null.
     */
    public static ClassAbility forClass(CharacterClass characterClass) {
        if (characterClass == null) return null;
        switch (characterClass) {
            case KNIGHT: return THICK_SKIN;
            case MAGE: return ARCANE_AFFINITY;
            case ROGUE: return KEEN_EDGE;
            default: return null;
        }
    }
}
