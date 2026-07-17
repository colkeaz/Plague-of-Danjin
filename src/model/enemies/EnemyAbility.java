package model.enemies;

import model.skills.Element;

/**
 * Immutable data object representing a powerful enemy attack that is
 * telegraphed 1 turn in advance before execution.
 */
public class EnemyAbility {
    private final String name;
    private final float damageMultiplier;
    private final Element element;
    private final String telegraphMessage;

    public EnemyAbility(String name, float damageMultiplier, Element element, String telegraphMessage) {
        this.name = name;
        this.damageMultiplier = damageMultiplier;
        this.element = element;
        this.telegraphMessage = telegraphMessage;
    }

    public String getName() {
        return name;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public Element getElement() {
        return element;
    }

    public String getTelegraphMessage() {
        return telegraphMessage;
    }
}
