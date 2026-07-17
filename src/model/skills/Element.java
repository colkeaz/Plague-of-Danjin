package model.skills;

/**
 * Represents elemental types for attacks and character affinities.
 * Used to compute damage multipliers based on elemental interactions.
 */
public enum Element {
    FIRE,
    HOLY,
    DARK,
    PHYSICAL,
    POISON;

    /**
     * Returns the damage multiplier when an attack of attackElement hits
     * a defender with defenseElement.
     *
     * Multiplier table:
     *   FIRE vs DARK: 2.0x (fire burns undead)
     *   HOLY vs DARK: 2.0x (holy smites undead)
     *   POISON vs PHYSICAL: 1.5x (poison is effective against the living)
     *   DARK vs HOLY: 1.5x (dark corrupts holy)
     *   Same element: 0.5x (resistance)
     *   All other combinations: 1.0x (neutral)
     */
    public static double getMultiplier(Element attackElement, Element defenseElement) {
        if (attackElement == defenseElement) {
            return 0.5;
        }

        if (attackElement == FIRE && defenseElement == DARK) {
            return 2.0;
        }
        if (attackElement == HOLY && defenseElement == DARK) {
            return 2.0;
        }
        if (attackElement == POISON && defenseElement == PHYSICAL) {
            return 1.5;
        }
        if (attackElement == DARK && defenseElement == HOLY) {
            return 1.5;
        }

        return 1.0;
    }
}
