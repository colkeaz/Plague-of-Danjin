package model;

import java.util.Arrays;
import java.util.Random;

import model.events.GameEvent;
import model.events.GameEventType;
import model.items.Inventory;
import model.items.Item;
import model.items.ItemEffect;
import model.items.ItemRarity;
import model.items.ItemSlot;
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
    private final CharacterClass characterClass;
    private final ClassAbility classAbility;
    private boolean dodgeNextAttack = false;
    private boolean guaranteeCrit = false;
    private boolean immortalStandUsed = false;
    private int spellCostReductionBonus = 0;
    private boolean plagueBearerActive = false;

    public Player(String name) {
        // Start with fixed stats: 100 HP, 30 Attack, 15 Defense
        super(name, 100, 30, 15);
        setElement(Element.PHYSICAL);
        this.inventory = new Inventory();
        this.skillTree = new SkillTree();
        this.characterClass = null;
        this.classAbility = null;
    }

    /**
     * Class-based constructor. Applies class starting stats, skills, passives, and equipment.
     */
    public Player(String name, CharacterClass characterClass) {
        super(name, characterClass.getStartingHp(), characterClass.getStartingAtk(), characterClass.getStartingDef());
        setElement(Element.PHYSICAL);
        this.inventory = new Inventory();
        this.characterClass = characterClass;
        this.classAbility = ClassAbility.forClass(characterClass);
        this.skillTree = new SkillTree(characterClass);

        // Set mana based on class (override GameCharacter default of 100/75)
        setMaxManaRaw(characterClass.getStartingMaxMp());
        setManaRaw(characterClass.getStartingMp());

        // Apply class passive
        applyClassPassive();

        // Equip class starting items
        equipClassStartingItems();
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
     * Returns the CharacterClass, or null for legacy players.
     */
    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    /**
     * Returns the class passive ability, or null for legacy players.
     */
    public ClassAbility getClassAbility() {
        return classAbility;
    }

    /**
     * Returns true if the player can dodge the next attack.
     */
    public boolean isDodgeNextAttack() {
        return dodgeNextAttack;
    }

    /**
     * Sets the dodge-next-attack flag.
     */
    public void setDodgeNextAttack(boolean dodgeNextAttack) {
        this.dodgeNextAttack = dodgeNextAttack;
    }

    /**
     * Returns true if the next attack is guaranteed to crit.
     */
    public boolean isGuaranteeCrit() {
        return guaranteeCrit;
    }

    /**
     * Sets the guarantee-crit flag.
     */
    public void setGuaranteeCrit(boolean guaranteeCrit) {
        this.guaranteeCrit = guaranteeCrit;
    }

    /**
     * Returns true if Immortal Stand has already been used this fight.
     */
    public boolean isImmortalStandUsed() {
        return immortalStandUsed;
    }

    /**
     * Sets whether Immortal Stand has been used.
     */
    public void setImmortalStandUsed(boolean used) {
        this.immortalStandUsed = used;
    }

    /**
     * Returns the permanent spell cost reduction bonus (from Arcane Mastery).
     */
    public int getSpellCostReductionBonus() {
        return spellCostReductionBonus;
    }

    /**
     * Returns whether Plague Bearer passive is active (double poison damage).
     */
    public boolean isPlagueBearerActive() {
        return plagueBearerActive;
    }

    /**
     * Applies class passive based on the ClassAbility.
     */
    private void applyClassPassive() {
        if (classAbility == null) return;
        switch (classAbility) {
            case KEEN_EDGE:
                setBaseCritChance(0.25f);
                setCritDamageMultiplier(2.5f);
                break;
            case THICK_SKIN:
            case ARCANE_AFFINITY:
                // These are applied at damage time / mana regen time in CombatEngine
                break;
        }
    }

    /**
     * Equips class-specific starting items.
     */
    private void equipClassStartingItems() {
        if (characterClass == null) return;
        switch (characterClass) {
            case KNIGHT:
                inventory.equip(new Item(
                        "Knight's Longsword", "A sturdy blade forged for battle.",
                        ItemRarity.COMMON, ItemSlot.WEAPON,
                        Arrays.asList(new ItemEffect(ItemEffect.ATK, 10))));
                inventory.equip(new Item(
                        "Plate Mail", "Heavy armor that provides excellent protection.",
                        ItemRarity.COMMON, ItemSlot.ARMOR,
                        Arrays.asList(
                                new ItemEffect(ItemEffect.DEF, 12),
                                new ItemEffect(ItemEffect.MAX_HP, 20))));
                break;
            case MAGE:
                inventory.equip(new Item(
                        "Apprentice Staff", "A staff imbued with fire magic.",
                        ItemRarity.COMMON, ItemSlot.WEAPON,
                        Arrays.asList(
                                new ItemEffect(ItemEffect.ATK, 3, Element.FIRE),
                                new ItemEffect(ItemEffect.MAX_MP, 10))));
                inventory.equip(new Item(
                        "Enchanted Robe", "Robes woven with mana-enhancing threads.",
                        ItemRarity.COMMON, ItemSlot.ARMOR,
                        Arrays.asList(
                                new ItemEffect(ItemEffect.DEF, 5),
                                new ItemEffect(ItemEffect.MAX_MP, 20))));
                break;
            case ROGUE:
                inventory.equip(new Item(
                        "Poison Dagger", "A dagger coated in deadly venom.",
                        ItemRarity.COMMON, ItemSlot.WEAPON,
                        Arrays.asList(
                                new ItemEffect(ItemEffect.ATK, 8, Element.POISON),
                                new ItemEffect(ItemEffect.CRIT_CHANCE, 5))));
                inventory.equip(new Item(
                        "Shadow Cloak", "A dark cloak that helps strike unseen.",
                        ItemRarity.COMMON, ItemSlot.ARMOR,
                        Arrays.asList(
                                new ItemEffect(ItemEffect.DEF, 5),
                                new ItemEffect(ItemEffect.CRIT_CHANCE, 10))));
                break;
        }
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
     * Special case: HP_COST_DAMAGE (Dragon Slayer) costs 30 HP instead of mana.
     * Special case: MANA_SINGULARITY consumes all remaining MP.
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
                return false;
            }
            takeDamage(20);
        } else if (skill.getSkillEffect() == model.skills.SkillEffect.HP_COST_DAMAGE) {
            // Dragon Slayer costs 30 HP
            if (getHp() <= 30) {
                return false;
            }
            takeDamage(30);
        } else if (skill.getSkillEffect() == model.skills.SkillEffect.SACRIFICE_BUFF) {
            // Warlord's Fury costs 30 HP
            if (getHp() <= 30) {
                return false;
            }
            takeDamage(30);
        } else if (skill.getSkillEffect() == model.skills.SkillEffect.MANA_SINGULARITY) {
            // Mana Singularity consumes all MP - no mana cost check needed, just need some MP
            if (getMana() <= 0) {
                return false;
            }
            // MP consumption happens in applySkillEffect
        } else {
            int spellCostReduction = inventory.getTotalStatBonus(ItemEffect.SPELL_COST_REDUCTION);
            // Apply Arcane Affinity passive: -3 MP to all spell costs
            if (classAbility == ClassAbility.ARCANE_AFFINITY) {
                spellCostReduction += 3;
            }
            // Apply permanent cost reduction from Arcane Mastery
            spellCostReduction += spellCostReductionBonus;

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

            // --- Class-specific skill effects ---

            case POISON_DAMAGE:
                int poisonDmg = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(poisonDmg, skill.getElement());
                int poisonPotency = plagueBearerActive ? 10 : 5;
                target.getStatusManager().addEffect(
                        new StatusEffect(StatusType.POISON, 3, poisonPotency, getName()));
                break;

            case DODGE_NEXT:
                // Dodge the next incoming attack and guarantee next attack is a crit
                this.dodgeNextAttack = true;
                this.guaranteeCrit = true;
                break;

            case CONDITIONAL_DAMAGE:
                // Backstab: 4x if target is POISONED, else 2x
                float backstabMult = target.getStatusManager().hasEffect(StatusType.POISON)
                        ? skill.getDamageMultiplier() : 2.0f;
                int backstabDmg = (int)(getTotalAttackPower() * backstabMult);
                target.takeDamage(backstabDmg, skill.getElement());
                break;

            case SELF_HEAL_CURE:
                // Rally: heal 20 HP and remove POISON from self
                this.heal(20);
                this.getStatusManager().removeEffect(StatusType.POISON);
                break;

            case BUFF_DEF_TEMP:
                // Fortress/Taunt: apply +10 DEF for 3 turns (via upgrade that persists)
                this.upgradeDefense(10);
                break;

            case SHIELD_SINGLE:
                // Arcane Shield: apply SHIELD that blocks 1 hit
                StatusEffect singleShield = new StatusEffect(StatusType.SHIELD, 1, 0, getName());
                this.getStatusManager().addEffect(singleShield);
                break;

            case STUN_MULTI_TARGET:
                // Frost Nova: damage + stun all (applied as multi-target in CombatEngine)
                int frostDmg = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(frostDmg, skill.getElement());
                target.getStatusManager().addEffect(
                        new StatusEffect(StatusType.STUN, 1, 0, getName()));
                break;

            case CLASS_ASSASSINATE:
                // 8x if enemy below 25% HP, else 2x
                float classAssassMult = target.getHp() < target.getMaxHp() * 0.25
                        ? skill.getDamageMultiplier() : 2.0f;
                int classAssassDmg = (int)(getTotalAttackPower() * classAssassMult);
                target.takeDamage(classAssassDmg, skill.getElement());
                break;

            case MANA_DRAIN:
                // Steal 20 MP from enemy (symbolically) and deal damage
                this.regenMana(20);
                int manaDrainDmg = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(manaDrainDmg, skill.getElement());
                break;

            case DRAIN_OVER_TIME:
                // Paladin's Oath: deal damage + apply REGEN to self for 3 turns
                int drainOTDmg = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(drainOTDmg, skill.getElement());
                this.getStatusManager().addEffect(
                        new StatusEffect(StatusType.REGEN, 3, drainOTDmg / 6, getName()));
                break;

            case INVULNERABLE:
                // Bulwark: apply SHIELD for 1 turn (blocks all attacks for 1 turn)
                StatusEffect invuln = new StatusEffect(StatusType.SHIELD, 1, 0, getName());
                this.getStatusManager().addEffect(invuln);
                break;

            case SACRIFICE_BUFF:
                // Warlord's Fury: HP cost already deducted, +50% ATK for 3 turns
                int atkBoost = getTotalAttackPower() / 2;
                this.upgradePower(atkBoost);
                break;

            case IMMORTAL_STAND:
                // Passive: when HP drops below 20%, auto-heal to 50% (once per fight)
                // This just activates the passive flag; actual trigger is in CombatEngine
                this.immortalStandUsed = false;
                this.autoReviveActive = true;
                break;

            case HP_COST_DAMAGE:
                // Dragon Slayer: HP cost already deducted, deal 5x damage
                int hpCostDmg = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(hpCostDmg, skill.getElement());
                break;

            case SHIELD_ALL:
                // Aegis of Light: apply SHIELD that blocks up to 50 damage
                StatusEffect aegis = new StatusEffect(StatusType.SHIELD, 3, 50, getName());
                this.getStatusManager().addEffect(aegis);
                break;

            case TIME_WARP:
                // Time Warp: the extra turn logic is handled by CombatEngine
                // For now, just mark the state (CombatEngine checks for this)
                break;

            case PASSIVE_COST_REDUCTION:
                // Arcane Mastery: permanently reduce all spell costs by 5
                this.spellCostReductionBonus += 5;
                break;

            case MANA_SINGULARITY:
                // Consume ALL remaining MP, deal damage equal to 5x MP consumed
                int currentMp = getMana();
                spendMana(currentMp);
                int singularityDmg = currentMp * 5;
                target.takeDamage(singularityDmg, skill.getElement());
                break;

            case POISON_MULTI_TARGET:
                // Fan of Knives: damage + POISON all (multi-target handled by CombatEngine)
                int fanDmg = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(fanDmg, skill.getElement());
                int fanPoisonPotency = plagueBearerActive ? 10 : 5;
                target.getStatusManager().addEffect(
                        new StatusEffect(StatusType.POISON, 3, fanPoisonPotency, getName()));
                break;

            case SMOKE_BOMB:
                // Dodge ALL attacks for 1 turn
                this.dodgeNextAttack = true;
                break;

            case DAMAGE_OVER_TIME:
                // Lacerate: deal initial damage + apply bleed (POISON-like DoT)
                int lacDmg = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(lacDmg, skill.getElement());
                target.getStatusManager().addEffect(
                        new StatusEffect(StatusType.POISON, 3, 10, getName()));
                break;

            case TOXIC_EXPLOSION:
                // Explode all poison on target for 3x remaining damage
                if (target.getStatusManager().hasEffect(StatusType.POISON)) {
                    int explosionDmg = (int)(getTotalAttackPower() * skill.getDamageMultiplier());
                    target.takeDamage(explosionDmg, skill.getElement());
                    target.getStatusManager().removeEffect(StatusType.POISON);
                } else {
                    // Reduced damage if no poison
                    int reducedDmg = (int)(getTotalAttackPower() * 1.0f);
                    target.takeDamage(reducedDmg, skill.getElement());
                }
                break;

            case SHADOW_CLONE:
                // Creates a clone that attacks for 50% ATK each turn for 3 turns
                // Implemented as a REGEN-like effect that deals damage via the status system
                // For simplicity, deal upfront burst of 3 turns worth of 50% ATK
                int cloneDmg = (int)(getTotalAttackPower() * 0.5f * 3);
                target.takeDamage(cloneDmg, skill.getElement());
                break;

            case DEATH_MARK:
                // Mark enemy: conceptually all damage +100% for 3 turns
                // Implemented as a STUN-like debuff placeholder + immediate burst
                int markDmg = (int)(getTotalAttackPower() * 2.0f);
                target.takeDamage(markDmg, skill.getElement());
                break;

            case VANISH:
                // Become invisible: dodge next attacks + guaranteed crit on next attack
                this.dodgeNextAttack = true;
                this.guaranteeCrit = true;
                break;

            case PLAGUE_BEARER:
                // Passive: all POISON effects deal double damage permanently
                this.plagueBearerActive = true;
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

        // Crit chance: use baseCritChance from GameCharacter + bonus from equipped items
        int critChancePercent = (int)(getBaseCritChance() * 100) + inventory.getTotalStatBonus(ItemEffect.CRIT_CHANCE);
        boolean isCrit = guaranteeCrit || rand.nextInt(100) < critChancePercent;
        if (isCrit) {
            damageDealt = (int)(damageDealt * getCritDamageMultiplier());
            guaranteeCrit = false; // consume the guarantee
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

    /**
     * Overrides takeDamage to apply Knight's THICK_SKIN passive:
     * 10% less damage from all sources after defense calculation.
     * Also handles dodge-next-attack flag.
     */
    @Override
    public void takeDamage(int damage) {
        // Check dodge
        if (dodgeNextAttack) {
            dodgeNextAttack = false;
            fireEvent(GameEvent.builder(GameEventType.SHIELD_BLOCKED)
                    .put("targetName", getName())
                    .put("blockedDamage", damage)
                    .build());
            return;
        }

        // Apply THICK_SKIN: reduce incoming damage by 10%
        int modifiedDamage = damage;
        if (classAbility == ClassAbility.THICK_SKIN) {
            modifiedDamage = (int)(damage * 0.9f);
        }
        super.takeDamage(modifiedDamage);

        // Check Immortal Stand trigger
        checkImmortalStand();
    }

    /**
     * Overrides elemental takeDamage to apply dodge and Thick Skin passive.
     */
    @Override
    public void takeDamage(int damage, Element attackElement) {
        // Check dodge
        if (dodgeNextAttack) {
            dodgeNextAttack = false;
            fireEvent(GameEvent.builder(GameEventType.SHIELD_BLOCKED)
                    .put("targetName", getName())
                    .put("blockedDamage", damage)
                    .build());
            return;
        }

        // Apply THICK_SKIN: reduce incoming damage by 10%
        int modifiedDamage = damage;
        if (classAbility == ClassAbility.THICK_SKIN) {
            modifiedDamage = (int)(damage * 0.9f);
        }
        super.takeDamage(modifiedDamage, attackElement);

        // Check Immortal Stand trigger
        checkImmortalStand();
    }

    /**
     * Checks and triggers Immortal Stand if HP dropped below 20%.
     */
    private void checkImmortalStand() {
        if (!immortalStandUsed && autoReviveActive && characterClass == CharacterClass.KNIGHT) {
            if (isAlive() && getHp() < getMaxHp() * 0.2) {
                immortalStandUsed = true;
                int healTo = getMaxHp() / 2;
                setHp(healTo);
            }
        }
    }
}
