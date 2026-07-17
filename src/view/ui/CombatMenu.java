package view.ui;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import model.Player;
import model.skills.Element;
import model.skills.Skill;

/**
 * Action selection UI. Shows available actions: "1. Attack" and "2. Skills >" at top level.
 * Skills sub-menu shows all unlocked skills with mana cost. Highlights current selection.
 * Grays out cooldown skills (shows remaining turns). Grays out insufficient mana.
 * Supports keyboard (up/down/enter, number keys) and mouse click. Element color next to each skill.
 */
public class CombatMenu {
    private static final Color SELECTED_COLOR = Color.YELLOW;
    private static final Color AVAILABLE_COLOR = Color.WHITE;
    private static final Color DISABLED_COLOR = Color.GRAY;
    private static final Color HIGHLIGHT_COLOR = Color.GOLD;

    private int selectedIndex;
    private boolean inSkillsSubMenu;
    private int skillMenuIndex;
    private Player player;

    public CombatMenu() {
        this.selectedIndex = 0;
        this.inSkillsSubMenu = false;
        this.skillMenuIndex = 0;
    }

    /**
     * Sets the player reference for reading skill data.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Renders the combat menu.
     */
    public void render(SpriteBatch batch, BitmapFont font, float x, float y) {
        if (player == null) return;

        if (!inSkillsSubMenu) {
            renderMainMenu(batch, font, x, y);
        } else {
            renderSkillsMenu(batch, font, x, y);
        }
    }

    private void renderMainMenu(SpriteBatch batch, BitmapFont font, float x, float y) {
        float lineHeight = 10f;

        // Option 1: Attack
        font.setColor(selectedIndex == 0 ? SELECTED_COLOR : AVAILABLE_COLOR);
        String attackPrefix = selectedIndex == 0 ? "> " : "  ";
        font.draw(batch, attackPrefix + "1. Attack", x, y);

        // Option 2: Skills
        font.setColor(selectedIndex == 1 ? SELECTED_COLOR : AVAILABLE_COLOR);
        String skillsPrefix = selectedIndex == 1 ? "> " : "  ";
        font.draw(batch, skillsPrefix + "2. Skills >", x, y - lineHeight);

        font.setColor(Color.WHITE);
    }

    private void renderSkillsMenu(SpriteBatch batch, BitmapFont font, float x, float y) {
        if (player == null) return;

        List<Skill> skills = player.getSkillTree().getUnlockedSkills();
        float lineHeight = 10f;

        // Header
        font.setColor(Color.CYAN);
        font.draw(batch, "Skills (ESC: back)", x, y);

        for (int i = 0; i < skills.size(); i++) {
            Skill skill = skills.get(i);
            float yPos = y - (i + 1) * lineHeight;

            // Determine color based on state
            Color color;
            if (!skill.isReady()) {
                color = DISABLED_COLOR;
            } else if (skill.getManaCost() > player.getMana()) {
                color = DISABLED_COLOR;
            } else if (i == skillMenuIndex) {
                color = SELECTED_COLOR;
            } else {
                color = AVAILABLE_COLOR;
            }

            font.setColor(color);
            String prefix = (i == skillMenuIndex) ? "> " : "  ";
            String suffix = "";
            if (!skill.isReady()) {
                suffix = " [CD:" + skill.getCurrentCooldown() + "]";
            } else if (skill.getManaCost() > 0) {
                suffix = " (" + skill.getManaCost() + " MP)";
            }

            // Element indicator
            String elementTag = getElementTag(skill.getElement());
            font.draw(batch, prefix + (i + 1) + ". " + elementTag + skill.getName() + suffix, x, yPos);
        }

        font.setColor(Color.WHITE);
    }

    private String getElementTag(Element element) {
        switch (element) {
            case FIRE: return "[F]";
            case HOLY: return "[H]";
            case DARK: return "[D]";
            case POISON: return "[P]";
            default: return "";
        }
    }

    /**
     * Returns the element color for UI display.
     */
    public static Color getElementColor(Element element) {
        switch (element) {
            case FIRE: return Color.ORANGE;
            case HOLY: return Color.GOLD;
            case DARK: return Color.PURPLE;
            case POISON: return Color.GREEN;
            default: return Color.WHITE;
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
