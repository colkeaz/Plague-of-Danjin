package model.items;

import model.skills.Element;

/**
 * Represents a stat modifier on an item. Immutable data object.
 * The stat field uses string constants: ATK, DEF, MAX_HP, MAX_MP, CRIT_CHANCE, SPELL_COST_REDUCTION.
 * The elementOverride field is non-null only for weapons that change basic attack element.
 */
public class ItemEffect {
    public static final String ATK = "ATK";
    public static final String DEF = "DEF";
    public static final String MAX_HP = "MAX_HP";
    public static final String MAX_MP = "MAX_MP";
    public static final String CRIT_CHANCE = "CRIT_CHANCE";
    public static final String SPELL_COST_REDUCTION = "SPELL_COST_REDUCTION";

    private final String stat;
    private final int value;
    private final Element elementOverride;

    public ItemEffect(String stat, int value) {
        this(stat, value, null);
    }

    public ItemEffect(String stat, int value, Element elementOverride) {
        this.stat = stat;
        this.value = value;
        this.elementOverride = elementOverride;
    }

    public String getStat() {
        return stat;
    }

    public int getValue() {
        return value;
    }

    public Element getElementOverride() {
        return elementOverride;
    }
}
