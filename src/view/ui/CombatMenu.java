package view.ui;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import model.Player;
import model.skills.Element;
import model.skills.Skill;
import view.assets.AssetLoader;
import view.sprites.ColorPalette;

/**
 * Action selection UI with pixel-art menu frame border.
 * Shows available actions: "1. Attack" and "2. Skills >" at top level.
 * Skills sub-menu shows all unlocked skills with mana cost, element icons,
 * element color coding, blinking cursor, and cooldown overlay.
 * Supports keyboard (up/down/enter, number keys) and mouse click.
 */
public class CombatMenu {
    private static final Color SELECTED_COLOR = Color.YELLOW;
    private static final Color AVAILABLE_COLOR = Color.WHITE;
    private static final Color DISABLED_COLOR = Color.GRAY;
    private static final Color COOLDOWN_OVERLAY = new Color(0.3f, 0.3f, 0.3f, 0.6f);

    private int selectedIndex;
    private boolean inSkillsSubMenu;
    private int skillMenuIndex;
    private Player player;

    // Blinking cursor timer
    private float cursorBlinkTimer;
    private static final float CURSOR_BLINK_RATE = 3.0f;

    public CombatMenu() {
        this.selectedIndex = 0;
        this.inSkillsSubMenu = false;
        this.skillMenuIndex = 0;
        this.cursorBlinkTimer = 0f;
    }

    /**
     * Sets the player reference for reading skill data.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Renders the combat menu with optional AssetLoader for sprite elements.
     */
    public void render(SpriteBatch batch, BitmapFont font, float x, float y) {
        render(batch, font, null, x, y);
    }

    /**
     * Renders the combat menu with pixel-art frame, element icons, and visual indicators.
     */
    public void render(SpriteBatch batch, BitmapFont font, AssetLoader assets, float x, float y) {
        if (player == null) return;

        // Update blink timer
        cursorBlinkTimer += com.badlogic.gdx.Gdx.graphics.getDeltaTime();

        // Render menu frame border
        renderMenuFrame(batch, assets, x, y);

        if (!inSkillsSubMenu) {
            renderMainMenu(batch, font, assets, x, y);
        } else {
            renderSkillsMenu(batch, font, assets, x, y);
        }
    }

    /**
     * Renders the pixel-art menu frame border around the action list.
     */
    private void renderMenuFrame(SpriteBatch batch, AssetLoader assets, float x, float y) {
        if (assets == null) return;
        TextureRegion menuFrame = assets.getMenuFrame();
        if (menuFrame == null) return;

        // Draw frame around the menu area (slightly larger than content)
        float frameX = x - 4f;
        float frameY = y - 42f;
        float frameW = 148f;
        float frameH = 50f;

        batch.setColor(Color.WHITE);
        batch.draw(menuFrame, frameX, frameY, frameW, frameH);
    }

    private void renderMainMenu(SpriteBatch batch, BitmapFont font, AssetLoader assets, float x, float y) {
        float lineHeight = 10f;

        // Option 1: Attack with blinking cursor
        boolean cursorVisible = isCursorVisible();
        String attackPrefix = (selectedIndex == 0 && cursorVisible) ? "> " : "  ";
        font.setColor(selectedIndex == 0 ? SELECTED_COLOR : AVAILABLE_COLOR);
        font.draw(batch, attackPrefix + "1. Attack", x, y);

        // Option 2: Skills with blinking cursor
        String skillsPrefix = (selectedIndex == 1 && cursorVisible) ? "> " : "  ";
        font.setColor(selectedIndex == 1 ? SELECTED_COLOR : AVAILABLE_COLOR);
        font.draw(batch, skillsPrefix + "2. Skills >", x, y - lineHeight);

        font.setColor(Color.WHITE);
    }

