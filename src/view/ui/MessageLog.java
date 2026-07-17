package view.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import model.events.GameEvent;
import model.events.GameEventListener;
import model.events.GameEventType;

/**
 * Implements GameEventListener. Subscribes to CombatEngine event bus via engine.addListener(messageLog).
 * Converts GameEvents to display strings based on type. Typewriter effect (delta-time).
 * Scrollable history of last ~8 messages. Color-coded: damage red, heals green,
 * system white, enemy orange, crits yellow. Messages queue sequentially.
 */
public class MessageLog implements GameEventListener {
    private static final int MAX_MESSAGES = 8;
    private static final float TYPEWRITER_SPEED = 60f; // characters per second

    private final List<LogEntry> displayedMessages;
    private final Queue<LogEntry> pendingMessages;
    private LogEntry currentTyping;
    private int typedCharCount;
    private float typewriterTimer;

    public MessageLog() {
        this.displayedMessages = new ArrayList<>();
        this.pendingMessages = new LinkedList<>();
        this.currentTyping = null;
        this.typedCharCount = 0;
        this.typewriterTimer = 0f;
    }

    @Override
    public void onEvent(GameEvent event) {
        String message = convertEventToString(event);
        if (message == null || message.isEmpty()) {
            return;
        }

        Color color = getColorForEvent(event.getType());
        LogEntry entry = new LogEntry(message, color);
        pendingMessages.add(entry);

        // Start typing if nothing is currently being typed
        if (currentTyping == null) {
            advanceTyping();
        }
    }

    /**
     * Updates the typewriter effect. Call each frame with delta time.
     */
    public void update(float delta) {
        if (currentTyping == null) {
            return;
        }

        typewriterTimer += delta * TYPEWRITER_SPEED;
        int targetChars = (int) typewriterTimer;

        if (targetChars >= currentTyping.message.length()) {
            // Finished typing this message
            typedCharCount = currentTyping.message.length();
            displayedMessages.add(currentTyping);

            // Keep only the last MAX_MESSAGES
            while (displayedMessages.size() > MAX_MESSAGES) {
                displayedMessages.remove(0);
            }

            advanceTyping();
        } else {
            typedCharCount = targetChars;
        }
    }

    /**
     * Returns true if a message is currently being typed out.
     */
    public boolean isTyping() {
        return currentTyping != null;
    }

    /**
     * Renders the message log at the given position.
     */
    public void render(SpriteBatch batch, BitmapFont font, float x, float y, float lineHeight) {
        // Render displayed messages
        float currentY = y;
        for (int i = 0; i < displayedMessages.size(); i++) {
            LogEntry entry = displayedMessages.get(i);
            font.setColor(entry.color);
            font.draw(batch, entry.message, x, currentY);
            currentY -= lineHeight;
        }

        // Render current typing message
        if (currentTyping != null && typedCharCount > 0) {
            String partial = currentTyping.message.substring(0, typedCharCount);
            font.setColor(currentTyping.color);
            font.draw(batch, partial, x, currentY);
        }

        // Reset color
        font.setColor(Color.WHITE);
    }

    private void advanceTyping() {
        if (pendingMessages.isEmpty()) {
            currentTyping = null;
            typedCharCount = 0;
            typewriterTimer = 0f;
        } else {
            currentTyping = pendingMessages.poll();
            typedCharCount = 0;
            typewriterTimer = 0f;
        }
    }

    private String convertEventToString(GameEvent event) {
        GameEventType type = event.getType();
        switch (type) {
            case DAMAGE_DEALT:
                return event.getString("targetName") + " takes " +
                       event.getInt("finalDamage") + " damage! (" +
                       event.getInt("currentHp") + "/" + event.getInt("maxHp") + " HP)";

            case CRITICAL_HIT:
                return "CRITICAL HIT! " + event.getString("attackerName") +
                       " deals " + event.getInt("damage") + " damage!";

            case HEAL:
                return event.getString("targetName") + " heals for " +
                       event.getInt("amount") + " HP. (" +
                       event.getInt("currentHp") + "/" + event.getInt("maxHp") + ")";

            case PLAYER_BASIC_ATTACK:
                String critText = event.getInt("isCritical") != 0 ? " (CRIT!)" : "";
                return event.getString("attackerName") + " attacks " +
                       event.getString("targetName") + " for " +
                       event.getInt("damage") + " damage" + critText;

            case SPELL_CAST:
                return event.getString("casterName") + " casts " +
                       event.getString("spellName") + "! (cost: " +
                       event.getInt("manaCost") + " MP)";

            case ENEMY_ATTACK:
                return event.getString("attackerName") + " attacks for " +
                       event.getInt("damage") + " damage!";

            case ENEMY_TELEGRAPH:
                return event.getString("telegraphMessage");

            case ENEMY_ABILITY_FIRED:
                return event.getString("attackerName") + " uses " +
                       event.getString("abilityName") + " for " +
                       event.getInt("damage") + " damage!";

            case ENEMY_DEFEATED:
                return event.getString("enemyName") + " has been defeated!";

            case PLAYER_DEFEATED:
                return event.getString("playerName") + " has fallen at wave " +
                       event.getInt("waveNumber") + "...";

            case WAVE_COMPLETE:
                return "Wave " + event.getInt("waveNumber") + " complete!";

            case WAVE_START:
                return "Wave " + event.getInt("waveNumber") + " begins!";

            case CHEST_FOUND:
                return "A chest appears!";

            case ITEM_EQUIPPED:
                return "Equipped: " + event.getString("itemName");

            case STATUS_APPLIED:
                return event.getString("statusType") + " applied! (duration: " +
                       event.getInt("duration") + " turns)";

            case STATUS_EXPIRED:
                return event.getString("statusType") + " has worn off.";

            case SKILL_UNLOCKED:
                return "Skill unlocked: " + event.getString("skillName") + "!";

            case SKILL_ON_COOLDOWN:
                return event.getString("skillName") + " is on cooldown (" +
                       event.getInt("turnsRemaining") + " turns remaining)";

            case MANA_INSUFFICIENT:
                return "Not enough mana!";

            case GAME_VICTORY:
                return event.getString("playerName") + " has conquered the Plague of Danjin!";

            case SHIELD_BLOCKED:
                return "Shield blocks " + event.getInt("blockedDamage") + " damage!";

            case SHIELD_BROKEN:
                return event.getString("targetName") + "'s shield shatters!";

            case CURSE_APPLIED:
                return "CURSED! " + event.getString("effect");

            case FLAVOR_TEXT:
                return event.getString("text");

            default:
                return null;
        }
    }

    private Color getColorForEvent(GameEventType type) {
        switch (type) {
            case DAMAGE_DEALT:
            case PLAYER_DEFEATED:
            case MANA_INSUFFICIENT:
                return Color.RED;

            case CRITICAL_HIT:
                return Color.YELLOW;

            case HEAL:
            case MANA_REGEN:
                return Color.GREEN;

            case ENEMY_ATTACK:
            case ENEMY_TELEGRAPH:
            case ENEMY_ABILITY_FIRED:
                return Color.ORANGE;

            case SKILL_UNLOCKED:
            case ITEM_EQUIPPED:
                return Color.CYAN;

            case CURSE_APPLIED:
                return Color.PURPLE;

            default:
                return Color.WHITE;
        }
    }

    /**
     * Represents a single log entry with its color.
     */
    private static class LogEntry {
        final String message;
        final Color color;

        LogEntry(String message, Color color) {
            this.message = message;
            this.color = color;
        }
    }
}
