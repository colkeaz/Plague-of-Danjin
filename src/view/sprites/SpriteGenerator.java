package view.sprites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

public class SpriteGenerator implements Disposable {
    private final List<Texture> managedTextures;
    private final Map<String, TextureRegion[][]> entitySprites;
    private final Map<String, TextureRegion> uiSprites;
    private final Map<String, TextureRegion[]> uiAnimatedSprites;

    public SpriteGenerator() {
        managedTextures = new ArrayList<>();
        entitySprites = new HashMap<>();
        uiSprites = new HashMap<>();
        uiAnimatedSprites = new HashMap<>();
    }

    public void generateAll() {
        generatePlayerSprite();
        generateKnightSprite();
        generateMageSprite();
        generateRogueSprite();
        generateGoblinSprite();
        generatePlagueGoblinSprite();
        generateSkeletonSprite();
        generateShieldedSkeletonSprite();
        generateGoblinKingSprite();
        generateGoblinChieftainSprite();
        generateBoneColossusSprite();
        generateLichSprite();
        generatePlagueElementalSprite();
        generateThornmotherSprite();
        generateHpBarFrame();
        generateMpBarFrame();
        generateStatusIcons();
        generateChestSprites();
        generateMenuFrame();
        generateWaveBanner();
        generateElementIcons();
        generateBackgroundTile();
        generateParticleTextures();
        generateWorldMapNodes();
    }

    public TextureRegion[][] getEntitySprites(String entityName) {
        return entitySprites.get(entityName);
    }

    public TextureRegion getUiSprite(String key) {
        return uiSprites.get(key);
    }

    public TextureRegion[] getUiAnimatedSprite(String key) {
        return uiAnimatedSprites.get(key);
    }

    public Map<AnimationState, TextureRegion[]> getAnimationFrames(String entityName) {
        TextureRegion[][] sprites = entitySprites.get(entityName);
        if (sprites == null) return new HashMap<>();
        Map<AnimationState, TextureRegion[]> map = new HashMap<>();
        if (sprites.length > 0) map.put(AnimationState.IDLE, sprites[0]);
        if (sprites.length > 1) map.put(AnimationState.ATTACKING, sprites[1]);
        if (sprites.length > 2) map.put(AnimationState.HURT, sprites[2]);
        if (sprites.length > 3) map.put(AnimationState.DYING, sprites[3]);
        if (sprites.length > 4) map.put(AnimationState.CASTING, sprites[4]);
        return map;
    }

    private TextureRegion createRegionFromPixmap(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        managedTextures.add(texture);
        pixmap.dispose();
        return new TextureRegion(texture);
    }

    private void setPixel(Pixmap pm, int x, int y, Color c) {
        pm.setColor(c);
        pm.drawPixel(x, y);
    }