    private void renderSkillsMenu(SpriteBatch batch, BitmapFont font, AssetLoader assets, float x, float y) {
        if (player == null) return;

        List<Skill> skills = player.getSkillTree().getUnlockedSkills();
        float lineHeight = 10f;

        // Header
        font.setColor(Color.CYAN);
        font.draw(batch, "Skills (ESC: back)", x, y);

        boolean cursorVisible = isCursorVisible();

        for (int i = 0; i < skills.size(); i++) {
            Skill skill = skills.get(i);
            float yPos = y - (i + 1) * lineHeight;

            boolean onCooldown = !skill.isReady();
            boolean insufficientMana = skill.getManaCost() > player.getMana();

            // Determine text color based on state with element color coding
            Color color;
            if (onCooldown || insufficientMana) {
                color = DISABLED_COLOR;
            } else if (i == skillMenuIndex) {
                color = SELECTED_COLOR;
            } else {
                // Element color coding for available skills
                color = getElementColor(skill.getElement());
            }

            font.setColor(color);

            // Blinking cursor for selected item
            String prefix = (i == skillMenuIndex && cursorVisible) ? "> " : "  ";

            // Build display string
            StringBuilder display = new StringBuilder();
            display.append(prefix).append(i + 1).append(". ");
            display.append(skill.getName());

            // Show mana cost visually
            if (skill.getManaCost() > 0) {
                display.append(" ").append(skill.getManaCost()).append("MP");
            }

            // Cooldown overlay text
            if (onCooldown) {
                display.append(" [").append(skill.getCurrentCooldown()).append("T]");
            }

            font.draw(batch, display.toString(), x, yPos);

            // Render element icon next to skill name if assets available
            if (assets != null && skill.getElement() != Element.PHYSICAL) {
                TextureRegion elementIcon = assets.getElementIcon(skill.getElement());
                if (elementIcon != null) {
                    float iconSize = 7f;
                    float iconX = x + 12f;
                    float iconY = yPos - iconSize + 1f;
                    batch.setColor(Color.WHITE);
                    batch.draw(elementIcon, iconX, iconY, iconSize, iconSize);
                }
            }
        }

        font.setColor(Color.WHITE);
    }

    /**
     * Returns whether the blinking cursor is currently visible.
     */
    private boolean isCursorVisible() {
        return MathUtils.sin(cursorBlinkTimer * CURSOR_BLINK_RATE * MathUtils.PI2) > 0f;
    }

    /**
     * Returns the element color for UI display.
     */
    public static Color getElementColor(Element element) {
        switch (element) {
            case FIRE: return ColorPalette.FIRE_ORANGE;
            case HOLY: return ColorPalette.HOLY_GOLD;
            case DARK: return ColorPalette.DARK_PURPLE;
            case POISON: return ColorPalette.POISON_GREEN;
            default: return ColorPalette.TEXT_WHITE;
        }
    }

    // --- Input handling ---

    public void moveUp() {
        if (inSkillsSubMenu) {
            skillMenuIndex = Math.max(0, skillMenuIndex - 1);
        } else {
            selectedIndex = Math.max(0, selectedIndex - 1);
        }
    }

    public void moveDown() {
        if (inSkillsSubMenu) {
            if (player != null) {
                int maxIndex = player.getSkillTree().getUnlockedSkills().size() - 1;
                skillMenuIndex = Math.min(maxIndex, skillMenuIndex + 1);
            }
        } else {
            selectedIndex = Math.min(1, selectedIndex + 1);
        }
    }

    /**
     * Confirms the current selection. Returns the selected action index or -1 if navigating.
     * Returns: skill index (0+) for a skill action, -1 if navigating to sub-menu.
     */
    public int confirm() {
        if (!inSkillsSubMenu) {
            if (selectedIndex == 0) {
                // Basic Attack selected - skill index 0
                return 0;
            } else {
                // Open skills sub-menu
                inSkillsSubMenu = true;
                skillMenuIndex = 0;
                return -1;
            }
        } else {
            // Skill selected from sub-menu
            if (player != null) {
                List<Skill> skills = player.getSkillTree().getUnlockedSkills();
                if (skillMenuIndex >= 0 && skillMenuIndex < skills.size()) {
                    Skill skill = skills.get(skillMenuIndex);
                    if (skill.isReady() && skill.getManaCost() <= player.getMana()) {
                        return skillMenuIndex;
                    }
                }
            }
            return -1;
        }
    }

    /**
     * Selects a specific option by number key (1-based).
     * Returns the action skill index or -1 if invalid.
     */
    public int selectByNumber(int number) {
        if (!inSkillsSubMenu) {
            if (number == 1) {
                return 0; // Basic Attack
            } else if (number == 2) {
                inSkillsSubMenu = true;
                skillMenuIndex = 0;
                return -1;
            }
        } else {
            int index = number - 1;
            if (player != null) {
                List<Skill> skills = player.getSkillTree().getUnlockedSkills();
                if (index >= 0 && index < skills.size()) {
                    Skill skill = skills.get(index);
                    if (skill.isReady() && skill.getManaCost() <= player.getMana()) {
                        return index;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Goes back to main menu from skills sub-menu.
     */
    public void back() {
        if (inSkillsSubMenu) {
            inSkillsSubMenu = false;
        }
    }

    /**
     * Resets menu state.
     */
    public void reset() {
        selectedIndex = 0;
        inSkillsSubMenu = false;
        skillMenuIndex = 0;
    }

    public boolean isInSkillsSubMenu() {
        return inSkillsSubMenu;
    }

    public int getSelectedIndex() {
        return inSkillsSubMenu ? skillMenuIndex : selectedIndex;
    }
}
