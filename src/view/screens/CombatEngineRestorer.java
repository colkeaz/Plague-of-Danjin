package view.screens;

import java.util.List;

import controller.CombatEngine;
import model.Player;
import model.SaveData;
import model.items.Item;
import model.items.ItemRegistry;
import model.skills.Skill;
import model.skills.SkillTree;
import model.status.StatusEffect;
import model.status.StatusType;

/**
 * Utility class that restores a CombatEngine state from a SaveData object.
 * Used by MainMenuScreen to implement the Continue functionality.
 */
class CombatEngineRestorer {

    /**
     * Creates a new CombatEngine and restores its state from the given SaveData.
     * The engine will be at the start of the saved wave in AWAITING_PLAYER_ACTION state.
     */
    public CombatEngine restoreFromSave(SaveData saveData) {
        if (saveData == null) return null;

        CombatEngine engine = new CombatEngine();
        engine.startGame(saveData.getPlayerName());

        Player player = engine.getPlayer();

        // Restore player stats by adjusting from defaults
        // Player starts with: 100 HP, 30 ATK, 15 DEF, 75 mana, 100 maxMana
        int atkDiff = saveData.getAtk() - player.getAttackPower();
        if (atkDiff != 0) {
            player.upgradePower(atkDiff);
        }

        int defDiff = saveData.getDef() - player.getDefense();
        if (defDiff != 0) {
            player.upgradeDefense(defDiff);
        }

        // Reduce max HP if needed (cannot increase max HP via reduceMaxHp with negative)
        // We handle this by reducing by the difference
        int maxHpDiff = player.getMaxHp() - saveData.getMaxHp();
        if (maxHpDiff > 0) {
            player.reduceMaxHp(maxHpDiff);
        }

        // Set HP directly using setHp() to accurately restore saved HP (even below max)
        player.fullRestore();
        player.setHp(saveData.getHp());

        // Restore equipped items by name
        List<String> itemNames = saveData.getEquippedItemNames();
        if (itemNames != null) {
            for (String name : itemNames) {
                if (name != null && !name.isEmpty()) {
                    Item item = findItemByName(name);
                    if (item != null) {
                        player.getInventory().equip(item);
                    }
                }
            }
        }

        // Restore unlocked skills beyond defaults
        List<String> skillIds = saveData.getUnlockedSkillIds();
        if (skillIds != null) {
            // The engine already has the 4 default skills unlocked
            // We need to unlock any additional ones
            List<Skill> currentSkills = player.getSkillTree().getUnlockedSkills();
            for (String skillId : skillIds) {
                boolean alreadyUnlocked = false;
                for (Skill existing : currentSkills) {
                    if (existing.getId().equals(skillId)) {
                        alreadyUnlocked = true;
                        break;
                    }
                }
                if (!alreadyUnlocked) {
                    Skill skill = findSkillById(skillId);
                    if (skill != null) {
                        player.getSkillTree().unlockSkill(skill);
                    }
                }
            }
        }

        // Restore status effects
        List<String> statusEffects = saveData.getActiveStatusEffects();
        if (statusEffects != null) {
            for (String effectStr : statusEffects) {
                StatusEffect effect = parseStatusEffect(effectStr);
                if (effect != null) {
                    player.getStatusManager().addEffect(effect);
                }
            }
        }

        // Restore run modifiers
        engine.getRunModifiers().setDanjinHeartAbsorbed(saveData.isDanjinHeartAbsorbed());
        engine.getRunModifiers().setDanjinHeartShattered(saveData.isDanjinHeartShattered());
        engine.getRunModifiers().setPermanentDamagePerTurn(saveData.getPermanentDamagePerTurn());

        // Restore wave - advance to the saved wave
        engine.restoreToWave(saveData.getWaveNumber());

        return engine;
    }

    /**
     * Finds an item by name from the ItemRegistry.
     */
    private Item findItemByName(String name) {
        for (Item item : ItemRegistry.getAllItems()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Finds a skill by ID using the SkillTree milestone choices.
     */
    private Skill findSkillById(String skillId) {
        // Check wave 5 milestone skills
        SkillTree tempTree = new SkillTree();
        List<Skill> wave5 = tempTree.getChoicesForMilestone(5);
        for (Skill s : wave5) {
            if (s.getId().equals(skillId)) return s;
        }
        List<Skill> wave10 = tempTree.getChoicesForMilestone(10);
        for (Skill s : wave10) {
            if (s.getId().equals(skillId)) return s;
        }
        List<Skill> wave15 = tempTree.getChoicesForMilestone(15);
        for (Skill s : wave15) {
            if (s.getId().equals(skillId)) return s;
        }
        return null;
    }

    /**
     * Parses a status effect from its serialized format: "TYPE:duration:potency"
     */
    private StatusEffect parseStatusEffect(String effectStr) {
        if (effectStr == null || effectStr.isEmpty()) return null;
        String[] parts = effectStr.split(":");
        if (parts.length < 3) return null;

        try {
            StatusType type = StatusType.valueOf(parts[0]);
            int duration = Integer.parseInt(parts[1]);
            int potency = Integer.parseInt(parts[2]);
            return new StatusEffect(type, duration, potency, "SaveRestore");
        } catch (Exception e) {
            return null;
        }
    }
}
