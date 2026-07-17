package model.items;

import java.util.Collections;
import java.util.List;

/**
 * Represents an item that can be equipped by the player.
 * Immutable data object - all fields set at construction.
 */
public class Item {
    private final String name;
    private final String description;
    private final ItemRarity rarity;
    private final ItemSlot slot;
    private final List<ItemEffect> effects;

    public Item(String name, String description, ItemRarity rarity, ItemSlot slot, List<ItemEffect> effects) {
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.slot = slot;
        this.effects = Collections.unmodifiableList(effects);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemRarity getRarity() {
        return rarity;
    }

    public ItemSlot getSlot() {
        return slot;
    }

    public List<ItemEffect> getEffects() {
        return effects;
    }
}