    private Pixmap createPixmap(int w, int h) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(ColorPalette.TRANSPARENT);
        pm.fill();
        return pm;
    }

    // ===== PLAYER SPRITE (24x32 knight/warrior) =====
    private void generatePlayerSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2]; // IDLE
        frames[1] = new TextureRegion[3]; // ATTACKING
        frames[2] = new TextureRegion[2]; // HURT
        frames[3] = new TextureRegion[3]; // DYING
        frames[4] = new TextureRegion[2]; // CASTING

        // IDLE frame 1
        Pixmap pm = createPixmap(24, 32);
        drawPlayerBase(pm, 0);
        frames[0][0] = createRegionFromPixmap(pm);

        // IDLE frame 2 (bob down 1px)
        pm = createPixmap(24, 32);
        drawPlayerBase(pm, 1);
        frames[0][1] = createRegionFromPixmap(pm);

        // ATTACK frames
        pm = createPixmap(24, 32);
        drawPlayerAttack(pm, 0);
        frames[1][0] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        drawPlayerAttack(pm, 1);
        frames[1][1] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        drawPlayerAttack(pm, 2);
        frames[1][2] = createRegionFromPixmap(pm);

        // HURT frames
        pm = createPixmap(24, 32);
        drawPlayerHurt(pm, 0);
        frames[2][0] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        drawPlayerHurt(pm, 1);
        frames[2][1] = createRegionFromPixmap(pm);

        // DYING frames
        pm = createPixmap(24, 32);
        drawPlayerDying(pm, 0);
        frames[3][0] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        drawPlayerDying(pm, 1);
        frames[3][1] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        drawPlayerDying(pm, 2);
        frames[3][2] = createRegionFromPixmap(pm);

        // CASTING frames
        pm = createPixmap(24, 32);
        drawPlayerBase(pm, 0);
        frames[4][0] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        drawPlayerBase(pm, 1);
        frames[4][1] = createRegionFromPixmap(pm);

        entitySprites.put("player", frames);
    }

    private void drawPlayerBase(Pixmap pm, int yOff) {
        Color blue = ColorPalette.PLAYER_BLUE;
        Color armor = ColorPalette.PLAYER_ARMOR;
        Color dark = ColorPalette.BONE_DARK;
        // Helmet
        for (int x = 9; x <= 14; x++) { setPixel(pm, x, 2+yOff, armor); }
        for (int x = 8; x <= 15; x++) { setPixel(pm, x, 3+yOff, armor); }
        for (int x = 8; x <= 15; x++) { setPixel(pm, x, 4+yOff, armor); }
        // Visor
        setPixel(pm, 9, 5+yOff, dark); setPixel(pm, 10, 5+yOff, dark);
        setPixel(pm, 13, 5+yOff, dark); setPixel(pm, 14, 5+yOff, dark);
        for (int x = 8; x <= 15; x++) { setPixel(pm, x, 5+yOff, x>=9 && x<=14 ? dark : armor); }
        for (int x = 8; x <= 15; x++) { setPixel(pm, x, 6+yOff, armor); }
        // Neck
        for (int x = 10; x <= 13; x++) { setPixel(pm, x, 7+yOff, blue); }
        // Shoulders and torso
        for (int x = 6; x <= 17; x++) { setPixel(pm, x, 8+yOff, armor); }
        for (int x = 6; x <= 17; x++) { setPixel(pm, x, 9+yOff, armor); }
        for (int x = 7; x <= 16; x++) { setPixel(pm, x, 10+yOff, blue); }
        for (int x = 7; x <= 16; x++) { setPixel(pm, x, 11+yOff, blue); }
        for (int x = 8; x <= 15; x++) { setPixel(pm, x, 12+yOff, blue); }
        for (int x = 8; x <= 15; x++) { setPixel(pm, x, 13+yOff, blue); }
        for (int x = 8; x <= 15; x++) { setPixel(pm, x, 14+yOff, blue); }
        // Belt
        for (int x = 9; x <= 14; x++) { setPixel(pm, x, 15+yOff, dark); }
        // Legs
        for (int x = 9; x <= 11; x++) { setPixel(pm, x, 16+yOff, blue); }
        for (int x = 12; x <= 14; x++) { setPixel(pm, x, 16+yOff, blue); }
        for (int x = 9; x <= 11; x++) { setPixel(pm, x, 17+yOff, blue); }
        for (int x = 12; x <= 14; x++) { setPixel(pm, x, 17+yOff, blue); }
        for (int x = 9; x <= 11; x++) { setPixel(pm, x, 18+yOff, blue); }
        for (int x = 12; x <= 14; x++) { setPixel(pm, x, 18+yOff, blue); }
        for (int x = 9; x <= 11; x++) { setPixel(pm, x, 19+yOff, blue); }
        for (int x = 12; x <= 14; x++) { setPixel(pm, x, 19+yOff, blue); }
        // Boots
        for (int x = 8; x <= 11; x++) { setPixel(pm, x, 20+yOff, dark); }
        for (int x = 12; x <= 15; x++) { setPixel(pm, x, 20+yOff, dark); }
        // Sword (right side)
        setPixel(pm, 18, 8+yOff, armor); setPixel(pm, 18, 9+yOff, armor);
        setPixel(pm, 18, 10+yOff, armor); setPixel(pm, 18, 11+yOff, armor);
        setPixel(pm, 18, 12+yOff, armor); setPixel(pm, 18, 13+yOff, armor);
        setPixel(pm, 17, 9+yOff, dark); setPixel(pm, 19, 9+yOff, dark);
    }

    private void drawPlayerAttack(Pixmap pm, int frame) {
        drawPlayerBase(pm, 0);
        Color armor = ColorPalette.PLAYER_ARMOR;
        // Extend sword further right based on frame
        int swordX = 19 + frame * 2;
        for (int y = 6; y <= 12; y++) {
            if (swordX < 24) setPixel(pm, swordX, y, armor);
            if (swordX + 1 < 24) setPixel(pm, swordX + 1, y, armor);
        }
    }

    private void drawPlayerHurt(Pixmap pm, int frame) {
        Color white = ColorPalette.HOLY_WHITE;
        if (frame == 0) {
            // Flash white silhouette
            for (int x = 8; x <= 15; x++) for (int y = 2; y <= 20; y++) setPixel(pm, x, y, white);
        } else {
            drawPlayerBase(pm, 0);
        }
    }

    private void drawPlayerDying(Pixmap pm, int frame) {
        Color blue = ColorPalette.PLAYER_BLUE;
        Color armor = ColorPalette.PLAYER_ARMOR;
        if (frame == 0) {
            drawPlayerBase(pm, 0);
        } else if (frame == 1) {
            // Leaning/falling
            for (int x = 7; x <= 16; x++) for (int y = 10; y <= 22; y++) setPixel(pm, x, y, blue);
            for (int x = 8; x <= 14; x++) setPixel(pm, x, 9, armor);
        } else {
            // Collapsed on ground
            for (int x = 5; x <= 18; x++) { setPixel(pm, x, 26, armor); setPixel(pm, x, 27, blue); }
        }
    }

    // ===== KNIGHT CLASS SPRITE (24x32 heavy plate armor with shield) =====
    private void generateKnightSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2]; // IDLE
        frames[1] = new TextureRegion[3]; // ATTACKING
        frames[2] = new TextureRegion[2]; // HURT
        frames[3] = new TextureRegion[3]; // DYING
        frames[4] = new TextureRegion[2]; // CASTING

        // IDLE frames
        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(24, 32);
            drawKnightBase(pm, i);
            frames[0][i] = createRegionFromPixmap(pm);
        }

        // ATTACK frames
        for (int f = 0; f < 3; f++) {
            Pixmap pm = createPixmap(24, 32);
            drawKnightBase(pm, 0);
            Color silver = ColorPalette.KNIGHT_SILVER;
            // Sword extends further each frame
            int swordX = 19 + f * 2;
            for (int y = 5; y <= 13; y++) {
                if (swordX < 24) setPixel(pm, swordX, y, silver);
                if (swordX + 1 < 24) setPixel(pm, swordX + 1, y, silver);
            }
            frames[1][f] = createRegionFromPixmap(pm);
        }

        // HURT frames
        Pixmap pm = createPixmap(24, 32);
        for (int x = 7; x <= 16; x++) for (int y = 2; y <= 21; y++) setPixel(pm, x, y, ColorPalette.HOLY_WHITE);
        frames[2][0] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        drawKnightBase(pm, 0);
        frames[2][1] = createRegionFromPixmap(pm);

        // DYING frames
        pm = createPixmap(24, 32);
        drawKnightBase(pm, 0);
        frames[3][0] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        Color kBlue = ColorPalette.KNIGHT_BLUE;
        for (int x = 7; x <= 16; x++) for (int y = 10; y <= 22; y++) setPixel(pm, x, y, kBlue);
        for (int x = 8; x <= 14; x++) setPixel(pm, x, 9, ColorPalette.KNIGHT_SILVER);
        frames[3][1] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        for (int x = 5; x <= 18; x++) { setPixel(pm, x, 26, ColorPalette.KNIGHT_SILVER); setPixel(pm, x, 27, kBlue); }
        frames[3][2] = createRegionFromPixmap(pm);

        // CASTING frames
        for (int i = 0; i < 2; i++) {
            pm = createPixmap(24, 32);
            drawKnightBase(pm, i);
            frames[4][i] = createRegionFromPixmap(pm);
        }

        entitySprites.put("player_knight", frames);
    }

    private void drawKnightBase(Pixmap pm, int yOff) {
        Color blue = ColorPalette.KNIGHT_BLUE;
        Color silver = ColorPalette.KNIGHT_SILVER;
        Color dark = ColorPalette.BONE_DARK;
        // Helmet (full plate helm)
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 2+yOff, silver);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 3+yOff, silver);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 4+yOff, silver);
        // Visor slit
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 5+yOff, dark);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 6+yOff, silver);
        // Crest on top
        setPixel(pm, 11, 1+yOff, blue); setPixel(pm, 12, 1+yOff, blue);
        // Neck guard
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 7+yOff, silver);
        // Pauldrons (large shoulder plates)
        for (int x = 5; x <= 7; x++) { setPixel(pm, x, 8+yOff, silver); setPixel(pm, x, 9+yOff, silver); }
        for (int x = 16; x <= 18; x++) { setPixel(pm, x, 8+yOff, silver); setPixel(pm, x, 9+yOff, silver); }
        // Chest plate
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 8+yOff, silver);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 9+yOff, silver);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 10+yOff, blue);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 11+yOff, blue);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 12+yOff, blue);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 13+yOff, blue);
        // Belt
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 14+yOff, dark);
        // Tassets (plate skirt)
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 15+yOff, silver);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 16+yOff, silver);
        // Legs (plate greaves)
        for (int x = 9; x <= 11; x++) for (int y = 17; y <= 19; y++) setPixel(pm, x, y+yOff, blue);
        for (int x = 12; x <= 14; x++) for (int y = 17; y <= 19; y++) setPixel(pm, x, y+yOff, blue);
        // Boots
        for (int x = 8; x <= 11; x++) setPixel(pm, x, 20+yOff, dark);
        for (int x = 12; x <= 15; x++) setPixel(pm, x, 20+yOff, dark);
        // Shield (left side)
        for (int y = 9; y <= 15; y++) { setPixel(pm, 4, y+yOff, silver); setPixel(pm, 5, y+yOff, blue); setPixel(pm, 6, y+yOff, blue); }
        setPixel(pm, 5, 12+yOff, silver); // Shield emblem center
        // Sword (right side)
        for (int y = 7; y <= 14; y++) setPixel(pm, 18, y+yOff, silver);
        setPixel(pm, 17, 8+yOff, dark); setPixel(pm, 19, 8+yOff, dark); // crossguard
    }

    // ===== MAGE CLASS SPRITE (24x32 robes with staff) =====
    private void generateMageSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2]; // IDLE
        frames[1] = new TextureRegion[3]; // ATTACKING
        frames[2] = new TextureRegion[2]; // HURT
        frames[3] = new TextureRegion[3]; // DYING
        frames[4] = new TextureRegion[2]; // CASTING

        // IDLE frames
        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(24, 32);
            drawMageBase(pm, i);
            frames[0][i] = createRegionFromPixmap(pm);
        }

        // ATTACK frames (staff glows)
        for (int f = 0; f < 3; f++) {
            Pixmap pm = createPixmap(24, 32);
            drawMageBase(pm, 0);
            // Staff orb glows more intensely
            Color glow = (f == 1) ? ColorPalette.HOLY_GOLD : ColorPalette.MAGE_GOLD;
            setPixel(pm, 19, 2, glow); setPixel(pm, 20, 2, glow);
            setPixel(pm, 19, 3, glow); setPixel(pm, 20, 3, glow);
            if (f == 2) { setPixel(pm, 18, 2, glow); setPixel(pm, 21, 2, glow); }
            frames[1][f] = createRegionFromPixmap(pm);
        }

        // HURT frames
        Pixmap pm = createPixmap(24, 32);
        for (int x = 7; x <= 16; x++) for (int y = 2; y <= 24; y++) setPixel(pm, x, y, ColorPalette.HOLY_WHITE);
        frames[2][0] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        drawMageBase(pm, 0);
        frames[2][1] = createRegionFromPixmap(pm);

        // DYING frames
        pm = createPixmap(24, 32);
        drawMageBase(pm, 0);
        frames[3][0] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        Color mPurple = ColorPalette.MAGE_PURPLE;
        for (int x = 7; x <= 16; x++) for (int y = 10; y <= 24; y++) setPixel(pm, x, y, mPurple);
        frames[3][1] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        for (int x = 5; x <= 18; x++) { setPixel(pm, x, 27, mPurple); setPixel(pm, x, 28, mPurple); }
        frames[3][2] = createRegionFromPixmap(pm);

        // CASTING frames (arms raised, orb bright)
        for (int i = 0; i < 2; i++) {
            pm = createPixmap(24, 32);
            drawMageBase(pm, i);
            Color glow = (i == 0) ? ColorPalette.MAGE_GOLD : ColorPalette.FIRE_YELLOW;
            setPixel(pm, 19, 1, glow); setPixel(pm, 20, 1, glow);
            setPixel(pm, 18, 2, glow); setPixel(pm, 21, 2, glow);
            setPixel(pm, 19, 2, glow); setPixel(pm, 20, 2, glow);
            frames[4][i] = createRegionFromPixmap(pm);
        }

        entitySprites.put("player_mage", frames);
    }

    private void drawMageBase(Pixmap pm, int yOff) {
        Color purple = ColorPalette.MAGE_PURPLE;
        Color gold = ColorPalette.MAGE_GOLD;
        Color dark = ColorPalette.BONE_DARK;
        Color skin = ColorPalette.BONE_WHITE;
        // Hood
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 2+yOff, purple);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 3+yOff, purple);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 4+yOff, purple);
        // Face inside hood
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 5+yOff, dark);
        setPixel(pm, 10, 5+yOff, skin); setPixel(pm, 13, 5+yOff, skin); // eyes visible
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 6+yOff, purple);
        // Collar with gold trim
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 7+yOff, gold);
        // Robe body (flowing, wider at bottom)
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 8+yOff, purple);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 9+yOff, purple);
        for (int x = 7; x <= 16; x++) setPixel(pm, x, 10+yOff, purple);
        for (int x = 7; x <= 16; x++) setPixel(pm, x, 11+yOff, purple);
        // Gold belt/sash
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 12+yOff, gold);
        // Lower robe (flowing wider)
        for (int x = 7; x <= 16; x++) setPixel(pm, x, 13+yOff, purple);
        for (int x = 6; x <= 17; x++) setPixel(pm, x, 14+yOff, purple);
        for (int x = 6; x <= 17; x++) setPixel(pm, x, 15+yOff, purple);
        for (int x = 6; x <= 17; x++) setPixel(pm, x, 16+yOff, purple);
        for (int x = 6; x <= 17; x++) setPixel(pm, x, 17+yOff, purple);
        for (int x = 6; x <= 17; x++) setPixel(pm, x, 18+yOff, purple);
        for (int x = 6; x <= 17; x++) setPixel(pm, x, 19+yOff, purple);
        // Robe bottom hem with gold trim
        for (int x = 6; x <= 17; x++) setPixel(pm, x, 20+yOff, gold);
        // Sleeves (wide)
        for (int y = 9; y <= 12; y++) { setPixel(pm, 5, y+yOff, purple); setPixel(pm, 6, y+yOff, purple); }
        for (int y = 9; y <= 12; y++) { setPixel(pm, 17, y+yOff, purple); setPixel(pm, 18, y+yOff, purple); }
        // Staff (right side, tall)
        for (int y = 3; y <= 20; y++) setPixel(pm, 20, y+yOff, dark);
        // Staff orb at top
        setPixel(pm, 19, 3+yOff, gold); setPixel(pm, 20, 2+yOff, gold); setPixel(pm, 21, 3+yOff, gold);
        setPixel(pm, 20, 4+yOff, gold);
    }

    // ===== ROGUE CLASS SPRITE (24x32 hooded cloak with daggers) =====
    private void generateRogueSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2]; // IDLE
        frames[1] = new TextureRegion[3]; // ATTACKING
        frames[2] = new TextureRegion[2]; // HURT
        frames[3] = new TextureRegion[3]; // DYING
        frames[4] = new TextureRegion[2]; // CASTING

        // IDLE frames
        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(24, 32);
            drawRogueBase(pm, i);
            frames[0][i] = createRegionFromPixmap(pm);
        }

        // ATTACK frames (daggers slash outward)
        for (int f = 0; f < 3; f++) {
            Pixmap pm = createPixmap(24, 32);
            drawRogueBase(pm, 0);
            Color crimson = ColorPalette.ROGUE_CRIMSON;
            // Dagger slash arcs outward each frame
            int offset = f * 2;
            setPixel(pm, 19 + offset, 9, crimson);
            setPixel(pm, 19 + offset, 10, crimson);
            setPixel(pm, 19 + offset, 11, crimson);
            if (4 - offset >= 0) {
                setPixel(pm, 4 - offset, 9, crimson);
                setPixel(pm, 4 - offset, 10, crimson);
                setPixel(pm, 4 - offset, 11, crimson);
            }
            frames[1][f] = createRegionFromPixmap(pm);
        }

        // HURT frames
        Pixmap pm = createPixmap(24, 32);
        for (int x = 7; x <= 16; x++) for (int y = 2; y <= 21; y++) setPixel(pm, x, y, ColorPalette.HOLY_WHITE);
        frames[2][0] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        drawRogueBase(pm, 0);
        frames[2][1] = createRegionFromPixmap(pm);

        // DYING frames
        pm = createPixmap(24, 32);
        drawRogueBase(pm, 0);
        frames[3][0] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        Color rGreen = ColorPalette.ROGUE_GREEN;
        for (int x = 7; x <= 16; x++) for (int y = 10; y <= 22; y++) setPixel(pm, x, y, rGreen);
        frames[3][1] = createRegionFromPixmap(pm);
        pm = createPixmap(24, 32);
        for (int x = 5; x <= 18; x++) { setPixel(pm, x, 26, rGreen); setPixel(pm, x, 27, ColorPalette.ROGUE_CRIMSON); }
        frames[3][2] = createRegionFromPixmap(pm);

        // CASTING frames
        for (int i = 0; i < 2; i++) {
            pm = createPixmap(24, 32);
            drawRogueBase(pm, i);
            frames[4][i] = createRegionFromPixmap(pm);
        }

        entitySprites.put("player_rogue", frames);
    }

    private void drawRogueBase(Pixmap pm, int yOff) {
        Color green = ColorPalette.ROGUE_GREEN;
        Color crimson = ColorPalette.ROGUE_CRIMSON;
        Color dark = ColorPalette.BONE_DARK;
        Color skin = ColorPalette.BONE_WHITE;
        // Hood (pointed)
        setPixel(pm, 11, 1+yOff, green); setPixel(pm, 12, 1+yOff, green);
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 2+yOff, green);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 3+yOff, green);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 4+yOff, green);
        // Face (shadowed, eyes visible as crimson dots)
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 5+yOff, dark);
        setPixel(pm, 10, 5+yOff, crimson); setPixel(pm, 13, 5+yOff, crimson); // glowing eyes
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 6+yOff, green);
        // Scarf/bandana
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 7+yOff, crimson);
        // Cloak/body (slim silhouette)
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 8+yOff, green);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 9+yOff, green);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 10+yOff, green);
        for (int x = 8; x <= 15; x++) setPixel(pm, x, 11+yOff, green);
        // Belt with crimson accent
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 12+yOff, crimson);
        // Lower body (tight, agile)
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 13+yOff, dark);
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 14+yOff, dark);
        for (int x = 9; x <= 14; x++) setPixel(pm, x, 15+yOff, dark);
        // Cloak tails (flowing behind)
        setPixel(pm, 7, 10+yOff, green); setPixel(pm, 7, 11+yOff, green);
        setPixel(pm, 6, 12+yOff, green); setPixel(pm, 6, 13+yOff, green);
        setPixel(pm, 16, 10+yOff, green); setPixel(pm, 16, 11+yOff, green);
        setPixel(pm, 17, 12+yOff, green); setPixel(pm, 17, 13+yOff, green);
        // Legs (lean)
        for (int x = 9; x <= 11; x++) for (int y = 16; y <= 19; y++) setPixel(pm, x, y+yOff, dark);
        for (int x = 12; x <= 14; x++) for (int y = 16; y <= 19; y++) setPixel(pm, x, y+yOff, dark);
        // Boots (slim)
        for (int x = 9; x <= 11; x++) setPixel(pm, x, 20+yOff, crimson);
        for (int x = 12; x <= 14; x++) setPixel(pm, x, 20+yOff, crimson);
        // Twin daggers (one each side)
        setPixel(pm, 5, 9+yOff, skin); setPixel(pm, 5, 10+yOff, skin); setPixel(pm, 5, 11+yOff, skin);
        setPixel(pm, 18, 9+yOff, skin); setPixel(pm, 18, 10+yOff, skin); setPixel(pm, 18, 11+yOff, skin);
        // Dagger handles
        setPixel(pm, 5, 12+yOff, crimson); setPixel(pm, 18, 12+yOff, crimson);
    }

    // ===== GOBLIN SPRITE (16x16 small green creature) =====
    private void generateGoblinSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = generateSimpleEntity(16, 16, ColorPalette.GOBLIN_GREEN, ColorPalette.GOBLIN_BROWN, false, false);
        frames[1] = generateAttackFrames(16, 16, ColorPalette.GOBLIN_GREEN, ColorPalette.GOBLIN_BROWN);
        frames[2] = generateHurtFrames(16, 16, ColorPalette.GOBLIN_GREEN);
        frames[3] = generateDeathFrames(16, 16, ColorPalette.GOBLIN_GREEN);
        frames[4] = generateCastFrames(16, 16, ColorPalette.GOBLIN_GREEN);
        entitySprites.put("goblin", frames);
    }

    // ===== PLAGUE GOBLIN (16x16 green with purple aura) =====
    private void generatePlagueGoblinSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2];
        frames[1] = generateAttackFrames(16, 16, ColorPalette.GOBLIN_GREEN, ColorPalette.POISON_GREEN);
        frames[2] = generateHurtFrames(16, 16, ColorPalette.GOBLIN_GREEN);
        frames[3] = generateDeathFrames(16, 16, ColorPalette.GOBLIN_GREEN);
        frames[4] = generateCastFrames(16, 16, ColorPalette.GOBLIN_GREEN);

        // IDLE with poison drips
        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(16, 16);
            drawGoblinBody(pm, i, ColorPalette.GOBLIN_GREEN);
            // Poison aura drips
            Color poison = ColorPalette.POISON_GREEN;
            setPixel(pm, 3, 12 + i, poison);
            setPixel(pm, 7, 13 + i, poison);
            setPixel(pm, 11, 11 + i, poison);
            setPixel(pm, 5, 14, poison);
            setPixel(pm, 9, 14 + i, poison);
            // Purple tint on edges
            Color purple = ColorPalette.DARK_PURPLE;
            setPixel(pm, 2, 5, purple); setPixel(pm, 13, 5, purple);
            setPixel(pm, 1, 8, purple); setPixel(pm, 14, 8, purple);
            frames[0][i] = createRegionFromPixmap(pm);
        }
        entitySprites.put("plague_goblin", frames);
    }

    // ===== SKELETON (16x16 bone white with sword) =====
    private void generateSkeletonSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2];
        frames[1] = generateAttackFrames(16, 16, ColorPalette.BONE_WHITE, ColorPalette.PLAYER_ARMOR);
        frames[2] = generateHurtFrames(16, 16, ColorPalette.BONE_WHITE);
        frames[3] = generateDeathFrames(16, 16, ColorPalette.BONE_WHITE);
        frames[4] = generateCastFrames(16, 16, ColorPalette.BONE_WHITE);

        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(16, 16);
            drawSkeletonBody(pm, i);
            frames[0][i] = createRegionFromPixmap(pm);
        }
        entitySprites.put("skeleton", frames);
    }

    // ===== SHIELDED SKELETON (16x16 skeleton + shield) =====
    private void generateShieldedSkeletonSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2];
        frames[1] = generateAttackFrames(16, 16, ColorPalette.BONE_WHITE, ColorPalette.PLAYER_ARMOR);
        frames[2] = generateHurtFrames(16, 16, ColorPalette.BONE_WHITE);
        frames[3] = generateDeathFrames(16, 16, ColorPalette.BONE_WHITE);
        frames[4] = generateCastFrames(16, 16, ColorPalette.BONE_WHITE);

        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(16, 16);
            drawSkeletonBody(pm, i);
            // Draw shield in front (left side)
            Color shield = ColorPalette.SHIELD_BLUE;
            Color border = ColorPalette.BONE_DARK;
            for (int y = 5; y <= 11; y++) { setPixel(pm, 2, y, border); setPixel(pm, 3, y, shield); setPixel(pm, 4, y, shield); setPixel(pm, 5, y, border); }
            setPixel(pm, 3, 4, border); setPixel(pm, 4, 4, border);
            setPixel(pm, 3, 12, border); setPixel(pm, 4, 12, border);
            frames[0][i] = createRegionFromPixmap(pm);
        }
        entitySprites.put("shielded_skeleton", frames);
    }

    // ===== GOBLIN KING (32x32 larger goblin with crown) =====
    private void generateGoblinKingSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2];
        frames[1] = generateAttackFrames(32, 32, ColorPalette.GOBLIN_GREEN, ColorPalette.CROWN_GOLD);
        frames[2] = generateHurtFrames(32, 32, ColorPalette.GOBLIN_GREEN);
        frames[3] = generateDeathFrames(32, 32, ColorPalette.GOBLIN_GREEN);
        frames[4] = generateCastFrames(32, 32, ColorPalette.GOBLIN_GREEN);

        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(32, 32);
            drawGoblinKingBody(pm, i);
            frames[0][i] = createRegionFromPixmap(pm);
        }
        entitySprites.put("goblin_king", frames);
    }

    // ===== GOBLIN CHIEFTAIN (24x24 mid-size with war paint) =====
    private void generateGoblinChieftainSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2];
        frames[1] = generateAttackFrames(24, 24, ColorPalette.GOBLIN_GREEN, ColorPalette.ENRAGE_RED);
        frames[2] = generateHurtFrames(24, 24, ColorPalette.GOBLIN_GREEN);
        frames[3] = generateDeathFrames(24, 24, ColorPalette.GOBLIN_GREEN);
        frames[4] = generateCastFrames(24, 24, ColorPalette.GOBLIN_GREEN);

        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(24, 24);
            drawGoblinChieftainBody(pm, i);
            frames[0][i] = createRegionFromPixmap(pm);
        }
        entitySprites.put("goblin_chieftain", frames);
    }

    // ===== BONE COLOSSUS (32x32 large skeletal giant) =====
    private void generateBoneColossusSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2];
        frames[1] = generateAttackFrames(32, 32, ColorPalette.BONE_WHITE, ColorPalette.BONE_DARK);
        frames[2] = generateHurtFrames(32, 32, ColorPalette.BONE_WHITE);
        frames[3] = generateDeathFrames(32, 32, ColorPalette.BONE_WHITE);
        frames[4] = generateCastFrames(32, 32, ColorPalette.BONE_WHITE);

        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(32, 32);
            drawBoneColossusBody(pm, i);
            frames[0][i] = createRegionFromPixmap(pm);
        }
        entitySprites.put("bone_colossus", frames);
    }

    // ===== LICH (24x32 hooded figure with staff) =====
    private void generateLichSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2];
        frames[1] = generateAttackFrames(24, 32, ColorPalette.LICH_PURPLE, ColorPalette.LICH_EYES);
        frames[2] = generateHurtFrames(24, 32, ColorPalette.LICH_PURPLE);
        frames[3] = generateDeathFrames(24, 32, ColorPalette.LICH_PURPLE);
        frames[4] = generateCastFrames(24, 32, ColorPalette.LICH_PURPLE);

        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(24, 32);
            drawLichBody(pm, i);
            frames[0][i] = createRegionFromPixmap(pm);
        }
        entitySprites.put("lich", frames);
    }

    // ===== HELPER: Draw goblin body (16x16) =====
    private void drawGoblinBody(Pixmap pm, int yOff, Color bodyColor) {
        Color brown = ColorPalette.GOBLIN_BROWN;
        Color dark = ColorPalette.BONE_DARK;
        // Head with pointy ears
        setPixel(pm, 6, 1+yOff, bodyColor); setPixel(pm, 9, 1+yOff, bodyColor); // ear tips
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 2+yOff, bodyColor);
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 3+yOff, bodyColor);
        // Eyes
        setPixel(pm, 6, 3+yOff, dark); setPixel(pm, 9, 3+yOff, dark);
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 4+yOff, bodyColor);
        // Mouth
        setPixel(pm, 7, 4+yOff, dark); setPixel(pm, 8, 4+yOff, dark);
        // Body
        for (int x = 6; x <= 9; x++) setPixel(pm, x, 5+yOff, brown);
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 6+yOff, brown);
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 7+yOff, brown);
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 8+yOff, brown);
        for (int x = 6; x <= 9; x++) setPixel(pm, x, 9+yOff, brown);
        // Arms
        setPixel(pm, 4, 6+yOff, bodyColor); setPixel(pm, 4, 7+yOff, bodyColor);
        setPixel(pm, 11, 6+yOff, bodyColor); setPixel(pm, 11, 7+yOff, bodyColor);
        // Legs
        setPixel(pm, 6, 10+yOff, bodyColor); setPixel(pm, 7, 10+yOff, bodyColor);
        setPixel(pm, 8, 10+yOff, bodyColor); setPixel(pm, 9, 10+yOff, bodyColor);
        setPixel(pm, 6, 11+yOff, bodyColor); setPixel(pm, 9, 11+yOff, bodyColor);
        // Feet
        setPixel(pm, 5, 12+yOff, dark); setPixel(pm, 6, 12+yOff, dark);
        setPixel(pm, 9, 12+yOff, dark); setPixel(pm, 10, 12+yOff, dark);
    }

    // ===== HELPER: Draw skeleton body (16x16) =====
    private void drawSkeletonBody(Pixmap pm, int yOff) {
        Color bone = ColorPalette.BONE_WHITE;
        Color dark = ColorPalette.BONE_DARK;
        // Skull
        for (int x = 6; x <= 10; x++) setPixel(pm, x, 1+yOff, bone);
        for (int x = 5; x <= 11; x++) setPixel(pm, x, 2+yOff, bone);
        for (int x = 5; x <= 11; x++) setPixel(pm, x, 3+yOff, bone);
        // Eye sockets
        setPixel(pm, 6, 3+yOff, dark); setPixel(pm, 7, 3+yOff, dark);
        setPixel(pm, 9, 3+yOff, dark); setPixel(pm, 10, 3+yOff, dark);
        for (int x = 5; x <= 11; x++) setPixel(pm, x, 4+yOff, bone);
        // Jaw
        setPixel(pm, 7, 4+yOff, dark); setPixel(pm, 8, 4+yOff, dark); setPixel(pm, 9, 4+yOff, dark);
        // Spine
        setPixel(pm, 8, 5+yOff, bone); setPixel(pm, 8, 6+yOff, bone);
        // Ribcage
        for (int x = 6; x <= 10; x++) setPixel(pm, x, 5+yOff, bone);
        setPixel(pm, 6, 6+yOff, bone); setPixel(pm, 10, 6+yOff, bone);
        for (int x = 6; x <= 10; x++) setPixel(pm, x, 7+yOff, bone);
        setPixel(pm, 6, 8+yOff, bone); setPixel(pm, 10, 8+yOff, bone);
        for (int x = 6; x <= 10; x++) setPixel(pm, x, 9+yOff, bone);
        // Pelvis
        for (int x = 7; x <= 9; x++) setPixel(pm, x, 10+yOff, bone);
        // Legs (bones)
        setPixel(pm, 7, 11+yOff, bone); setPixel(pm, 9, 11+yOff, bone);
        setPixel(pm, 7, 12+yOff, bone); setPixel(pm, 9, 12+yOff, bone);
        setPixel(pm, 7, 13+yOff, bone); setPixel(pm, 9, 13+yOff, bone);
        // Sword on right
        Color sword = ColorPalette.PLAYER_ARMOR;
        setPixel(pm, 12, 4+yOff, sword); setPixel(pm, 12, 5+yOff, sword);
        setPixel(pm, 12, 6+yOff, sword); setPixel(pm, 12, 7+yOff, sword);
        setPixel(pm, 12, 8+yOff, sword); setPixel(pm, 12, 9+yOff, sword);
        setPixel(pm, 11, 6+yOff, dark); setPixel(pm, 13, 6+yOff, dark); // crossguard
    }

    // ===== HELPER: Draw Goblin King body (32x32) =====
    private void drawGoblinKingBody(Pixmap pm, int yOff) {
        Color green = ColorPalette.GOBLIN_GREEN;
        Color brown = ColorPalette.GOBLIN_BROWN;
        Color gold = ColorPalette.CROWN_GOLD;
        Color dark = ColorPalette.BONE_DARK;
        // Crown
        for (int x = 11; x <= 20; x++) setPixel(pm, x, 2+yOff, gold);
        setPixel(pm, 12, 1+yOff, gold); setPixel(pm, 15, 1+yOff, gold); setPixel(pm, 19, 1+yOff, gold);
        for (int x = 11; x <= 20; x++) setPixel(pm, x, 3+yOff, gold);
        // Head (bigger)
        for (int y = 4; y <= 8; y++) for (int x = 10; x <= 21; x++) setPixel(pm, x, y+yOff, green);
        // Ears
        setPixel(pm, 9, 4+yOff, green); setPixel(pm, 8, 3+yOff, green);
        setPixel(pm, 22, 4+yOff, green); setPixel(pm, 23, 3+yOff, green);
        // Eyes
        setPixel(pm, 12, 6+yOff, dark); setPixel(pm, 13, 6+yOff, dark);
        setPixel(pm, 18, 6+yOff, dark); setPixel(pm, 19, 6+yOff, dark);
        // Mouth/fangs
        setPixel(pm, 14, 8+yOff, dark); setPixel(pm, 15, 8+yOff, ColorPalette.BONE_WHITE);
        setPixel(pm, 16, 8+yOff, dark); setPixel(pm, 17, 8+yOff, ColorPalette.BONE_WHITE);
        // Large body
        for (int y = 9; y <= 20; y++) for (int x = 8; x <= 23; x++) setPixel(pm, x, y+yOff, brown);
        // Arms
        for (int y = 10; y <= 16; y++) { setPixel(pm, 7, y+yOff, green); setPixel(pm, 6, y+yOff, green); }
        for (int y = 10; y <= 16; y++) { setPixel(pm, 24, y+yOff, green); setPixel(pm, 25, y+yOff, green); }
        // Legs
        for (int y = 21; y <= 26; y++) { for (int x = 10; x <= 14; x++) setPixel(pm, x, y+yOff, green); }
        for (int y = 21; y <= 26; y++) { for (int x = 17; x <= 21; x++) setPixel(pm, x, y+yOff, green); }
        // Feet
        for (int x = 9; x <= 15; x++) setPixel(pm, x, 27+yOff, dark);
        for (int x = 16; x <= 22; x++) setPixel(pm, x, 27+yOff, dark);
    }

    // ===== HELPER: Draw Goblin Chieftain body (24x24) =====
    private void drawGoblinChieftainBody(Pixmap pm, int yOff) {
        Color green = ColorPalette.GOBLIN_GREEN;
        Color brown = ColorPalette.GOBLIN_BROWN;
        Color red = ColorPalette.ENRAGE_RED;
        Color dark = ColorPalette.BONE_DARK;
        // Head with war paint
        for (int x = 8; x <= 15; x++) for (int y = 2; y <= 6; y++) setPixel(pm, x, y+yOff, green);
        // Pointy ears
        setPixel(pm, 7, 2+yOff, green); setPixel(pm, 6, 1+yOff, green);
        setPixel(pm, 16, 2+yOff, green); setPixel(pm, 17, 1+yOff, green);
        // Eyes
        setPixel(pm, 9, 4+yOff, dark); setPixel(pm, 14, 4+yOff, dark);
        // War paint stripes
        setPixel(pm, 10, 3+yOff, red); setPixel(pm, 10, 5+yOff, red);
        setPixel(pm, 13, 3+yOff, red); setPixel(pm, 13, 5+yOff, red);
        // Body
        for (int x = 7; x <= 16; x++) for (int y = 7; y <= 14; y++) setPixel(pm, x, y+yOff, brown);
        // Arms
        for (int y = 8; y <= 12; y++) { setPixel(pm, 5, y+yOff, green); setPixel(pm, 6, y+yOff, green); }
        for (int y = 8; y <= 12; y++) { setPixel(pm, 17, y+yOff, green); setPixel(pm, 18, y+yOff, green); }
        // Banner/weapon on right
        setPixel(pm, 19, 5+yOff, red); setPixel(pm, 19, 6+yOff, red); setPixel(pm, 19, 7+yOff, red);
        setPixel(pm, 19, 8+yOff, brown); setPixel(pm, 19, 9+yOff, brown);
        setPixel(pm, 19, 10+yOff, brown); setPixel(pm, 19, 11+yOff, brown);
        setPixel(pm, 20, 5+yOff, red); setPixel(pm, 21, 5+yOff, red);
        setPixel(pm, 20, 6+yOff, red); setPixel(pm, 21, 6+yOff, red);
        // Legs
        for (int x = 8; x <= 10; x++) for (int y = 15; y <= 19; y++) setPixel(pm, x, y+yOff, green);
        for (int x = 13; x <= 15; x++) for (int y = 15; y <= 19; y++) setPixel(pm, x, y+yOff, green);
        // Feet
        for (int x = 7; x <= 11; x++) setPixel(pm, x, 20+yOff, dark);
        for (int x = 12; x <= 16; x++) setPixel(pm, x, 20+yOff, dark);
    }

    // ===== HELPER: Draw Bone Colossus body (32x32) =====
    private void drawBoneColossusBody(Pixmap pm, int yOff) {
        Color bone = ColorPalette.BONE_WHITE;
        Color dark = ColorPalette.BONE_DARK;
        // Large skull
        for (int x = 10; x <= 22; x++) for (int y = 1; y <= 7; y++) setPixel(pm, x, y+yOff, bone);
        // Eye sockets (large)
        for (int x = 11; x <= 13; x++) for (int y = 3; y <= 5; y++) setPixel(pm, x, y+yOff, dark);
        for (int x = 19; x <= 21; x++) for (int y = 3; y <= 5; y++) setPixel(pm, x, y+yOff, dark);
        // Jaw
        for (int x = 12; x <= 20; x++) setPixel(pm, x, 7+yOff, dark);
        setPixel(pm, 13, 8+yOff, bone); setPixel(pm, 15, 8+yOff, bone);
        setPixel(pm, 17, 8+yOff, bone); setPixel(pm, 19, 8+yOff, bone);
        // Massive ribcage
        for (int y = 9; y <= 20; y++) {
            setPixel(pm, 8, y+yOff, bone); setPixel(pm, 9, y+yOff, bone);
            setPixel(pm, 22, y+yOff, bone); setPixel(pm, 23, y+yOff, bone);
            if (y % 2 == 0) { for (int x = 10; x <= 21; x++) setPixel(pm, x, y+yOff, bone); }
        }
        // Spine
        for (int y = 9; y <= 22; y++) { setPixel(pm, 15, y+yOff, bone); setPixel(pm, 16, y+yOff, bone); }
        // Pelvis
        for (int x = 10; x <= 21; x++) setPixel(pm, x, 21+yOff, bone);
        for (int x = 11; x <= 20; x++) setPixel(pm, x, 22+yOff, bone);
        // Legs (thick bones)
        for (int y = 23; y <= 29; y++) { setPixel(pm, 11, y+yOff, bone); setPixel(pm, 12, y+yOff, bone); setPixel(pm, 13, y+yOff, bone); }
        for (int y = 23; y <= 29; y++) { setPixel(pm, 18, y+yOff, bone); setPixel(pm, 19, y+yOff, bone); setPixel(pm, 20, y+yOff, bone); }
        // Arms (reaching out)
        for (int x = 3; x <= 8; x++) { setPixel(pm, x, 11+yOff, bone); setPixel(pm, x, 12+yOff, bone); }
        for (int x = 23; x <= 28; x++) { setPixel(pm, x, 11+yOff, bone); setPixel(pm, x, 12+yOff, bone); }
        // Hands (claws)
        setPixel(pm, 2, 12+yOff, dark); setPixel(pm, 2, 13+yOff, dark); setPixel(pm, 3, 13+yOff, dark);
        setPixel(pm, 29, 12+yOff, dark); setPixel(pm, 29, 13+yOff, dark); setPixel(pm, 28, 13+yOff, dark);
    }

    // ===== HELPER: Draw Lich body (24x32) =====
    private void drawLichBody(Pixmap pm, int yOff) {
        Color purple = ColorPalette.LICH_PURPLE;
        Color eyes = ColorPalette.LICH_EYES;
        Color dark = ColorPalette.DARK_BLACK;
        Color bone = ColorPalette.BONE_WHITE;
        // Hood
        for (int x = 8; x <= 16; x++) setPixel(pm, x, 2+yOff, purple);
        for (int x = 7; x <= 17; x++) setPixel(pm, x, 3+yOff, purple);
        for (int x = 6; x <= 18; x++) for (int y = 4; y <= 7; y++) setPixel(pm, x, y+yOff, purple);
        // Dark face inside hood
        for (int x = 8; x <= 16; x++) for (int y = 5; y <= 7; y++) setPixel(pm, x, y+yOff, dark);
        // Glowing eyes
        setPixel(pm, 9, 6+yOff, eyes); setPixel(pm, 10, 6+yOff, eyes);
        setPixel(pm, 14, 6+yOff, eyes); setPixel(pm, 15, 6+yOff, eyes);
        // Robes (flowing)
        for (int y = 8; y <= 26; y++) {
            int spread = (y - 8) / 3;
            for (int x = 7-spread; x <= 17+spread; x++) {
                if (x >= 0 && x < 24) setPixel(pm, x, y+yOff, purple);
            }
        }
        // Dark trim on robe
        for (int y = 22; y <= 26; y++) {
            int spread = (y - 8) / 3;
            int left = 7-spread;
            int right = 17+spread;
            if (left >= 0) setPixel(pm, left, y+yOff, dark);
            if (right < 24) setPixel(pm, right, y+yOff, dark);
        }
        // Staff (left side)
        for (int y = 3; y <= 26; y++) setPixel(pm, 3, y+yOff, bone);
        // Staff orb
        setPixel(pm, 2, 2+yOff, eyes); setPixel(pm, 3, 1+yOff, eyes);
        setPixel(pm, 4, 2+yOff, eyes); setPixel(pm, 3, 2+yOff, eyes);
        // Skeletal hands
        setPixel(pm, 6, 12+yOff, bone); setPixel(pm, 5, 13+yOff, bone);
        setPixel(pm, 18, 12+yOff, bone); setPixel(pm, 19, 13+yOff, bone);
    }

    // ===== Generic frame generators for reuse =====
    private TextureRegion[] generateSimpleEntity(int w, int h, Color body, Color accent, boolean isBoss, boolean hasShield) {
        TextureRegion[] idle = new TextureRegion[2];
        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(w, h);
            drawGoblinBody(pm, i, body);
            idle[i] = createRegionFromPixmap(pm);
        }
        return idle;
    }

    private TextureRegion[] generateAttackFrames(int w, int h, Color body, Color weapon) {
        TextureRegion[] frames = new TextureRegion[3];
        for (int f = 0; f < 3; f++) {
            Pixmap pm = createPixmap(w, h);
            // Body silhouette
            int cx = w / 2; int cy = h / 2;
            int bw = w / 3; int bh = h / 2;
            pm.setColor(body);
            pm.fillRectangle(cx - bw/2, cy - bh/2, bw, bh);
            // Head
            pm.fillCircle(cx, cy - bh/2 - bw/4, bw/3);
            // Attack motion - weapon extends further each frame
            pm.setColor(weapon);
            int weaponLen = w/4 + f * (w/6);
            pm.fillRectangle(cx + bw/2, cy - 1, weaponLen, 2);
            frames[f] = createRegionFromPixmap(pm);
        }
        return frames;
    }

    private TextureRegion[] generateHurtFrames(int w, int h, Color body) {
        TextureRegion[] frames = new TextureRegion[2];
        // Frame 0: white flash
        Pixmap pm = createPixmap(w, h);
        int cx = w / 2; int cy = h / 2;
        int bw = w / 3; int bh = h / 2;
        pm.setColor(ColorPalette.HOLY_WHITE);
        pm.fillRectangle(cx - bw/2, cy - bh/2, bw, bh);
        pm.fillCircle(cx, cy - bh/2 - bw/4, bw/3);
        frames[0] = createRegionFromPixmap(pm);
        // Frame 1: recoil (shifted left)
        pm = createPixmap(w, h);
        pm.setColor(body);
        pm.fillRectangle(cx - bw/2 - 2, cy - bh/2, bw, bh);
        pm.fillCircle(cx - 2, cy - bh/2 - bw/4, bw/3);
        frames[1] = createRegionFromPixmap(pm);
        return frames;
    }

    private TextureRegion[] generateDeathFrames(int w, int h, Color body) {
        TextureRegion[] frames = new TextureRegion[3];
        int cx = w / 2; int cy = h / 2;
        int bw = w / 3; int bh = h / 2;
        // Frame 0: normal stance
        Pixmap pm = createPixmap(w, h);
        pm.setColor(body);
        pm.fillRectangle(cx - bw/2, cy - bh/2, bw, bh);
        pm.fillCircle(cx, cy - bh/2 - bw/4, bw/3);
        frames[0] = createRegionFromPixmap(pm);
        // Frame 1: falling/leaning
        pm = createPixmap(w, h);
        pm.setColor(body);
        pm.fillRectangle(cx - bw/2 + 1, cy - bh/2 + 2, bw, bh - 2);
        pm.fillCircle(cx + 2, cy - bh/2 + 1, bw/3);
        frames[1] = createRegionFromPixmap(pm);
        // Frame 2: collapsed
        pm = createPixmap(w, h);
        pm.setColor(body);
        pm.fillRectangle(cx - bh/2, h - 4, bh, 3);
        frames[2] = createRegionFromPixmap(pm);
        return frames;
    }

    private TextureRegion[] generateCastFrames(int w, int h, Color body) {
        TextureRegion[] frames = new TextureRegion[2];
        int cx = w / 2; int cy = h / 2;
        int bw = w / 3; int bh = h / 2;
        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(w, h);
            pm.setColor(body);
            pm.fillRectangle(cx - bw/2, cy - bh/2, bw, bh);
            pm.fillCircle(cx, cy - bh/2 - bw/4, bw/3);
            // Casting glow above head
            Color glow = (i == 0) ? ColorPalette.HOLY_GOLD : ColorPalette.LICH_EYES;
            pm.setColor(glow);
            pm.fillCircle(cx, cy - bh/2 - bw/2 - 2, 2 + i);
            frames[i] = createRegionFromPixmap(pm);
        }
        return frames;
    }

    // ===== UI SPRITE GENERATION =====

    private void generateHpBarFrame() {
        Pixmap pm = createPixmap(64, 8);
        Color border = ColorPalette.UI_BORDER;
        Color bg = ColorPalette.HP_BG;
        // Textured border
        pm.setColor(border);
        pm.drawRectangle(0, 0, 64, 8);
        // Corner accents
        setPixel(pm, 1, 1, border); setPixel(pm, 62, 1, border);
        setPixel(pm, 1, 6, border); setPixel(pm, 62, 6, border);
        // Dark fill
        pm.setColor(bg);
        pm.fillRectangle(2, 2, 60, 4);
        uiSprites.put("hp_bar_frame", createRegionFromPixmap(pm));
    }

    private void generateMpBarFrame() {
        Pixmap pm = createPixmap(64, 8);
        Color border = ColorPalette.UI_BORDER;
        Color bg = ColorPalette.MP_BG;
        pm.setColor(border);
        pm.drawRectangle(0, 0, 64, 8);
        setPixel(pm, 1, 1, border); setPixel(pm, 62, 1, border);
        setPixel(pm, 1, 6, border); setPixel(pm, 62, 6, border);
        pm.setColor(bg);
        pm.fillRectangle(2, 2, 60, 4);
        uiSprites.put("mp_bar_frame", createRegionFromPixmap(pm));
    }

    private void generateStatusIcons() {
        // Poison icon (16x16) - green skull/droplet
        Pixmap pm = createPixmap(16, 16);
        Color green = ColorPalette.POISON_GREEN;
        Color dark = ColorPalette.POISON_DARK;
        // Droplet shape
        for (int x = 6; x <= 9; x++) setPixel(pm, x, 2, green);
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 3, green);
        for (int x = 4; x <= 11; x++) for (int y = 4; y <= 9; y++) setPixel(pm, x, y, green);
        for (int x = 5; x <= 10; x++) for (int y = 10; y <= 11; y++) setPixel(pm, x, y, green);
        for (int x = 6; x <= 9; x++) setPixel(pm, x, 12, green);
        // Skull face in droplet
        setPixel(pm, 6, 6, dark); setPixel(pm, 7, 6, dark);
        setPixel(pm, 9, 6, dark); setPixel(pm, 10, 6, dark);
        setPixel(pm, 7, 9, dark); setPixel(pm, 8, 9, dark);
        uiSprites.put("status_poison", createRegionFromPixmap(pm));

        // Shield icon (16x16) - blue shield
        pm = createPixmap(16, 16);
        Color blue = ColorPalette.SHIELD_BLUE;
        Color border = ColorPalette.UI_BORDER;
        for (int x = 4; x <= 11; x++) setPixel(pm, x, 2, border);
        for (int y = 3; y <= 10; y++) { setPixel(pm, 3, y, border); setPixel(pm, 12, y, border); }
        for (int x = 4; x <= 11; x++) for (int y = 3; y <= 10; y++) setPixel(pm, x, y, blue);
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 11, blue);
        for (int x = 6; x <= 9; x++) setPixel(pm, x, 12, blue);
        setPixel(pm, 7, 13, blue); setPixel(pm, 8, 13, blue);
        uiSprites.put("status_shield", createRegionFromPixmap(pm));

        // Stun icon (16x16) - yellow stars
        pm = createPixmap(16, 16);
        Color yellow = ColorPalette.STUN_YELLOW;
        // Star 1
        setPixel(pm, 4, 3, yellow); setPixel(pm, 4, 4, yellow); setPixel(pm, 3, 4, yellow); setPixel(pm, 5, 4, yellow); setPixel(pm, 4, 5, yellow);
        // Star 2
        setPixel(pm, 10, 5, yellow); setPixel(pm, 10, 6, yellow); setPixel(pm, 9, 6, yellow); setPixel(pm, 11, 6, yellow); setPixel(pm, 10, 7, yellow);
        // Star 3
        setPixel(pm, 6, 9, yellow); setPixel(pm, 6, 10, yellow); setPixel(pm, 5, 10, yellow); setPixel(pm, 7, 10, yellow); setPixel(pm, 6, 11, yellow);
        uiSprites.put("status_stun", createRegionFromPixmap(pm));

        // Regen icon (16x16) - green heart
        pm = createPixmap(16, 16);
        Color regen = ColorPalette.REGEN_GREEN;
        setPixel(pm, 4, 4, regen); setPixel(pm, 5, 3, regen); setPixel(pm, 6, 3, regen); setPixel(pm, 7, 4, regen);
        setPixel(pm, 8, 4, regen); setPixel(pm, 9, 3, regen); setPixel(pm, 10, 3, regen); setPixel(pm, 11, 4, regen);
        for (int x = 4; x <= 11; x++) setPixel(pm, x, 5, regen);
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 6, regen);
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 7, regen);
        for (int x = 6; x <= 9; x++) setPixel(pm, x, 8, regen);
        for (int x = 6; x <= 9; x++) setPixel(pm, x, 9, regen);
        for (int x = 7; x <= 8; x++) setPixel(pm, x, 10, regen);
        uiSprites.put("status_regen", createRegionFromPixmap(pm));

        // Enrage icon (16x16) - red flame
        pm = createPixmap(16, 16);
        Color red = ColorPalette.ENRAGE_RED;
        Color orange = ColorPalette.FIRE_ORANGE;
        setPixel(pm, 8, 2, orange); setPixel(pm, 7, 3, red); setPixel(pm, 8, 3, orange); setPixel(pm, 9, 3, red);
        for (int x = 6; x <= 10; x++) setPixel(pm, x, 4, red);
        for (int x = 5; x <= 11; x++) setPixel(pm, x, 5, red);
        for (int x = 5; x <= 11; x++) setPixel(pm, x, 6, red);
        for (int x = 5; x <= 11; x++) setPixel(pm, x, 7, orange);
        for (int x = 6; x <= 10; x++) setPixel(pm, x, 8, orange);
        for (int x = 6; x <= 10; x++) setPixel(pm, x, 9, red);
        for (int x = 7; x <= 9; x++) setPixel(pm, x, 10, red);
        for (int x = 7; x <= 9; x++) setPixel(pm, x, 11, orange);
        setPixel(pm, 8, 12, orange);
        uiSprites.put("status_enrage", createRegionFromPixmap(pm));

        // Curse icon (16x16) - purple swirl
        pm = createPixmap(16, 16);
        Color curse = ColorPalette.CURSE_PURPLE;
        setPixel(pm, 8, 3, curse); setPixel(pm, 9, 3, curse); setPixel(pm, 10, 4, curse);
        setPixel(pm, 11, 5, curse); setPixel(pm, 11, 6, curse); setPixel(pm, 11, 7, curse);
        setPixel(pm, 10, 8, curse); setPixel(pm, 9, 8, curse); setPixel(pm, 8, 8, curse);
        setPixel(pm, 7, 7, curse); setPixel(pm, 7, 6, curse); setPixel(pm, 8, 5, curse);
        setPixel(pm, 9, 5, curse); setPixel(pm, 9, 6, curse);
        setPixel(pm, 5, 4, curse); setPixel(pm, 5, 9, curse); setPixel(pm, 6, 10, curse);
        setPixel(pm, 7, 11, curse); setPixel(pm, 8, 11, curse); setPixel(pm, 9, 10, curse);
        uiSprites.put("status_curse", createRegionFromPixmap(pm));
    }

    private void generateChestSprites() {
        TextureRegion[] chest = new TextureRegion[3];
        Color brown = ColorPalette.GOBLIN_BROWN;
        Color gold = ColorPalette.CROWN_GOLD;
        Color dark = ColorPalette.BONE_DARK;

        // Frame 0: Closed chest
        Pixmap pm = createPixmap(16, 16);
        pm.setColor(brown);
        pm.fillRectangle(2, 6, 12, 8);
        pm.setColor(dark);
        pm.drawRectangle(2, 6, 12, 8);
        pm.setColor(gold);
        pm.fillRectangle(6, 8, 4, 2); // lock
        // Lid
        pm.setColor(brown);
        pm.fillRectangle(2, 4, 12, 3);
        pm.setColor(dark);
        pm.drawRectangle(2, 4, 12, 3);
        chest[0] = createRegionFromPixmap(pm);

        // Frame 1: Opening chest
        pm = createPixmap(16, 16);
        pm.setColor(brown);
        pm.fillRectangle(2, 8, 12, 6);
        pm.setColor(dark);
        pm.drawRectangle(2, 8, 12, 6);
        pm.setColor(gold);
        pm.fillRectangle(6, 10, 4, 2);
        // Lid tilted up
        pm.setColor(brown);
        pm.fillRectangle(2, 4, 12, 2);
        pm.setColor(dark);
        pm.drawRectangle(2, 4, 12, 2);
        // Glow
        pm.setColor(ColorPalette.HOLY_GOLD);
        setPixel(pm, 7, 7, ColorPalette.HOLY_GOLD); setPixel(pm, 8, 7, ColorPalette.HOLY_GOLD);
        chest[1] = createRegionFromPixmap(pm);

        // Frame 2: Open chest
        pm = createPixmap(16, 16);
        pm.setColor(brown);
        pm.fillRectangle(2, 9, 12, 5);
        pm.setColor(dark);
        pm.drawRectangle(2, 9, 12, 5);
        // Lid fully open (back)
        pm.setColor(brown);
        pm.fillRectangle(2, 2, 12, 2);
        pm.setColor(dark);
        pm.drawRectangle(2, 2, 12, 2);
        // Treasure glow
        pm.setColor(ColorPalette.HOLY_GOLD);
        for (int x = 4; x <= 11; x++) setPixel(pm, x, 8, ColorPalette.HOLY_GOLD);
        for (int x = 5; x <= 10; x++) setPixel(pm, x, 7, ColorPalette.FIRE_YELLOW);
        for (int x = 6; x <= 9; x++) setPixel(pm, x, 6, ColorPalette.HOLY_GOLD);
        chest[2] = createRegionFromPixmap(pm);

        uiAnimatedSprites.put("chest", chest);
    }

    private void generateMenuFrame() {
        Pixmap pm = createPixmap(64, 64);
        Color border = ColorPalette.UI_BORDER;
        Color bg = ColorPalette.BACKGROUND;
        // Fill background
        pm.setColor(bg);
        pm.fill();
        // Double border
        pm.setColor(border);
        pm.drawRectangle(0, 0, 64, 64);
        pm.drawRectangle(2, 2, 60, 60);
        // Corner decorations
        setPixel(pm, 1, 1, border); setPixel(pm, 62, 1, border);
        setPixel(pm, 1, 62, border); setPixel(pm, 62, 62, border);
        // Inner corner accents
        for (int i = 4; i <= 6; i++) { setPixel(pm, i, 4, border); setPixel(pm, 4, i, border); }
        for (int i = 57; i <= 59; i++) { setPixel(pm, i, 4, border); setPixel(pm, 59, 64-i+3, border); }
        uiSprites.put("menu_frame", createRegionFromPixmap(pm));
    }

    private void generateWaveBanner() {
        Pixmap pm = createPixmap(128, 24);
        Color border = ColorPalette.UI_BORDER;
        Color bg = ColorPalette.BACKGROUND;
        Color gold = ColorPalette.CROWN_GOLD;
        pm.setColor(bg);
        pm.fill();
        // Banner shape with pointed ends
        pm.setColor(border);
        pm.fillRectangle(8, 4, 112, 16);
        pm.setColor(gold);
        pm.drawRectangle(8, 4, 112, 16);
        pm.drawRectangle(10, 6, 108, 12);
        // Pointed edges
        for (int i = 0; i < 4; i++) {
            setPixel(pm, 4+i, 8+i, border); setPixel(pm, 4+i, 15-i, border);
            setPixel(pm, 123-i, 8+i, border); setPixel(pm, 123-i, 15-i, border);
        }
        uiSprites.put("wave_banner", createRegionFromPixmap(pm));
    }

    private void generateElementIcons() {
        // Fire icon (12x12) - orange flame
        Pixmap pm = createPixmap(12, 12);
        Color orange = ColorPalette.FIRE_ORANGE;
        Color yellow = ColorPalette.FIRE_YELLOW;
        setPixel(pm, 6, 1, yellow); setPixel(pm, 5, 2, orange); setPixel(pm, 6, 2, yellow); setPixel(pm, 7, 2, orange);
        for (int x = 4; x <= 8; x++) setPixel(pm, x, 3, orange);
        for (int x = 3; x <= 9; x++) setPixel(pm, x, 4, orange);
        for (int x = 3; x <= 9; x++) setPixel(pm, x, 5, orange);
        for (int x = 4; x <= 8; x++) setPixel(pm, x, 6, yellow);
        for (int x = 4; x <= 8; x++) setPixel(pm, x, 7, orange);
        for (int x = 5; x <= 7; x++) setPixel(pm, x, 8, orange);
        setPixel(pm, 6, 9, yellow);
        uiSprites.put("element_fire", createRegionFromPixmap(pm));

        // Holy icon (12x12) - golden cross/star
        pm = createPixmap(12, 12);
        Color gold = ColorPalette.HOLY_GOLD;
        Color white = ColorPalette.HOLY_WHITE;
        for (int y = 2; y <= 9; y++) setPixel(pm, 6, y, gold);
        for (int x = 3; x <= 9; x++) setPixel(pm, x, 5, gold);
        setPixel(pm, 5, 4, white); setPixel(pm, 7, 4, white);
        setPixel(pm, 5, 6, white); setPixel(pm, 7, 6, white);
        uiSprites.put("element_holy", createRegionFromPixmap(pm));

        // Dark icon (12x12) - purple orb
        pm = createPixmap(12, 12);
        Color purple = ColorPalette.DARK_PURPLE;
        Color darkBg = ColorPalette.DARK_BLACK;
        for (int x = 4; x <= 8; x++) setPixel(pm, x, 3, purple);
        for (int x = 3; x <= 9; x++) for (int y = 4; y <= 8; y++) setPixel(pm, x, y, purple);
        for (int x = 4; x <= 8; x++) setPixel(pm, x, 9, purple);
        // Dark center
        for (int x = 5; x <= 7; x++) for (int y = 5; y <= 7; y++) setPixel(pm, x, y, darkBg);
        uiSprites.put("element_dark", createRegionFromPixmap(pm));

        // Physical icon (12x12) - gray sword
        pm = createPixmap(12, 12);
        Color gray = ColorPalette.PLAYER_ARMOR;
        Color dark = ColorPalette.BONE_DARK;
        // Blade
        for (int i = 0; i <= 7; i++) setPixel(pm, 2+i, 9-i, gray);
        for (int i = 0; i <= 7; i++) setPixel(pm, 3+i, 9-i, gray);
        // Crossguard
        setPixel(pm, 4, 7, dark); setPixel(pm, 5, 7, dark); setPixel(pm, 7, 5, dark); setPixel(pm, 8, 5, dark);
        // Handle
        setPixel(pm, 2, 10, dark); setPixel(pm, 1, 11, dark);
        uiSprites.put("element_physical", createRegionFromPixmap(pm));

        // Poison icon (12x12) - green drop
        pm = createPixmap(12, 12);
        Color pGreen = ColorPalette.POISON_GREEN;
        Color pDark = ColorPalette.POISON_DARK;
        setPixel(pm, 6, 2, pGreen); setPixel(pm, 5, 3, pGreen); setPixel(pm, 6, 3, pGreen); setPixel(pm, 7, 3, pGreen);
        for (int x = 4; x <= 8; x++) setPixel(pm, x, 4, pGreen);
        for (int x = 3; x <= 9; x++) for (int y = 5; y <= 8; y++) setPixel(pm, x, y, pGreen);
        for (int x = 4; x <= 8; x++) setPixel(pm, x, 9, pGreen);
        for (int x = 5; x <= 7; x++) setPixel(pm, x, 10, pGreen);
        // Dark highlight
        setPixel(pm, 5, 6, pDark); setPixel(pm, 5, 7, pDark);
        uiSprites.put("element_poison", createRegionFromPixmap(pm));
    }

    private void generateBackgroundTile() {
        Pixmap pm = createPixmap(16, 16);
        Color floor = ColorPalette.DUNGEON_FLOOR;
        Color wall = ColorPalette.DUNGEON_WALL;
        // Base floor
        pm.setColor(floor);
        pm.fill();
        // Stone pattern (lighter cracks/lines)
        pm.setColor(wall);
        // Horizontal mortar lines
        pm.drawLine(0, 4, 15, 4);
        pm.drawLine(0, 11, 15, 11);
        // Vertical mortar (offset pattern like bricks)
        pm.drawLine(5, 0, 5, 4);
        pm.drawLine(11, 0, 11, 4);
        pm.drawLine(2, 4, 2, 11);
        pm.drawLine(8, 4, 8, 11);
        pm.drawLine(14, 4, 14, 11);
        pm.drawLine(5, 11, 5, 15);
        pm.drawLine(11, 11, 11, 15);
        uiSprites.put("background_tile", createRegionFromPixmap(pm));
    }

    private void generateParticleTextures() {
        // Fire particle (4x4)
        Pixmap pm = createPixmap(4, 4);
        pm.setColor(ColorPalette.FIRE_ORANGE);
        pm.fillRectangle(1, 0, 2, 4);
        pm.fillRectangle(0, 1, 4, 2);
        uiSprites.put("particle_fire", createRegionFromPixmap(pm));

        // Holy particle (4x4)
        pm = createPixmap(4, 4);
        pm.setColor(ColorPalette.HOLY_GOLD);
        setPixel(pm, 1, 0, ColorPalette.HOLY_GOLD); setPixel(pm, 2, 0, ColorPalette.HOLY_GOLD);
        setPixel(pm, 0, 1, ColorPalette.HOLY_WHITE); setPixel(pm, 3, 1, ColorPalette.HOLY_WHITE);
        setPixel(pm, 1, 2, ColorPalette.HOLY_GOLD); setPixel(pm, 2, 2, ColorPalette.HOLY_GOLD);
        setPixel(pm, 0, 2, ColorPalette.HOLY_WHITE); setPixel(pm, 3, 2, ColorPalette.HOLY_WHITE);
        setPixel(pm, 1, 3, ColorPalette.HOLY_GOLD); setPixel(pm, 2, 3, ColorPalette.HOLY_GOLD);
        uiSprites.put("particle_holy", createRegionFromPixmap(pm));

        // Dark particle (4x4)
        pm = createPixmap(4, 4);
        pm.setColor(ColorPalette.DARK_PURPLE);
        pm.fillCircle(2, 2, 1);
        setPixel(pm, 0, 0, ColorPalette.DARK_BLACK);
        setPixel(pm, 3, 3, ColorPalette.DARK_BLACK);
        uiSprites.put("particle_dark", createRegionFromPixmap(pm));

        // Poison particle (4x4)
        pm = createPixmap(4, 4);
        pm.setColor(ColorPalette.POISON_GREEN);
        pm.fillCircle(2, 2, 1);
        setPixel(pm, 1, 0, ColorPalette.POISON_DARK);
        uiSprites.put("particle_poison", createRegionFromPixmap(pm));

        // Generic impact particle (4x4)
        pm = createPixmap(4, 4);
        pm.setColor(ColorPalette.HOLY_WHITE);
        setPixel(pm, 1, 0, ColorPalette.HOLY_WHITE); setPixel(pm, 2, 0, ColorPalette.HOLY_WHITE);
        setPixel(pm, 0, 1, ColorPalette.HOLY_WHITE); setPixel(pm, 3, 1, ColorPalette.HOLY_WHITE);
        setPixel(pm, 0, 2, ColorPalette.HOLY_WHITE); setPixel(pm, 3, 2, ColorPalette.HOLY_WHITE);
        setPixel(pm, 1, 3, ColorPalette.HOLY_WHITE); setPixel(pm, 2, 3, ColorPalette.HOLY_WHITE);
        uiSprites.put("particle_impact", createRegionFromPixmap(pm));

        // Physical particle (4x4 white square) - default particle used by effects system
        pm = createPixmap(4, 4);
        pm.setColor(Color.WHITE);
        pm.fillRectangle(0, 0, 4, 4);
        uiSprites.put("particle_physical", createRegionFromPixmap(pm));
    }

    // ===== PLAGUE ELEMENTAL (16x16 green/purple swirling entity) =====
    private void generatePlagueElementalSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2];
        frames[1] = generateAttackFrames(16, 16, ColorPalette.POISON_GREEN, ColorPalette.DARK_PURPLE);
        frames[2] = generateHurtFrames(16, 16, ColorPalette.POISON_GREEN);
        frames[3] = generateDeathFrames(16, 16, ColorPalette.POISON_GREEN);
        frames[4] = generateCastFrames(16, 16, ColorPalette.POISON_GREEN);

        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(16, 16);
            drawPlagueElementalBody(pm, i);
            frames[0][i] = createRegionFromPixmap(pm);
        }
        entitySprites.put("plague_elemental", frames);
    }

    private void drawPlagueElementalBody(Pixmap pm, int frame) {
        Color green = ColorPalette.POISON_GREEN;
        Color purple = ColorPalette.DARK_PURPLE;
        Color dark = ColorPalette.POISON_DARK;

        // Swirling body (shifts slightly between frames)
        int shift = frame;

        // Core body
        for (int x = 5; x <= 10; x++) {
            for (int y = 3 + shift; y <= 10 + shift; y++) {
                if (y < 16) setPixel(pm, x, y, green);
            }
        }

        // Purple swirl accents
        setPixel(pm, 4, 5 + shift, purple); setPixel(pm, 11, 5 + shift, purple);
        setPixel(pm, 3, 7 + shift, purple); setPixel(pm, 12, 7 + shift, purple);
        setPixel(pm, 4, 9 + shift, purple); setPixel(pm, 11, 9 + shift, purple);

        // Eyes (glowing)
        setPixel(pm, 6, 5 + shift, dark); setPixel(pm, 9, 5 + shift, dark);

        // Wispy top tendrils
        setPixel(pm, 7, 2 + shift, green); setPixel(pm, 8, 1 + shift, green);
        setPixel(pm, 6, 2 + shift, purple);

        // Dripping bottom
        setPixel(pm, 6, 11 + shift, dark);
        setPixel(pm, 8, 12 + shift, dark);
        setPixel(pm, 7, 13 + shift, green);

        // Toxic aura particles
        setPixel(pm, 3, 4 + shift, green);
        setPixel(pm, 12, 8 + shift, green);
        setPixel(pm, 2, 9 + shift, purple);
    }

    // ===== THORNMOTHER (24x24 vine-covered boss) =====
    private void generateThornmotherSprite() {
        TextureRegion[][] frames = new TextureRegion[5][];
        frames[0] = new TextureRegion[2];
        frames[1] = generateAttackFrames(24, 24, ColorPalette.POISON_GREEN, ColorPalette.GOBLIN_GREEN);
        frames[2] = generateHurtFrames(24, 24, ColorPalette.POISON_GREEN);
        frames[3] = generateDeathFrames(24, 24, ColorPalette.POISON_GREEN);
        frames[4] = generateCastFrames(24, 24, ColorPalette.POISON_GREEN);

        for (int i = 0; i < 2; i++) {
            Pixmap pm = createPixmap(24, 24);
            drawThornmotherBody(pm, i);
            frames[0][i] = createRegionFromPixmap(pm);
        }
        entitySprites.put("thornmother", frames);
    }

    private void drawThornmotherBody(Pixmap pm, int frame) {
        Color green = ColorPalette.GOBLIN_GREEN;
        Color darkGreen = ColorPalette.POISON_DARK;
        Color vine = ColorPalette.POISON_GREEN;
        Color thorn = ColorPalette.GOBLIN_BROWN;
        Color eye = ColorPalette.LICH_EYES;

        // Main body (large trunk-like mass)
        for (int x = 8; x <= 16; x++) {
            for (int y = 5; y <= 18; y++) {
                setPixel(pm, x, y, green);
            }
        }

        // Darker bark texture
        for (int y = 6; y <= 17; y += 3) {
            setPixel(pm, 9, y, darkGreen);
            setPixel(pm, 14, y + 1, darkGreen);
        }

        // Head/crown with thorns
        for (int x = 9; x <= 15; x++) setPixel(pm, x, 4, green);
        for (int x = 10; x <= 14; x++) setPixel(pm, x, 3, green);
        setPixel(pm, 11, 2, thorn); setPixel(pm, 13, 2, thorn);
        setPixel(pm, 10, 1 + frame, thorn); setPixel(pm, 14, 1 + frame, thorn);

        // Glowing eyes
        setPixel(pm, 10, 6, eye); setPixel(pm, 14, 6, eye);

        // Vine arms (extend outward)
        // Left arm
        for (int x = 3; x <= 8; x++) setPixel(pm, x, 10 + frame, vine);
        for (int x = 2; x <= 4; x++) setPixel(pm, x, 11 + frame, vine);
        setPixel(pm, 2, 9 + frame, thorn); setPixel(pm, 3, 12 + frame, thorn);

        // Right arm
        for (int x = 16; x <= 21; x++) setPixel(pm, x, 10 + frame, vine);
        for (int x = 20; x <= 22; x++) setPixel(pm, x, 11 + frame, vine);
        setPixel(pm, 22, 9 + frame, thorn); setPixel(pm, 21, 12 + frame, thorn);

        // Root base
        for (int x = 6; x <= 18; x++) setPixel(pm, x, 19, darkGreen);
        for (int x = 5; x <= 19; x++) setPixel(pm, x, 20, darkGreen);
        setPixel(pm, 4, 21, vine); setPixel(pm, 7, 21, vine);
        setPixel(pm, 17, 21, vine); setPixel(pm, 20, 21, vine);

        // Thorns on body
        setPixel(pm, 7, 8, thorn); setPixel(pm, 17, 12, thorn);
        setPixel(pm, 7, 15, thorn); setPixel(pm, 17, 7, thorn);
    }

    // ===== WORLD MAP NODE SPRITES (16x16 in different colors) =====
    private void generateWorldMapNodes() {
        // Generate 5 node sprites for different area states
        Color[] nodeColors = {
            ColorPalette.TEXT_WHITE,     // available
            ColorPalette.HEAL_GREEN,     // completed
            ColorPalette.CRIT_YELLOW,    // in-progress
            ColorPalette.DAMAGE_RED,     // locked
            ColorPalette.HOLY_GOLD       // current
        };
        String[] nodeKeys = {
            "map_node_available", "map_node_completed", "map_node_progress",
            "map_node_locked", "map_node_current"
        };

        for (int n = 0; n < nodeColors.length; n++) {
            Pixmap pm = createPixmap(16, 16);
            Color color = nodeColors[n];

            // Draw circular node
            pm.setColor(color);
            for (int x = 5; x <= 10; x++) setPixel(pm, x, 3, color);
            for (int x = 4; x <= 11; x++) setPixel(pm, x, 4, color);
            for (int x = 3; x <= 12; x++) {
                for (int y = 5; y <= 10; y++) {
                    setPixel(pm, x, y, color);
                }
            }
            for (int x = 4; x <= 11; x++) setPixel(pm, x, 11, color);
            for (int x = 5; x <= 10; x++) setPixel(pm, x, 12, color);

            // Inner dark circle
            Color inner = ColorPalette.BACKGROUND;
            for (int x = 6; x <= 9; x++) {
                for (int y = 6; y <= 9; y++) {
                    setPixel(pm, x, y, inner);
                }
            }

            // Center highlight
            setPixel(pm, 7, 7, color);
            setPixel(pm, 8, 7, color);
            setPixel(pm, 7, 8, color);
            setPixel(pm, 8, 8, color);

            uiSprites.put(nodeKeys[n], createRegionFromPixmap(pm));
        }
    }

    @Override
    public void dispose() {
        for (Texture texture : managedTextures) {
            texture.dispose();
        }
        managedTextures.clear();
        entitySprites.clear();
        uiSprites.clear();
        uiAnimatedSprites.clear();
    }
}
