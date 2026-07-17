package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Player;
import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;

/**
 * Tracks escalating difficulty modifiers throughout a run.
 * Handles Danjin's Curse (every 5 waves reduce max HP by 5) and
 * permanent modifiers applied by event room choices.
 */
public class RunModifiers extends GameEventDispatcher {

    private final List<String> appliedModifiers = new ArrayList<>();
    private boolean danjinHeartAbsorbed = false;
    private boolean danjinHeartShattered = false;
    private int permanentDamagePerTurn = 0;

    /**
     * Applies wave-based modifiers. Called at the start of each wave.
     * Danjin's Curse fires at waves 5, 10, 15, 20: reduces player maxHp by 5.
     */
    public void applyWaveModifiers(int wave, Player player) {
        if (wave > 0 && wave % 5 == 0) {
            applyDanjinsCurse(player);
        }
    }

    /**
     * Applies the Danjin's Curse: -5 max HP permanently.
     * Fires a CURSE_APPLIED event for the view to display.
     */
    private void applyDanjinsCurse(Player player) {
        player.reduceMaxHp(5);

        appliedModifiers.add("Danjin's Curse: -5 Max HP");

        fireEvent(GameEvent.builder(GameEventType.CURSE_APPLIED)
                .put("curseName", "Danjin's Curse")
                .put("effect", "Max HP reduced by 5")
                .put("currentMaxHp", player.getMaxHp())
                .build());
    }

    /**
     * Applies permanent damage per turn (from Danjin's Heart absorption).
     * Called at the start of each player turn if the flag is set.
     */
    public void applyPermanentDamagePerTurn(Player player, int damage) {
        player.takeDamage(damage);
    }

    /**
     * Adds a permanent modifier (from event rooms).
     * Fires a RUN_MODIFIER_APPLIED event.
     */
    public void addModifier(String description) {
        appliedModifiers.add(description);
        fireEvent(GameEvent.builder(GameEventType.RUN_MODIFIER_APPLIED)
                .put("modifier", description)
                .build());
    }

    /**
     * Returns an unmodifiable view of all applied modifiers.
     */
    public List<String> getAppliedModifiers() {
        return Collections.unmodifiableList(appliedModifiers);
    }

    public boolean isDanjinHeartAbsorbed() {
        return danjinHeartAbsorbed;
    }

    public void setDanjinHeartAbsorbed(boolean absorbed) {
        this.danjinHeartAbsorbed = absorbed;
    }

    public boolean isDanjinHeartShattered() {
        return danjinHeartShattered;
    }

    public void setDanjinHeartShattered(boolean shattered) {
        this.danjinHeartShattered = shattered;
    }

    public int getPermanentDamagePerTurn() {
        return permanentDamagePerTurn;
    }

    public void setPermanentDamagePerTurn(int damage) {
        this.permanentDamagePerTurn = damage;
    }
}
