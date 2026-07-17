package view.sprites;

import com.badlogic.gdx.graphics.Color;

/**
 * Consistent NES-style 8-bit color palette (16-32 colors) used throughout
 * the game for pixel art sprite generation.
 */
public final class ColorPalette {
    private ColorPalette() {}

    // --- Background & Environment ---
    public static final Color BACKGROUND = fromHex(0x0D0D1AFF);
    public static final Color DUNGEON_WALL = fromHex(0x2A2A3AFF);
    public static final Color DUNGEON_FLOOR = fromHex(0x3D3D4DFF);

    // --- Player ---
    public static final Color PLAYER_BLUE = fromHex(0x4A6FA5FF);
    public static final Color PLAYER_ARMOR = fromHex(0xC0C0C0FF);

    // --- Goblins ---
    public static final Color GOBLIN_GREEN = fromHex(0x2D8B2DFF);
    public static final Color GOBLIN_BROWN = fromHex(0x8B5A2BFF);

    // --- Skeletons ---
    public static final Color BONE_WHITE = fromHex(0xE8E8D0FF);
    public static final Color BONE_DARK = fromHex(0x404040FF);

    // --- Lich ---
    public static final Color LICH_PURPLE = fromHex(0x4A0080FF);
    public static final Color LICH_EYES = fromHex(0x00FF00FF);

    // --- Fire Spells ---
    public static final Color FIRE_ORANGE = fromHex(0xFF6600FF);
    public static final Color FIRE_YELLOW = fromHex(0xFFD700FF);

    // --- Holy Spells ---
    public static final Color HOLY_GOLD = fromHex(0xFFD700FF);
    public static final Color HOLY_WHITE = fromHex(0xFFFFFFFF);

    // --- Poison ---
    public static final Color POISON_GREEN = fromHex(0x00CC44FF);
    public static final Color POISON_DARK = fromHex(0x006622FF);

    // --- Dark Spells ---
    public static final Color DARK_PURPLE = fromHex(0x8B00FFFF);
    public static final Color DARK_BLACK = fromHex(0x1A001AFF);

    // --- HP Bar ---
    public static final Color HP_RED = fromHex(0xCC0000FF);
    public static final Color HP_BG = fromHex(0x660000FF);

    // --- MP Bar ---
    public static final Color MP_BLUE = fromHex(0x0066CCFF);
    public static final Color MP_BG = fromHex(0x003366FF);

    // --- UI ---
    public static final Color UI_BORDER = fromHex(0x8B8B7AFF);
    public static final Color TEXT_WHITE = fromHex(0xE8E8E8FF);
    public static final Color DAMAGE_RED = fromHex(0xFF3333FF);
    public static final Color HEAL_GREEN = fromHex(0x33FF33FF);
    public static final Color CRIT_YELLOW = fromHex(0xFFFF00FF);

    // --- Additional utility colors ---
    public static final Color SHIELD_BLUE = fromHex(0x3399FFFF);
    public static final Color STUN_YELLOW = fromHex(0xFFDD00FF);
    public static final Color ENRAGE_RED = fromHex(0xFF2200FF);
    public static final Color CURSE_PURPLE = fromHex(0x9900CCFF);
    public static final Color REGEN_GREEN = fromHex(0x22CC22FF);
    public static final Color CROWN_GOLD = fromHex(0xFFCC00FF);
    public static final Color SKIN_GREEN_LIGHT = fromHex(0x44AA44FF);
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    /**
     * Creates a Color from a packed RGBA8888 hex integer.
     */
    private static Color fromHex(int rgba8888) {
        float r = ((rgba8888 >>> 24) & 0xFF) / 255f;
        float g = ((rgba8888 >>> 16) & 0xFF) / 255f;
        float b = ((rgba8888 >>> 8) & 0xFF) / 255f;
        float a = (rgba8888 & 0xFF) / 255f;
        return new Color(r, g, b, a);
    }
}
