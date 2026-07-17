package controller;

import java.util.Random;

import model.Player;
import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;

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
     * <10: legendary (+20 ATK)
     * <25: epic (+15 DEF, +10 HP)
     * <45: rare (+8 ATK, +8 DEF)
     * <85: common (+30 HP)
     * else: mimic (takeDamage 15)
     */
    private void applyChestReward(Player player) {
        int chance = rand.nextInt(100);

        fireEvent(GameEvent.builder(GameEventType.CHEST_FOUND)
                .put("playerName", player.getName())
                .build());

        if (chance < 10) {
            // Legendary: +20 ATK
            player.upgradePower(20);
            fireEvent(GameEvent.builder(GameEventType.CHEST_LEGENDARY)
                    .put("playerName", player.getName())
                    .put("attackBonus", 20)
                    .build());
        } else if (chance < 25) {
            // Epic: +15 DEF, +10 HP
            player.upgradeDefense(15);
            player.heal(10);
            fireEvent(GameEvent.builder(GameEventType.CHEST_EPIC)
                    .put("playerName", player.getName())
                    .put("defenseBonus", 15)
                    .put("hpBonus", 10)
                    .build());
        } else if (chance < 45) {
            // Rare: +8 ATK, +8 DEF
            player.upgradePower(8);
            player.upgradeDefense(8);
            fireEvent(GameEvent.builder(GameEventType.CHEST_RARE)
                    .put("playerName", player.getName())
                    .put("attackBonus", 8)
                    .put("defenseBonus", 8)
                    .build());
        } else if (chance < 85) {
            // Common: +30 HP
            player.heal(30);
            fireEvent(GameEvent.builder(GameEventType.CHEST_COMMON)
                    .put("playerName", player.getName())
                    .put("healAmount", 30)
                    .build());
        } else {
            // Mimic: takeDamage 15
            player.takeDamage(15);
            fireEvent(GameEvent.builder(GameEventType.CHEST_MIMIC)
                    .put("playerName", player.getName())
                    .put("damage", 15)
                    .build());
        }
    }
}
