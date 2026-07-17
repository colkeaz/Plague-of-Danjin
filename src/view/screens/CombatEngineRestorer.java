package view.screens;

import java.util.List;
import java.util.Map;

import controller.CombatEngine;
import controller.GameState;
import controller.MetaProgression;
import controller.WorldManager;
import model.CharacterClass;
import model.ClassSkillTree;
import model.Player;
import model.SaveData;
import model.items.Item;
import model.items.ItemRegistry;
import model.skills.Skill;
import model.skills.SkillTree;
import model.status.StatusEffect;
import model.status.StatusType;
import model.world.Area;
import model.world.WorldState;

/**
 * Utility class that restores a CombatEngine state from a SaveData object.
 * Used by MainMenuScreen to implement the Continue functionality.
 */
class CombatEngineRestorer {

    /**
     * Creates a new CombatEngine and restores its state from the given SaveData.
     * The engine will be at the start of the saved wave in AWAITING_PLAYER_ACTION state.
     * MetaProgression is applied before stat restoration so unlock items are available.
     */
    public CombatEngine restoreFromSave(SaveData saveData, MetaProgression meta) {
        if (saveData == null) return null;

        CombatEngine engine = new CombatEngine();

        // Start game with class if available in save data
        CharacterClass characterClass = parseCharacterClass(saveData.getCharacterClass());
        if (characterClass != null) {
            engine.startGame(saveData.getPlayerName(), characterClass);
        } else {
            engine.startGame(saveData.getPlayerName());
        }

        // Apply unlocks before restoring stats so unlock items (Veteran's Blade,
        // Iron Constitution, Swift Boots) are equipped and their effects are active.
        // This ensures findItemByName() is not needed for unlock items since they are
        // already equipped via applyUnlocks().
        if (meta != null) {
            engine.applyUnlocks(meta, engine.getChestSystem());
        }

        Player player = engine.getPlayer();

        // Restore max HP first using setMaxHp to handle both increases and decreases
        player.setMaxHp(saveData.getMaxHp());

        // Restore player stats by adjusting from current values (post-unlock)
        int atkDiff = saveData.getAtk() - player.getAttackPower();
        if (atkDiff != 0) {
            player.upgradePower(atkDiff);
        }

        int defDiff = saveData.getDef() - player.getDefense();
        if (defDiff != 0) {
            player.upgradeDefense(defDiff);
        }

        // Set HP directly using setHp() to accurately restore saved HP (even below max)
        player.setHp(saveData.getHp());

        // Restore equipped items by name (skip items already equipped by applyUnlocks)
        List<String> itemNames = saveData.getEquippedItemNames();
        if (itemNames != null) {
            for (String name : itemNames) {
                if (name != null && !name.isEmpty()) {
                    // Skip if this item is already equipped (from applyUnlocks)
                    if (isAlreadyEquipped(player, name)) continue;
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

        // Restore permanent skill flags
        if (saveData.getSpellCostReductionBonus() > 0) {
            player.setSpellCostReductionBonus(saveData.getSpellCostReductionBonus());
        }
        if (saveData.isPlagueBearerActive()) {
            player.setPlagueBearerActive(true);
        }
        if (saveData.isImmortalStandActive()) {
            player.setImmortalStandActive(true);
        }

        // Restore game mode: story vs classic
        String gameMode = saveData.getGameMode();
        if ("STORY".equals(gameMode)) {
            // Restore WorldState
            WorldState worldState = new WorldState();

            // Restore current area
            String currentAreaStr = saveData.getCurrentArea();
            if (currentAreaStr != null && !currentAreaStr.isEmpty()) {
                try {
                    worldState.setCurrentArea(Area.valueOf(currentAreaStr));
                } catch (IllegalArgumentException e) {
                    // Keep default (DANJINS_CORE)
                }
            }

            // Restore completed areas
            List<String> completedAreas = saveData.getCompletedAreas();
            if (completedAreas != null) {
                for (String areaName : completedAreas) {
                    try {
                        worldState.completeArea(Area.valueOf(areaName));
                    } catch (IllegalArgumentException e) {
                        // Skip invalid area names
                    }
                }
            }

            // Restore keys collected
            List<String> keys = saveData.getKeysCollected();
            if (keys != null) {
                for (String key : keys) {
                    worldState.collectKey(key);
                }
            }

            // Restore encounter progress (format: "AREA:index")
            List<String> progressEntries = saveData.getEncounterProgress();
            if (progressEntries != null) {
                for (String entry : progressEntries) {
                    int colonIdx = entry.lastIndexOf(':');
                    if (colonIdx > 0) {
                        String areaName = entry.substring(0, colonIdx);
                        String indexStr = entry.substring(colonIdx + 1);
                        try {
                            Area area = Area.valueOf(areaName);
                            int index = Integer.parseInt(indexStr);
                            // Set encounter progress by advancing to the saved index
                            for (int i = 0; i < index; i++) {
                                worldState.advanceEncounter(area);
                            }
                        } catch (Exception e) {
                            // Skip invalid entries
                        }
                    }
                }
            }

            // Restore rest shrine
            worldState.setRestShrineUsed(saveData.isRestShrineUsed());

            // Restore event choices (format: "eventName:choiceIndex")
            List<String> choiceEntries = saveData.getEventChoices();
            if (choiceEntries != null) {
                for (String entry : choiceEntries) {
                    int colonIdx = entry.lastIndexOf(':');
                    if (colonIdx > 0) {
                        String eventName = entry.substring(0, colonIdx);
                        String choiceStr = entry.substring(colonIdx + 1);
                        try {
                            int choiceIndex = Integer.parseInt(choiceStr);
                            worldState.recordEventChoice(eventName, choiceIndex);
                        } catch (NumberFormatException e) {
                            // Skip invalid entries
                        }
                    }
                }
            }

            // Create WorldManager with restored state and set it on the engine
            WorldManager worldManager = new WorldManager(worldState);
            engine.setWorldManager(worldManager);

            // Restore area mechanic flags
            engine.setToxicAtmospherePurified(saveData.isToxicAtmospherePurified());
            engine.setConsecratedGroundBlessing(saveData.isConsecratedGroundBlessing());

            // Set state to WORLD_MAP (player resumes at map)
            engine.setCurrentState(GameState.WORLD_MAP);
        } else {
            // Classic mode - restore wave
            engine.restoreToWave(saveData.getWaveNumber());
        }

        return engine;
    }

    /**
     * Backwards-compatible overload for callers that don't have MetaProgression.
     */
    public CombatEngine restoreFromSave(SaveData saveData) {
        return restoreFromSave(saveData, null);
    }

    /**
     * Checks if an item with the given name is already equipped on the player.
     */
    private boolean isAlreadyEquipped(Player player, String itemName) {
        for (model.items.ItemSlot slot : model.items.ItemSlot.values()) {
            Item equipped = player.getInventory().getEquipped(slot);
            if (equipped != null && equipped.getName().equals(itemName)) {
                return true;
            }
        }
        return false;
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
     * Finds a skill by ID using the SkillTree milestone choices and class skill trees.
     */
    private Skill findSkillById(String skillId) {
        // First check generic milestone skills
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

        // Then check class-specific skills
        Skill classSkill = ClassSkillTree.findSkillById(skillId);
        if (classSkill != null) return classSkill;

        return null;
    }

    /**
     * Parses a CharacterClass from a string name. Returns null if invalid or null.
     */
    private CharacterClass parseCharacterClass(String className) {
        if (className == null || className.isEmpty()) return null;
        try {
            return CharacterClass.valueOf(className);
        } catch (IllegalArgumentException e) {
            return null;
        }
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
