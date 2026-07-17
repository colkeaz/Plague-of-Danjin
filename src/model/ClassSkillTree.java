package model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import model.skills.Element;
import model.skills.Skill;
import model.skills.SkillEffect;

/**
 * Holds all class-specific skill definitions and provides
 * default skills and milestone choices per CharacterClass.
 * Keeps SkillTree.java from growing too large.
 */
public class ClassSkillTree {

    // ===== KNIGHT SKILLS =====

    // Starting skills
    public static final Skill KNIGHT_SHIELD_SLAM = new Skill(
            "knight_shield_slam", "Shield Slam", 8, 2,
            Element.PHYSICAL, 1.5f, SkillEffect.STUN);

    public static final Skill KNIGHT_RALLY = new Skill(
            "knight_rally", "Rally", 10, 0,
            Element.PHYSICAL, 0.0f, SkillEffect.SELF_HEAL_CURE);

    public static final Skill KNIGHT_FORTRESS = new Skill(
            "knight_fortress", "Fortress", 15, 3,
            Element.PHYSICAL, 0.0f, SkillEffect.BUFF_DEF_TEMP);

    // Wave 5 milestone choices
    public static final Skill KNIGHT_TAUNT = new Skill(
            "knight_taunt", "Taunt", 8, 2,
            Element.PHYSICAL, 0.0f, SkillEffect.BUFF_DEF_TEMP);

    public static final Skill KNIGHT_RIPOSTE = new Skill(
            "knight_riposte", "Riposte", 10, 2,
            Element.PHYSICAL, 2.0f, SkillEffect.DAMAGE);

    public static final Skill KNIGHT_EARTHQUAKE = new Skill(
            "knight_earthquake", "Earthquake", 20, 3,
            Element.PHYSICAL, 2.0f, SkillEffect.MULTI_TARGET);

    // Wave 10 milestone choices
    public static final Skill KNIGHT_PALADINS_OATH = new Skill(
            "knight_paladins_oath", "Paladin's Oath", 20, 3,
            Element.HOLY, 2.0f, SkillEffect.DRAIN_OVER_TIME);

    public static final Skill KNIGHT_BULWARK = new Skill(
            "knight_bulwark", "Bulwark", 25, 5,
            Element.PHYSICAL, 0.0f, SkillEffect.INVULNERABLE);

    public static final Skill KNIGHT_WARLORDS_FURY = new Skill(
            "knight_warlords_fury", "Warlord's Fury", 0, 3,
            Element.PHYSICAL, 0.0f, SkillEffect.SACRIFICE_BUFF);

    // Wave 15 milestone choices
    public static final Skill KNIGHT_IMMORTAL_STAND = new Skill(
            "knight_immortal_stand", "Immortal Stand", 0, 0,
            Element.PHYSICAL, 0.0f, SkillEffect.IMMORTAL_STAND);

    public static final Skill KNIGHT_DRAGON_SLAYER = new Skill(
            "knight_dragon_slayer", "Dragon Slayer", 0, 4,
            Element.FIRE, 5.0f, SkillEffect.HP_COST_DAMAGE);

    public static final Skill KNIGHT_AEGIS_OF_LIGHT = new Skill(
            "knight_aegis_of_light", "Aegis of Light", 30, 5,
            Element.HOLY, 0.0f, SkillEffect.SHIELD_ALL);

    // ===== MAGE SKILLS =====

    // Starting skills (Mage Fireball is cheaper at 15 MP)
    public static final Skill MAGE_FIREBALL = new Skill(
            "mage_fireball", "Fireball", 15, 1,
            Element.FIRE, 3.0f, SkillEffect.DAMAGE);

    public static final Skill MAGE_ICE_SHARD = new Skill(
            "mage_ice_shard", "Ice Shard", 12, 0,
            Element.PHYSICAL, 2.0f, SkillEffect.STUN);

    public static final Skill MAGE_ARCANE_SHIELD = new Skill(
            "mage_arcane_shield", "Arcane Shield", 20, 0,
            Element.PHYSICAL, 0.0f, SkillEffect.SHIELD_SINGLE);

    // Wave 5 milestone choices
    public static final Skill MAGE_LIGHTNING_STORM = new Skill(
            "mage_lightning_storm", "Lightning Storm", 18, 0,
            Element.FIRE, 2.0f, SkillEffect.MULTI_TARGET);

    public static final Skill MAGE_MANA_DRAIN = new Skill(
            "mage_mana_drain", "Mana Drain", 10, 0,
            Element.DARK, 1.5f, SkillEffect.MANA_DRAIN);

