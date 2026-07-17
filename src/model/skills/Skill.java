package model.skills;

/**
 * Represents a skill that a player can use in combat.
 * Tracks cooldown state and provides cost calculation with spell cost reduction.
 */
public class Skill {
    private final String id;
    private final String name;
    private final int manaCost;
    private final int cooldownTurns;
    private int currentCooldown;
    private final Element element;
    private final float damageMultiplier;
    private final SkillEffect skillEffect;

    public Skill(String id, String name, int manaCost, int cooldownTurns,
                 Element element, float damageMultiplier, SkillEffect skillEffect) {
        this.id = id;
        this.name = name;
        this.manaCost = manaCost;
        this.cooldownTurns = cooldownTurns;
        this.currentCooldown = 0;
        this.element = element;
        this.damageMultiplier = damageMultiplier;
        this.skillEffect = skillEffect;
    }

    /**
     * Creates a copy of this skill for player-specific cooldown tracking.
     */
    public Skill copy() {
        return new Skill(id, name, manaCost, cooldownTurns, element, damageMultiplier, skillEffect);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getCooldownTurns() {
        return cooldownTurns;
    }

    public int getCurrentCooldown() {
        return currentCooldown;
    }

    public Element getElement() {
        return element;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public SkillEffect getSkillEffect() {
        return skillEffect;
    }

    /**
     * Returns true if the skill is off cooldown and ready to use.
     */
    public boolean isReady() {
        return currentCooldown == 0;
    }

    /**
     * Puts the skill on cooldown after use.
     */
    public void use() {
        this.currentCooldown = this.cooldownTurns;
    }

    /**
     * Decrements cooldown by 1 if it is greater than 0.
     */
    public void tickCooldown() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }

    /**
     * Calculates effective mana cost after applying spell cost reduction from items.
     * Returns at minimum 0.
     */
    public int getEffectiveCost(int spellCostReduction) {
        int effective = manaCost - spellCostReduction;
        return Math.max(0, effective);
    }
}
