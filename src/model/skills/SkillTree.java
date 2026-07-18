package model.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import model.CharacterClass;
import model.ClassSkillTree;
import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;

/**
 * Manages the player's skill repertoire and milestone unlock choices.
 * Default skills are always available. At waves 5, 10, and 15, the player
 * is offered 3 skill choices and picks 1 to unlock.
 */
public class SkillTree extends GameEventDispatcher {

    // Default skills (always available) - kept for backward compatibility
    public static final Skill BASIC_ATTACK = new Skill(
            "basic_attack", "Basic Attack", 0, 0,
            Element.PHYSICAL, 1.0f, SkillEffect.DAMAGE);

    public static final Skill FIREBALL = new Skill(
            "fireball", "Fireball", 20, 2,
            Element.FIRE, 3.0f, SkillEffect.DAMAGE);

    public static final Skill HOLY_LIGHT = new Skill(
            "holy_light", "Holy Light", 15, 0,
            Element.HOLY, 0.0f, SkillEffect.HEAL);

    public static final Skill IRON_WILL = new Skill(
            "iron_will", "Iron Will", 10, 3,
            Element.PHYSICAL, 0.0f, SkillEffect.BUFF_DEF);

    // Wave 5 milestone choices
    private static final Skill CHAIN_LIGHTNING = new Skill(
            "chain_lightning", "Chain Lightning", 15, 0,
            Element.FIRE, 1.5f, SkillEffect.MULTI_TARGET);

    private static final Skill SHIELD_BASH = new Skill(
            "shield_bash", "Shield Bash", 10, 0,
            Element.PHYSICAL, 1.5f, SkillEffect.STUN);

    private static final Skill DRAIN_LIFE = new Skill(
            "drain_life", "Drain Life", 20, 0,
            Element.DARK, 2.0f, SkillEffect.DRAIN_LIFE);

    // Wave 10 milestone choices
    private static final Skill METEOR = new Skill(
            "meteor", "Meteor", 35, 3,
            Element.FIRE, 4.0f, SkillEffect.DAMAGE);

    private static final Skill DIVINE_SHIELD = new Skill(
            "divine_shield", "Divine Shield", 25, 0,
            Element.HOLY, 0.0f, SkillEffect.DIVINE_SHIELD);

    private static final Skill ASSASSINATE = new Skill(
            "assassinate", "Assassinate", 30, 0,
            Element.PHYSICAL, 6.0f, SkillEffect.ASSASSINATE);

    // Wave 15 milestone choices
    private static final Skill BLOOD_PACT = new Skill(
            "blood_pact", "Blood Pact", 0, 0,
            Element.DARK, 5.0f, SkillEffect.BLOOD_PACT);

    private static final Skill RESURRECTION = new Skill(
            "resurrection", "Resurrection", 50, 0,
            Element.HOLY, 0.0f, SkillEffect.RESURRECTION);

    private static final Skill BERSERKER_RAGE = new Skill(
            "berserker_rage", "Berserker Rage", 0, 5,
            Element.PHYSICAL, 0.0f, SkillEffect.BERSERKER_RAGE);

    private final List<Skill> unlockedSkills = new ArrayList<>();
    private final CharacterClass characterClass;

    /**
     * Legacy no-arg constructor for backward compatibility.
     * Uses the generic default skill set (Fireball/Holy Light/Iron Will).
     */
    public SkillTree() {
        this.characterClass = null;
        // Add default skills (copies for independent cooldown tracking)
        unlockedSkills.add(BASIC_ATTACK.copy());
        unlockedSkills.add(FIREBALL.copy());
        unlockedSkills.add(HOLY_LIGHT.copy());
        unlockedSkills.add(IRON_WILL.copy());
    }

    /**
     * Class-specific constructor. Uses class-based starting skills.
     * Basic Attack is always included as the first skill.
     */
    public SkillTree(CharacterClass characterClass) {
        this.characterClass = characterClass;
        unlockedSkills.add(BASIC_ATTACK.copy());
        List<Skill> classSkills = ClassSkillTree.getDefaultSkills(characterClass);
        for (Skill skill : classSkills) {
            unlockedSkills.add(skill);
        }
    }

    /**
     * Returns the CharacterClass this skill tree was built for, or null for legacy trees.
     */
    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    /**
     * Returns 3 skill choices for the given milestone wave.
     * If a CharacterClass is set, returns class-specific choices.
     * Otherwise returns the generic milestone choices.
     * Returns empty list if the wave is not a milestone.
     */
    public List<Skill> getChoicesForMilestone(int wave) {
        if (characterClass != null) {
            return ClassSkillTree.getMilestoneChoices(characterClass, wave);
        }

        switch (wave) {
            case 5:
                return Arrays.asList(
                        CHAIN_LIGHTNING.copy(),
                        SHIELD_BASH.copy(),
                        DRAIN_LIFE.copy());
            case 10:
                return Arrays.asList(
                        METEOR.copy(),
                        DIVINE_SHIELD.copy(),
                        ASSASSINATE.copy());
            case 15:
                return Arrays.asList(
                        BLOOD_PACT.copy(),
                        RESURRECTION.copy(),
                        BERSERKER_RAGE.copy());
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Unlocks a skill, adding it to the player's available repertoire.
     * Fires a SKILL_UNLOCKED event.
     */
    public void unlockSkill(Skill skill) {
        unlockedSkills.add(skill);
        fireEvent(GameEvent.builder(GameEventType.SKILL_UNLOCKED)
                .put("skillName", skill.getName())
                .put("skillId", skill.getId())
                .put("element", skill.getElement().name())
                .build());
    }

    /**
     * Returns all unlocked skills (defaults + milestone picks).
     */
    public List<Skill> getUnlockedSkills() {
        return Collections.unmodifiableList(unlockedSkills);
    }

    /**
     * Returns a skill by its id, or null if not found among unlocked skills.
     */
    public Skill getSkillById(String id) {
        for (Skill skill : unlockedSkills) {
            if (skill.getId().equals(id)) {
                return skill;
            }
        }
        return null;
    }

    /**
     * Returns a skill by its index in the unlocked skills list.
     * Returns null if the index is out of bounds.
     */
    public Skill getSkillByIndex(int index) {
        if (index < 0 || index >= unlockedSkills.size()) {
            return null;
        }
        return unlockedSkills.get(index);
    }

    /**
     * Ticks cooldowns on all unlocked skills. Should be called once per turn.
     */
    public void tickAllCooldowns() {
        for (Skill skill : unlockedSkills) {
            skill.tickCooldown();
        }
    }

    /**
     * Returns the number of unlocked skills.
     */
    public int getSkillCount() {
        return unlockedSkills.size();
    }
}
