package model;

import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;

public abstract class GameCharacter extends GameEventDispatcher {
    private String name;
    private int hp;
    private int maxHp;
    private int attackPower;
    private int defense;
    private int mana;
    private int maxMana;

    public GameCharacter(String name, int maxHp, int attackPower, int defense) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attackPower = attackPower;
        this.defense = defense;

        this.maxMana = 100;
        this.mana = 75;
    }

    public abstract void attack(GameCharacter target);

    // Getters
    public String getName() { return name; }

    public int getHp() { return hp; }

    public int getMaxHp() { return maxHp; }

    public int getAttackPower() { return attackPower; }

    public int getDefense() { return defense; }

    public int getMana() { return mana; }

    public int getMaxMana() { return maxMana; }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public void takeDamage(int damage) {
        // 1. Calculate reduction (e.g., 15 becomes 0.15)
        double reductionPercentage = this.defense / 100.0;

        // 2. Calculate how much damage is blocked
        double blockedAmount = damage * reductionPercentage;

        // 3. Subtract blocked amount from incoming damage
        int finalDamage = (int)(damage - blockedAmount);

        // Safety check: Damage cannot be negative
        if (finalDamage < 0) finalDamage = 0;

        this.hp -= finalDamage;
        if (this.hp < 0) this.hp = 0;

        // Fire event instead of printing
        fireEvent(GameEvent.builder(GameEventType.DAMAGE_DEALT)
                .put("targetName", this.name)
                .put("rawDamage", damage)
                .put("blockedAmount", (int) blockedAmount)
                .put("finalDamage", finalDamage)
                .put("currentHp", this.hp)
                .put("maxHp", this.maxHp)
                .build());
    }

    public void heal(int amount) {
        this.hp += amount;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }

        fireEvent(GameEvent.builder(GameEventType.HEAL)
                .put("targetName", this.name)
                .put("amount", amount)
                .put("currentHp", this.hp)
                .put("maxHp", this.maxHp)
                .build());
    }

    public void upgradePower(int amount) {
        this.attackPower += amount;

        fireEvent(GameEvent.builder(GameEventType.POWER_UPGRADED)
                .put("characterName", this.name)
                .put("amount", amount)
                .put("currentAttack", this.attackPower)
                .build());
    }

    public void upgradeDefense(int amount) {
        this.defense += amount;
        // Cap defense at 75 so they don't become invincible
        if (this.defense > 75) this.defense = 75;

        fireEvent(GameEvent.builder(GameEventType.DEFENSE_UPGRADED)
                .put("characterName", this.name)
                .put("amount", amount)
                .put("currentDefense", this.defense)
                .build());
    }

    public boolean spendMana(int cost) {
        if (this.mana >= cost) {
            this.mana -= cost;
            fireEvent(GameEvent.builder(GameEventType.MANA_SPENT)
                    .put("characterName", this.name)
                    .put("cost", cost)
                    .put("currentMana", this.mana)
                    .put("maxMana", this.maxMana)
                    .build());
            return true;
        }

        fireEvent(GameEvent.builder(GameEventType.MANA_INSUFFICIENT)
                .put("characterName", this.name)
                .put("cost", cost)
                .put("currentMana", this.mana)
                .build());
        return false;
    }

    public void regenMana(int amount) {
        this.mana += amount;
        if (this.mana > maxMana) this.mana = maxMana;

        fireEvent(GameEvent.builder(GameEventType.MANA_REGEN)
                .put("characterName", this.name)
                .put("amount", amount)
                .put("currentMana", this.mana)
                .put("maxMana", this.maxMana)
                .build());
    }
}
