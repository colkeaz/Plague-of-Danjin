package model.items;

import java.util.EnumMap;
import java.util.Map;

import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;
import model.skills.Element;

/**
 * Manages the player's equipped items across 3 slots (weapon, armor, accessory).
 * Fires events on equip/unequip and provides stat bonus computation.
 */
public class Inventory extends GameEventDispatcher {
    private final Map<ItemSlot, Item> equipped = new EnumMap<>(ItemSlot.class);

    /**
     * Equips an item in its corresponding slot.
     * If the slot is already occupied, the old item is replaced (unequip event fires first).
     */
    public void equip(Item item) {
        if (item == null) {
            return;
        }

        ItemSlot slot = item.getSlot();
        Item previous = equipped.get(slot);

        if (previous != null) {
            equipped.remove(slot);
            fireEvent(GameEvent.builder(GameEventType.ITEM_UNEQUIPPED)
                    .put("itemName", previous.getName())
                    .put("slot", slot.name())
                    .build());
        }

        equipped.put(slot, item);
        fireEvent(GameEvent.builder(GameEventType.ITEM_EQUIPPED)
                .put("itemName", item.getName())
                .put("slot", slot.name())
                .put("rarity", item.getRarity().name())
                .build());
    }

    /**
     * Unequips the item in the given slot.
     * Returns the removed item, or null if the slot was empty.
     */
    public Item unequip(ItemSlot slot) {
        Item removed = equipped.remove(slot);
        if (removed != null) {
            fireEvent(GameEvent.builder(GameEventType.ITEM_UNEQUIPPED)
                    .put("itemName", removed.getName())
                    .put("slot", slot.name())
                    .build());
        }
        return removed;
    }

    /**
     * Returns the item equipped in the given slot, or null if empty.
     */
    public Item getEquipped(ItemSlot slot) {
        return equipped.get(slot);
    }

    /**
     * Computes the total stat bonus from all equipped items for the given stat.
     * The stat parameter should match ItemEffect constants (ATK, DEF, MAX_HP, etc.).
     */
    public int getTotalStatBonus(String stat) {
        int total = 0;
        for (Item item : equipped.values()) {
            for (ItemEffect effect : item.getEffects()) {
                if (stat.equals(effect.getStat())) {
                    total += effect.getValue();
                }
            }
        }
        return total;
    }

    /**
     * Returns the element override from the equipped weapon, or null if no override exists.
     * Only weapon-slot items can provide an element override.
     */
    public Element getElementOverride() {
        Item weapon = equipped.get(ItemSlot.WEAPON);
        if (weapon != null) {
            for (ItemEffect effect : weapon.getEffects()) {
                if (effect.getElementOverride() != null) {
                    return effect.getElementOverride();
                }
            }
        }
        return null;
    }

    /**
     * Returns true if any equipped accessory provides lifesteal (Vampire Pendant).
     */
    public boolean hasLifesteal() {
        Item accessory = equipped.get(ItemSlot.ACCESSORY);
        return accessory != null && ItemRegistry.isVampirePendant(accessory);
    }

    /**
     * Returns the lifesteal percentage (10 for Vampire Pendant, 0 otherwise).
     */
    public int getLifestealPercent() {
        return hasLifesteal() ? 10 : 0;
    }

    /**
     * Returns true if the player has the Plague Mask equipped (poison immunity).
     */
    public boolean hasPoisonImmunity() {
        Item accessory = equipped.get(ItemSlot.ACCESSORY);
        return accessory != null && ItemRegistry.isPlagueMask(accessory);
    }
}
