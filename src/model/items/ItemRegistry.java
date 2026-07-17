package model.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import model.skills.Element;

/**
 * Static registry of all items in the game.
 * Provides lookup methods by rarity, slot, and random selection.
 */
public class ItemRegistry {
    private static final List<Item> ALL_ITEMS;
    private static final Random RAND = new Random();

    static {
        List<Item> items = new ArrayList<>();

        // --- WEAPONS ---
        items.add(new Item("Rusty Sword", "A worn blade that still cuts.",
                ItemRarity.COMMON, ItemSlot.WEAPON,
                Arrays.asList(new ItemEffect(ItemEffect.ATK, 5))));

        items.add(new Item("Iron Mace", "Heavy but reliable.",
                ItemRarity.COMMON, ItemSlot.WEAPON,
                Arrays.asList(new ItemEffect(ItemEffect.ATK, 6))));

        items.add(new Item("Flame Blade", "Burns with elemental fire.",
                ItemRarity.RARE, ItemSlot.WEAPON,
                Arrays.asList(new ItemEffect(ItemEffect.ATK, 10, Element.FIRE))));

        items.add(new Item("Plague Dagger", "Drips with toxic venom.",
                ItemRarity.RARE, ItemSlot.WEAPON,
                Arrays.asList(new ItemEffect(ItemEffect.ATK, 8, Element.POISON))));

        items.add(new Item("Shadow Blade", "Forged in darkness.",
                ItemRarity.EPIC, ItemSlot.WEAPON,
                Arrays.asList(new ItemEffect(ItemEffect.ATK, 12, Element.DARK))));

        items.add(new Item("Warhammer", "Crushes armor and bone alike.",
                ItemRarity.EPIC, ItemSlot.WEAPON,
                Arrays.asList(new ItemEffect(ItemEffect.ATK, 14))));

        items.add(new Item("Holy Avenger", "Blessed blade of divine wrath.",
                ItemRarity.LEGENDARY, ItemSlot.WEAPON,
                Arrays.asList(
                        new ItemEffect(ItemEffect.ATK, 15, Element.HOLY),
                        new ItemEffect(ItemEffect.CRIT_CHANCE, 10))));

        // --- ARMOR ---
        items.add(new Item("Leather Vest", "Basic protection from scratches.",
                ItemRarity.COMMON, ItemSlot.ARMOR,
                Arrays.asList(new ItemEffect(ItemEffect.DEF, 5))));

        items.add(new Item("Padded Tunic", "Softens blows slightly.",
                ItemRarity.COMMON, ItemSlot.ARMOR,
                Arrays.asList(new ItemEffect(ItemEffect.DEF, 4),
                        new ItemEffect(ItemEffect.MAX_HP, 10))));

        items.add(new Item("Bone Plate", "Crafted from fallen foes.",
                ItemRarity.RARE, ItemSlot.ARMOR,
                Arrays.asList(new ItemEffect(ItemEffect.DEF, 10))));

        items.add(new Item("Enchanted Robe", "Woven with arcane threads.",
                ItemRarity.RARE, ItemSlot.ARMOR,
                Arrays.asList(new ItemEffect(ItemEffect.DEF, 5),
                        new ItemEffect(ItemEffect.MAX_MP, 20))));

        items.add(new Item("Knight's Plate", "Heavy but nearly impenetrable.",
                ItemRarity.EPIC, ItemSlot.ARMOR,
                Arrays.asList(new ItemEffect(ItemEffect.DEF, 12),
                        new ItemEffect(ItemEffect.MAX_HP, 20))));

        items.add(new Item("Paladin's Guard", "Radiates holy protection.",
                ItemRarity.LEGENDARY, ItemSlot.ARMOR,
                Arrays.asList(new ItemEffect(ItemEffect.DEF, 15),
                        new ItemEffect(ItemEffect.MAX_HP, 30))));

        // --- ACCESSORIES ---
        items.add(new Item("Mana Crystal", "Pulses with arcane energy.",
                ItemRarity.RARE, ItemSlot.ACCESSORY,
                Arrays.asList(new ItemEffect(ItemEffect.MAX_MP, 15),
                        new ItemEffect(ItemEffect.SPELL_COST_REDUCTION, 2))));

        items.add(new Item("Critical Ring", "Sharpens your killer instinct.",
                ItemRarity.RARE, ItemSlot.ACCESSORY,
                Arrays.asList(new ItemEffect(ItemEffect.CRIT_CHANCE, 10))));

        items.add(new Item("Plague Mask", "Filters toxic fumes.",
                ItemRarity.EPIC, ItemSlot.ACCESSORY,
                Arrays.asList(new ItemEffect(ItemEffect.DEF, 3),
                        new ItemEffect(ItemEffect.MAX_HP, 15))));

        items.add(new Item("Vampire Pendant", "Drains life from your foes.",
                ItemRarity.EPIC, ItemSlot.ACCESSORY,
                Arrays.asList(new ItemEffect(ItemEffect.ATK, 5))));

        items.add(new Item("Lucky Charm", "Fortune favors the bold.",
                ItemRarity.COMMON, ItemSlot.ACCESSORY,
                Arrays.asList(new ItemEffect(ItemEffect.CRIT_CHANCE, 5))));

        items.add(new Item("Archmage Amulet", "Channels immense arcane power.",
                ItemRarity.LEGENDARY, ItemSlot.ACCESSORY,
                Arrays.asList(new ItemEffect(ItemEffect.MAX_MP, 30),
                        new ItemEffect(ItemEffect.SPELL_COST_REDUCTION, 5),
                        new ItemEffect(ItemEffect.CRIT_CHANCE, 5))));

        ALL_ITEMS = Collections.unmodifiableList(items);
    }

    public static List<Item> getAllItems() {
        return ALL_ITEMS;
    }

    public static List<Item> getByRarity(ItemRarity rarity) {
        return ALL_ITEMS.stream()
                .filter(item -> item.getRarity() == rarity)
                .collect(Collectors.toList());
    }

    public static List<Item> getBySlot(ItemSlot slot) {
        return ALL_ITEMS.stream()
                .filter(item -> item.getSlot() == slot)
                .collect(Collectors.toList());
    }

    public static Item getRandomByRarity(ItemRarity rarity) {
        List<Item> pool = getByRarity(rarity);
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(RAND.nextInt(pool.size()));
    }

    /**
     * Returns a random item from the specified rarity and slot combination.
     * Returns null if no matching items exist.
     */
    public static Item getRandomByRarityAndSlot(ItemRarity rarity, ItemSlot slot) {
        List<Item> pool = ALL_ITEMS.stream()
                .filter(item -> item.getRarity() == rarity && item.getSlot() == slot)
                .collect(Collectors.toList());
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(RAND.nextInt(pool.size()));
    }

    /**
     * Checks if an item is the Vampire Pendant (for lifesteal logic).
     */
    public static boolean isVampirePendant(Item item) {
        return "Vampire Pendant".equals(item.getName());
    }

    /**
     * Checks if an item is the Plague Mask (for poison immunity logic).
     */
    public static boolean isPlagueMask(Item item) {
        return "Plague Mask".equals(item.getName());
    }
}
