package model;

/**
 * Enum representing the three playable character classes.
 * Each class defines starting stats, display name, and description.
 */
public enum CharacterClass {
    KNIGHT(130, 35, 25, 60, 60,
            "Knight",
            "The classic frontliner. High survivability, strong basic attacks, limited magic."),

    MAGE(70, 15, 8, 100, 120,
            "Mage",
            "Devastating elemental damage dealer. High mana pool, powerful spells, but fragile."),

    ROGUE(85, 28, 10, 80, 80,
            "Rogue",
            "The agile assassin. High crit chance, poison synergy, dodge mechanics.");

    private final int startingHp;
    private final int startingAtk;
    private final int startingDef;
    private final int startingMp;
    private final int startingMaxMp;
    private final String displayName;
    private final String description;

    CharacterClass(int startingHp, int startingAtk, int startingDef,
                   int startingMp, int startingMaxMp,
                   String displayName, String description) {
        this.startingHp = startingHp;
        this.startingAtk = startingAtk;
        this.startingDef = startingDef;
        this.startingMp = startingMp;
        this.startingMaxMp = startingMaxMp;
        this.displayName = displayName;
        this.description = description;
    }

    public int getStartingHp() { return startingHp; }
    public int getStartingAtk() { return startingAtk; }
    public int getStartingDef() { return startingDef; }
    public int getStartingMp() { return startingMp; }
    public int getStartingMaxMp() { return startingMaxMp; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