    public static final Skill MAGE_FROST_NOVA = new Skill(
            "mage_frost_nova", "Frost Nova", 15, 0,
            Element.PHYSICAL, 1.5f, SkillEffect.STUN_MULTI_TARGET);

    // Wave 10 milestone choices
    public static final Skill MAGE_METEOR = new Skill(
            "mage_meteor", "Meteor", 40, 3,
            Element.FIRE, 5.0f, SkillEffect.DAMAGE);

    public static final Skill MAGE_TIME_WARP = new Skill(
            "mage_time_warp", "Time Warp", 35, 5,
            Element.PHYSICAL, 0.0f, SkillEffect.TIME_WARP);

    public static final Skill MAGE_ARCANE_MASTERY = new Skill(
            "mage_arcane_mastery", "Arcane Mastery", 0, 0,
            Element.PHYSICAL, 0.0f, SkillEffect.PASSIVE_COST_REDUCTION);

    // Wave 15 milestone choices
    public static final Skill MAGE_ARMAGEDDON = new Skill(
            "mage_armageddon", "Armageddon", 60, 4,
            Element.FIRE, 4.0f, SkillEffect.MULTI_TARGET);

    public static final Skill MAGE_SOUL_REAVE = new Skill(
            "mage_soul_reave", "Soul Reave", 45, 3,
            Element.DARK, 6.0f, SkillEffect.DRAIN_LIFE);

    public static final Skill MAGE_MANA_SINGULARITY = new Skill(
            "mage_mana_singularity", "Mana Singularity", 0, 4,
            Element.PHYSICAL, 0.0f, SkillEffect.MANA_SINGULARITY);

    // ===== ROGUE SKILLS =====

    // Starting skills
    public static final Skill ROGUE_POISON_STRIKE = new Skill(
            "rogue_poison_strike", "Poison Strike", 10, 0,
            Element.POISON, 1.5f, SkillEffect.POISON_DAMAGE);

    public static final Skill ROGUE_SHADOW_STEP = new Skill(
            "rogue_shadow_step", "Shadow Step", 12, 0,
            Element.PHYSICAL, 0.0f, SkillEffect.DODGE_NEXT);

    public static final Skill ROGUE_BACKSTAB = new Skill(
            "rogue_backstab", "Backstab", 18, 2,
            Element.PHYSICAL, 4.0f, SkillEffect.CONDITIONAL_DAMAGE);

    // Wave 5 milestone choices
    public static final Skill ROGUE_FAN_OF_KNIVES = new Skill(
            "rogue_fan_of_knives", "Fan of Knives", 12, 0,
            Element.PHYSICAL, 1.5f, SkillEffect.POISON_MULTI_TARGET);

    public static final Skill ROGUE_SMOKE_BOMB = new Skill(
            "rogue_smoke_bomb", "Smoke Bomb", 8, 2,
            Element.PHYSICAL, 0.0f, SkillEffect.SMOKE_BOMB);

    public static final Skill ROGUE_LACERATE = new Skill(
            "rogue_lacerate", "Lacerate", 15, 0,
            Element.PHYSICAL, 2.0f, SkillEffect.DAMAGE_OVER_TIME);

    // Wave 10 milestone choices
    public static final Skill ROGUE_ASSASSINATE = new Skill(
            "rogue_assassinate", "Assassinate", 30, 3,
            Element.PHYSICAL, 8.0f, SkillEffect.CLASS_ASSASSINATE);

    public static final Skill ROGUE_TOXIC_EXPLOSION = new Skill(
            "rogue_toxic_explosion", "Toxic Explosion", 20, 2,
            Element.POISON, 3.0f, SkillEffect.TOXIC_EXPLOSION);

    public static final Skill ROGUE_SHADOW_CLONE = new Skill(
            "rogue_shadow_clone", "Shadow Clone", 25, 4,
            Element.PHYSICAL, 0.5f, SkillEffect.SHADOW_CLONE);

    // Wave 15 milestone choices
    public static final Skill ROGUE_DEATH_MARK = new Skill(
            "rogue_death_mark", "Death Mark", 35, 5,
            Element.DARK, 0.0f, SkillEffect.DEATH_MARK);

    public static final Skill ROGUE_VANISH = new Skill(
            "rogue_vanish", "Vanish", 0, 5,
            Element.PHYSICAL, 0.0f, SkillEffect.VANISH);

    public static final Skill ROGUE_PLAGUE_BEARER = new Skill(
            "rogue_plague_bearer", "Plague Bearer", 0, 0,
            Element.POISON, 0.0f, SkillEffect.PLAGUE_BEARER);

