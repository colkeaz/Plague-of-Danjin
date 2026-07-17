package controller;

import java.util.Arrays;
import java.util.List;

import model.Player;
import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;
import model.items.Inventory;
import model.items.Item;
import model.items.ItemRarity;
import model.items.ItemRegistry;
import model.items.ItemSlot;
import model.status.StatusEffect;
import model.status.StatusType;

/**
 * Manages event rooms that appear after specific waves (3, 7, 13, 17).
 * Each event room presents the player with choices that have permanent consequences.
 */
public class EventRoomManager extends GameEventDispatcher {

    private static final int[] EVENT_WAVES = {3, 7, 13, 17};

    /**
     * Returns true if an event room should trigger after the given wave.
     */
    public boolean shouldTrigger(int wave) {
        for (int eventWave : EVENT_WAVES) {
            if (wave == eventWave) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the event room description for the given wave.
     * Returns null if no event room exists for that wave.
     */
    public String getEventRoomDescription(int wave) {
        switch (wave) {
            case 3:
                return "The Dying Knight";
            case 7:
                return "Cursed Fountain";
            case 13:
                return "The Merchant Ghost";
            case 17:
                return "Danjin's Heart";
            default:
                return null;
        }
    }

    /**
     * Returns the choice descriptions for the event room at the given wave.
     */
    public List<String> getChoices(int wave) {
        switch (wave) {
            case 3:
                return Arrays.asList(
                        "Help him (+1 random RARE item)",
                        "Loot his corpse (+1 EPIC weapon, but CURSE: -5 max HP)");
            case 7:
                return Arrays.asList(
                        "Drink (+50 HP, -10 max MP permanently)",
                        "Smash it (+15 ATK permanently)",
                        "Walk away (nothing)");
            case 13:
                return Arrays.asList(
                        "Buy a random LEGENDARY item (costs 50 HP)",
                        "Trade your weapon for a random EPIC weapon",
                        "Decline");
            case 17:
                return Arrays.asList(
                        "Absorb corruption (+30 ATK, +30 DEF, but take 10 damage per turn permanently)",
                        "Resist (full heal + full mana restore)",
                        "Shatter it (remove all CURSE effects, enemies in remaining waves lose 20% HP)");
            default:
                return Arrays.asList();
        }
    }

    /**
     * Returns the number of choices for the event room at the given wave.
     */
    public int getChoiceCount(int wave) {
        return getChoices(wave).size();
    }

    /**
     * Applies the chosen effect for the event room at the given wave.
     * 
     * @param wave the wave number after which the event room appeared
     * @param choiceIndex 0-based index of the player's choice
     * @param player the player to apply effects to
     * @param modifiers the run modifiers to track permanent effects
     */
    public void applyChoice(int wave, int choiceIndex, Player player, RunModifiers modifiers) {
        fireEvent(GameEvent.builder(GameEventType.EVENT_ROOM_ENTERED)
                .put("roomName", getEventRoomDescription(wave))
                .put("waveNumber", wave)
                .build());

        switch (wave) {
            case 3:
                applyDyingKnightChoice(choiceIndex, player, modifiers);
                break;
            case 7:
                applyCursedFountainChoice(choiceIndex, player, modifiers);
                break;
            case 13:
                applyMerchantGhostChoice(choiceIndex, player, modifiers);
                break;
            case 17:
                applyDanjinHeartChoice(choiceIndex, player, modifiers);
                break;
            default:
                break;
        }
    }

    /**
     * Wave 3 - The Dying Knight:
     * Choice A (0): Help him - gain 1 random RARE item
     * Choice B (1): Loot his corpse - gain 1 EPIC weapon, but CURSE: -5 max HP
     */
    private void applyDyingKnightChoice(int choice, Player player, RunModifiers modifiers) {
        if (choice == 0) {
            // Help him: +1 random RARE item
            Item rareItem = ItemRegistry.getRandomByRarity(ItemRarity.RARE);
            if (rareItem != null) {
                player.getInventory().equip(rareItem);
            }
            modifiers.addModifier("Helped the Dying Knight: received " +
                    (rareItem != null ? rareItem.getName() : "a RARE item"));
        } else {
            // Loot his corpse: +1 EPIC weapon, CURSE: -5 max HP
            Item epicWeapon = ItemRegistry.getRandomByRarityAndSlot(ItemRarity.EPIC, ItemSlot.WEAPON);
            if (epicWeapon != null) {
                player.getInventory().equip(epicWeapon);
            }
            player.reduceMaxHp(5);
            modifiers.addModifier("Looted the Dying Knight: received EPIC weapon, CURSE: -5 max HP");

            fireEvent(GameEvent.builder(GameEventType.CURSE_APPLIED)
                    .put("curseName", "Knight's Curse")
                    .put("effect", "Max HP reduced by 5")
                    .put("currentMaxHp", player.getMaxHp())
                    .build());
        }

        fireEvent(GameEvent.builder(GameEventType.EVENT_ROOM_CHOICE_MADE)
                .put("roomName", "The Dying Knight")
                .put("choiceIndex", choice)
                .build());
    }

    /**
     * Wave 7 - Cursed Fountain:
     * Choice A (0): Drink - heal 50 HP, reduce max mana by 10 permanently
     * Choice B (1): Smash it - +15 ATK permanently
     * Choice C (2): Walk away - nothing
     */
    private void applyCursedFountainChoice(int choice, Player player, RunModifiers modifiers) {
        if (choice == 0) {
            // Drink: +50 HP, -10 max MP permanently
            player.heal(50);
            player.reduceMaxMana(10);
            modifiers.addModifier("Drank from Cursed Fountain: +50 HP, -10 max MP permanently");
        } else if (choice == 1) {
            // Smash it: +15 ATK permanently
            player.upgradePower(15);
            modifiers.addModifier("Smashed Cursed Fountain: +15 ATK permanently");
        }
        // Choice 2: Walk away - nothing

        fireEvent(GameEvent.builder(GameEventType.EVENT_ROOM_CHOICE_MADE)
                .put("roomName", "Cursed Fountain")
                .put("choiceIndex", choice)
                .build());
    }

    /**
     * Wave 13 - The Merchant Ghost:
     * Choice A (0): Buy random LEGENDARY item (costs 50 HP)
     * Choice B (1): Trade weapon for random EPIC weapon
     * Choice C (2): Decline - nothing
     */
    private void applyMerchantGhostChoice(int choice, Player player, RunModifiers modifiers) {
        if (choice == 0) {
            // Buy LEGENDARY item for 50 HP
            player.takeDamage(50);
            Item legendary = ItemRegistry.getRandomByRarity(ItemRarity.LEGENDARY);
            if (legendary != null) {
                player.getInventory().equip(legendary);
            }
            modifiers.addModifier("Bought from Merchant Ghost: LEGENDARY item for 50 HP");
        } else if (choice == 1) {
            // Trade weapon for random EPIC weapon
            player.getInventory().unequip(ItemSlot.WEAPON);
            Item epicWeapon = ItemRegistry.getRandomByRarityAndSlot(ItemRarity.EPIC, ItemSlot.WEAPON);
            if (epicWeapon != null) {
                player.getInventory().equip(epicWeapon);
            }
            modifiers.addModifier("Traded weapon with Merchant Ghost: received random EPIC weapon");
        }
        // Choice 2: Decline - nothing

        fireEvent(GameEvent.builder(GameEventType.EVENT_ROOM_CHOICE_MADE)
                .put("roomName", "The Merchant Ghost")
                .put("choiceIndex", choice)
                .build());
    }

    /**
     * Wave 17 - Danjin's Heart:
     * Choice A (0): Absorb corruption (+30 ATK, +30 DEF, 10 damage/turn permanently)
     * Choice B (1): Resist (full heal + full mana restore)
     * Choice C (2): Shatter it (remove all CURSE effects, enemies lose 20% HP)
     */
    private void applyDanjinHeartChoice(int choice, Player player, RunModifiers modifiers) {
        if (choice == 0) {
            // Absorb: +30 ATK, +30 DEF, take 10 damage per turn permanently
            player.upgradePower(30);
            player.upgradeDefense(30);
            modifiers.setDanjinHeartAbsorbed(true);
            modifiers.setPermanentDamagePerTurn(10);
            modifiers.addModifier("Absorbed Danjin's Heart: +30 ATK, +30 DEF, 10 damage/turn permanently");
        } else if (choice == 1) {
            // Resist: full heal + full mana restore
            player.fullRestore();
            modifiers.addModifier("Resisted Danjin's Heart: full heal and mana restore");
        } else {
            // Shatter: remove all CURSE effects, enemies lose 20% HP
            player.getStatusManager().removeEffect(StatusType.CURSE);
            modifiers.setDanjinHeartShattered(true);
            modifiers.addModifier("Shattered Danjin's Heart: curses removed, enemies weakened 20%");
        }

        fireEvent(GameEvent.builder(GameEventType.EVENT_ROOM_CHOICE_MADE)
                .put("roomName", "Danjin's Heart")
                .put("choiceIndex", choice)
                .build());
    }
}
