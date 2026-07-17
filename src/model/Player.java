package model;

import java.util.Random;

import model.events.GameEvent;
import model.events.GameEventType;
import model.items.Inventory;
import model.items.ItemEffect;
import model.skills.Element;
import model.skills.Skill;
import model.skills.SkillTree;
import model.status.StatusEffect;
import model.status.StatusType;

public class Player extends GameCharacter {
    private final Random rand = new Random();
    private final Inventory inventory;
    private final SkillTree skillTree;
    private boolean autoReviveActive = false;

    public Player(String name) {
        // Start with fixed stats: 100 HP, 30 Attack, 15 Defense
        super(name, 100, 30, 15);
        setElement(Element.PHYSICAL);
        this.inventory = new Inventory();
        this.skillTree = new SkillTree();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public SkillTree getSkillTree() {
        return skillTree;
    }

    /**
     * Returns whether the auto-revive passive (from Resurrection skill) is active.
     */
    public boolean isAutoReviveActive() {
        return autoReviveActive;
    }

    /**
     * Sets the auto-revive flag. When true, the player will revive with 50% HP
     * on death (one-time use, consumed on trigger).
     */
    public void setAutoReviveActive(boolean active) {
        this.autoReviveActive = active;
    }

    /**
     * Returns total attack power including bonuses from equipped items.
     */
    public int getTotalAttackPower() {
        return getAttackPower() + inventory.getTotalStatBonus(ItemEffect.ATK);
    }

    /**
     * Execute a player action programmatically without any I/O.
     * Returns true if the action was successfully executed, false if it failed (e.g., insufficient mana).
     */
    public boolean executeAction(PlayerAction action, GameCharacter target) {
        switch (action) {
            case BASIC_ATTACK:
                performBasicAttack(target);
                return true;

            case FIREBALL:
                return executeSkillByIndex(1, target);

            case HOLY_LIGHT:
                return executeSkillByIndex(2, target);

            case IRON_WILL:
                return executeSkillByIndex(3, target);

            case USE_SKILL_1:
            case USE_SKILL_2:
            case USE_SKILL_3:
            case USE_SKILL_4:
            case USE_SKILL_5:
            case USE_SKILL_6:
                return executeSkillByIndex(action.getSkillIndex(), target);

            default:
                return false;
        }
    }

    /**
     * Executes a skill by its index in the skill tree.
     * Handles mana cost (with spell cost reduction), cooldown checks, and event firing.
     * Special case: Blood Pact costs 20 HP instead of mana.
     */
    private boolean executeSkillByIndex(int index, GameCharacter target) {
        Skill skill = skillTree.getSkillByIndex(index);
        if (skill == null) {
            return false;
        }

        if (!skill.isReady()) {
            fireEvent(GameEvent.builder(GameEventType.SKILL_ON_COOLDOWN)
                    .put("skillName", skill.getName())
                    .put("turnsRemaining", skill.getCurrentCooldown())
                    .build());
            return false;
        }

        // Blood Pact costs 20 HP instead of mana
        if (skill.getSkillEffect() == model.skills.SkillEffect.BLOOD_PACT) {
            if (getHp() <= 20) {
                // Not enough HP to pay the cost (would kill the player)
                return false;
            }
            takeDamage(20);
        } else {
            int spellCostReduction = inventory.getTotalStatBonus(ItemEffect.SPELL_COST_REDUCTION);
            int effectiveCost = skill.getEffectiveCost(spellCostReduction);

            if (effectiveCost > 0 && !spendMana(effectiveCost)) {
                return false;
            }
        }

        skill.use();

        fireEvent(GameEvent.builder(GameEventType.SPELL_CAST)
                .put("casterName", getName())
                .put("spellName", skill.getName())
                .put("manaCost", skill.getManaCost())
                .build());

        applySkillEffect(skill, target);
        return true;
    }

    /**
     * Applies the effect of a skill based on its SkillEffect type.
     */
    private void applySkillEffect(Skill skill, GameCharacter target) {
        switch (skill.getSkillEffect()) {
            case DAMAGE:
                int damage = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(damage, skill.getElement());
                break;

            case HEAL:
                this.heal(30);
                break;

            case BUFF_DEF:
                this.upgradeDefense(5);
                break;

            case BUFF_ATK:
                this.upgradePower(5);
                break;

            case STUN:
                int stunDamage = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(stunDamage, skill.getElement());
                // Apply STUN status for 1 turn
                target.getStatusManager().addEffect(
                        new StatusEffect(StatusType.STUN, 1, 0, getName()));
                break;

            case MULTI_TARGET:
                int multiDamage = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(multiDamage, skill.getElement());
                break;

            case DRAIN_LIFE:
                int drainDamage = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(drainDamage, skill.getElement());
                // Heal caster for 50% of damage dealt
                int healAmount = drainDamage / 2;
                if (healAmount > 0) {
                    this.heal(healAmount);
                }
                break;

            case ASSASSINATE:
                // 6x if enemy below 30% HP, else 2x
                float multiplier;
                if (target.getHp() < target.getMaxHp() * 0.3) {
                    multiplier = 6.0f;
                } else {
                    multiplier = 2.0f;
                }
                int assassinateDamage = (int)(getTotalAttackPower() * multiplier);
                target.takeDamage(assassinateDamage, skill.getElement());
                break;

            case BLOOD_PACT:
                // HP cost already deducted in executeSkillByIndex; just deal 5x damage
                int bloodDamage = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(bloodDamage, skill.getElement());
                break;

            case DIVINE_SHIELD:
                // Apply SHIELD status that blocks next 2 attacks
                StatusEffect shield = new StatusEffect(StatusType.SHIELD, 2, 0, getName());
                this.getStatusManager().addEffect(shield);
                break;

            case BERSERKER_RAGE:
                // Apply ENRAGE status: +100% ATK, -50% DEF for 3 turns
                StatusEffect enrage = new StatusEffect(StatusType.ENRAGE, 3, 0, getName());
                this.getStatusManager().addEffect(enrage);
                break;

            case RESURRECTION:
                // Set passive auto-revive flag (one-time, triggers on death)
                this.autoReviveActive = true;
                fireEvent(GameEvent.builder(GameEventType.SKILL_UNLOCKED)
                        .put("skillName", "Resurrection")
                        .put("skillId", "resurrection_passive")
                        .put("element", Element.HOLY.name())
                        .build());
                break;
        }
    }

    private void performBasicAttack(GameCharacter target) {
        int damageDealt = getTotalAttackPower();

        // Determine element: check inventory for element override, else PHYSICAL
        Element attackElement = inventory.getElementOverride();
        if (attackElement == null) {
            attackElement = Element.PHYSICAL;
        }

        // Crit chance: base 15% + bonus from equipped items
        int critChance = 15 + inventory.getTotalStatBonus(ItemEffect.CRIT_CHANCE);
        boolean isCrit = rand.nextInt(100) < critChance;
        if (isCrit) {
            damageDealt *= 2;
            fireEvent(GameEvent.builder(GameEventType.CRITICAL_HIT)
                    .put("attackerName", getName())
                    .put("damage", damageDealt)
                    .build());
        }

        fireEvent(GameEvent.builder(GameEventType.PLAYER_BASIC_ATTACK)
                .put("attackerName", getName())
                .put("damage", damageDealt)
                .put("targetName", target.getName())
                .put("isCritical", isCrit)
                .put("element", attackElement.name())
                .build());

        target.takeDamage(damageDealt, attackElement);

        // Lifesteal: heal percentage of damage dealt
        if (inventory.hasLifesteal()) {
            int lifestealAmount = damageDealt * inventory.getLifestealPercent() / 100;
            if (lifestealAmount > 0) {
                this.heal(lifestealAmount);
            }
        }
    }

    /**
     * Default attack() implementation calls executeAction with BASIC_ATTACK
     * for AI compatibility and polymorphism support.
     * Note: Mana regen is the controller's responsibility (CombatEngine.processPlayerAction),
     * not the model's. This method does not regen mana.
     */
    @Override
    public void attack(GameCharacter target) {
        executeAction(PlayerAction.BASIC_ATTACK, target);
    }
}