    private ClassSkillTree() {
        // Utility class, no instantiation
    }

    /**
     * Returns the default skill set (excluding Basic Attack) for a class.
     * Each class gets 3 starting skills (slots 1-3 in the skill tree).
     */
    public static List<Skill> getDefaultSkills(CharacterClass characterClass) {
        if (characterClass == null) return Collections.emptyList();

        switch (characterClass) {
            case KNIGHT:
                return Arrays.asList(
                        KNIGHT_SHIELD_SLAM.copy(),
                        KNIGHT_RALLY.copy(),
                        KNIGHT_FORTRESS.copy());
            case MAGE:
                return Arrays.asList(
                        MAGE_FIREBALL.copy(),
                        MAGE_ICE_SHARD.copy(),
                        MAGE_ARCANE_SHIELD.copy());
            case ROGUE:
                return Arrays.asList(
                        ROGUE_POISON_STRIKE.copy(),
                        ROGUE_SHADOW_STEP.copy(),
                        ROGUE_BACKSTAB.copy());
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Returns 3 milestone skill choices for a class at the given wave.
     * Returns empty list if the wave is not a milestone (5, 10, 15).
     */
    public static List<Skill> getMilestoneChoices(CharacterClass characterClass, int wave) {
        if (characterClass == null) return Collections.emptyList();

        switch (characterClass) {
            case KNIGHT:
                return getKnightMilestone(wave);
            case MAGE:
                return getMageMilestone(wave);
            case ROGUE:
                return getRogueMilestone(wave);
            default:
                return Collections.emptyList();
        }
    }

    private static List<Skill> getKnightMilestone(int wave) {
        switch (wave) {
            case 5:
                return Arrays.asList(
                        KNIGHT_TAUNT.copy(),
                        KNIGHT_RIPOSTE.copy(),
                        KNIGHT_EARTHQUAKE.copy());
            case 10:
                return Arrays.asList(
                        KNIGHT_PALADINS_OATH.copy(),
                        KNIGHT_BULWARK.copy(),
                        KNIGHT_WARLORDS_FURY.copy());
            case 15:
                return Arrays.asList(
                        KNIGHT_IMMORTAL_STAND.copy(),
                        KNIGHT_DRAGON_SLAYER.copy(),
                        KNIGHT_AEGIS_OF_LIGHT.copy());
            default:
                return Collections.emptyList();
        }
    }

    private static List<Skill> getMageMilestone(int wave) {
        switch (wave) {
            case 5:
                return Arrays.asList(
                        MAGE_LIGHTNING_STORM.copy(),
                        MAGE_MANA_DRAIN.copy(),
                        MAGE_FROST_NOVA.copy());
            case 10:
                return Arrays.asList(
                        MAGE_METEOR.copy(),
                        MAGE_TIME_WARP.copy(),
                        MAGE_ARCANE_MASTERY.copy());
            case 15:
                return Arrays.asList(
                        MAGE_ARMAGEDDON.copy(),
                        MAGE_SOUL_REAVE.copy(),
                        MAGE_MANA_SINGULARITY.copy());
            default:
                return Collections.emptyList();
        }
    }

    private static List<Skill> getRogueMilestone(int wave) {
        switch (wave) {
            case 5:
                return Arrays.asList(
                        ROGUE_FAN_OF_KNIVES.copy(),
                        ROGUE_SMOKE_BOMB.copy(),
                        ROGUE_LACERATE.copy());
            case 10:
                return Arrays.asList(
                        ROGUE_ASSASSINATE.copy(),
                        ROGUE_TOXIC_EXPLOSION.copy(),
                        ROGUE_SHADOW_CLONE.copy());
            case 15:
                return Arrays.asList(
                        ROGUE_DEATH_MARK.copy(),
                        ROGUE_VANISH.copy(),
                        ROGUE_PLAGUE_BEARER.copy());
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Searches all class skills (defaults + all milestones) for a skill with the given ID.
     * Returns a copy of the skill if found, or null if not found.
     * Used by CombatEngineRestorer to find class skills during save restoration.
     */
    public static Skill findSkillById(String skillId) {
        if (skillId == null) return null;

        for (CharacterClass cc : CharacterClass.values()) {
            // Check default skills
            for (Skill s : getDefaultSkills(cc)) {
                if (s.getId().equals(skillId)) return s;
            }
            // Check all milestones
            for (int wave : new int[]{5, 10, 15}) {
                for (Skill s : getMilestoneChoices(cc, wave)) {
                    if (s.getId().equals(skillId)) return s;
                }
            }
        }
        return null;
    }
}
