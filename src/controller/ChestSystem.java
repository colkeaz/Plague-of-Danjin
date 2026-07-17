package controller;

import java.util.Random;

import model.Player;
import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;
import model.items.Item;
import model.items.ItemRarity;
import model.items.ItemRegistry;

public class ChestSystem extends GameEventDispatcher {
    private final Random rand = new Random();

    /**
     * Attempts to find a chest with a 50% chance.
     * If a chest is found, applies the loot effect to the player and fires the appropriate event.
     * Returns true if a chest was found, false otherwise.
     */
    public boolean tryFindChest(Player player) {
        if (rand.nextInt(100) < 50) {
            applyChestReward(player);
            return true;
        }
        return false;
    }

    /**
     * Applies chest reward based on exact probability distribution:
     * <10: legendary item (random LEGENDARY from ItemRegistry)
     * <25: epic item (random EPIC from ItemRegistry)
     * <45: rare item (random RARE from ItemRegistry)
     * <85: common item (random COMMON from ItemRegistry)
     * else: mimic (takeDamage 15)
     *
     * Items are auto-equipped into the player's inventory.
     */
    private void applyChestReward(Player player) {
        int chance = rand.nextInt(100);

        fireEvent(GameEvent.builder(GameEventType.CHEST_FOUND)
                .put("playerName", player.getName())
                .build());

        if (chance < 10) {
            // Legendary item
            Item item = ItemRegistry.getRandomByRarity(ItemRarity.LEGENDARY);
            dropAndEquip(player, item, GameEventType.CHEST_LEGENDARY);
        } else if (chance < 25) {
            // Epic item
            Item item = ItemRegistry.getRandomByRarity(ItemRarity.EPIC);
            dropAndEquip(player, item, GameEventType.CHEST_EPIC);
        } else if (chance < 45) {
            // Rare item
            Item item = ItemRegistry.getRandomByRarity(ItemRarity.RARE);
            dropAndEquip(player, item, GameEventType.CHEST_RARE);
        } else if (chance < 85) {
            // Common item
            Item item = ItemRegistry.getRandomByRarity(ItemRarity.COMMON);
            dropAndEquip(player, item, GameEventType.CHEST_COMMON);
        } else {
            // Mimic: takeDamage 15
            player.takeDamage(15);
            fireEvent(GameEvent.builder(GameEventType.CHEST_MIMIC)
                    .put("playerName", player.getName())
                    .put("damage", 15)
                    .build());
        }
    }

    /**
     * Fires ITEM_DROPPED event and auto-equips the item into the player's inventory.
     */
    private void dropAndEquip(Player player, Item item, GameEventType chestType) {
        fireEvent(GameEvent.builder(GameEventType.ITEM_DROPPED)
                .put("itemName", item.getName())
                .put("rarity", item.getRarity().name())
                .put("slot", item.getSlot().name())
                .build());

        fireEvent(GameEvent.builder(chestType)
                .put("playerName", player.getName())
                .put("itemName", item.getName())
                .put("rarity", item.getRarity().name())
                .build());

        player.getInventory().equip(item);
    }
}
