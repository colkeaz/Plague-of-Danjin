package model.status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;

/**
 * Manages active status effects on a character.
 * Handles adding, ticking, expiring, and querying effects.
 * Extends GameEventDispatcher to emit status-related events.
 */
public class StatusManager extends GameEventDispatcher {
    private static final int MAX_POISON_STACKS = 3;

    private final List<StatusEffect> activeEffects = new ArrayList<>();

    /**
     * Adds a status effect. Poison is stackable up to 3 stacks.
     * Other effects replace existing effects of the same type.
     * Fires a STATUS_APPLIED event.
     */
    public void addEffect(StatusEffect effect) {
        if (effect.getType() == StatusType.POISON) {
            long poisonCount = activeEffects.stream()
                    .filter(e -> e.getType() == StatusType.POISON)
                    .count();
            if (poisonCount >= MAX_POISON_STACKS) {
                return; // Cannot stack more poison
            }
        } else {
            // Non-stackable: remove existing effect of same type
            activeEffects.removeIf(e -> e.getType() == effect.getType());
        }

        activeEffects.add(effect);

        fireEvent(GameEvent.builder(GameEventType.STATUS_APPLIED)
                .put("statusType", effect.getType().name())
                .put("duration", effect.getDuration())
                .put("potency", effect.getPotency())
                .put("sourceName", effect.getSourceName())
                .build());
    }

    /**
     * Processes all active effects at the start of a turn.
     * Returns a list of GameEvents describing what happened (poison damage, regen, etc.).
     * Removes expired effects and fires STATUS_EXPIRED events for them.
     */
    public List<GameEvent> tickAll() {
        List<GameEvent> events = new ArrayList<>();

        Iterator<StatusEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            int value = effect.tick();

            GameEvent tickEvent = GameEvent.builder(GameEventType.STATUS_TICKED)
                    .put("statusType", effect.getType().name())
                    .put("potency", value)
                    .put("remainingDuration", effect.getDuration())
                    .put("sourceName", effect.getSourceName())
                    .build();
            events.add(tickEvent);
            fireEvent(tickEvent);

            if (effect.isExpired()) {
                GameEvent expiredEvent = GameEvent.builder(GameEventType.STATUS_EXPIRED)
                        .put("statusType", effect.getType().name())
                        .put("sourceName", effect.getSourceName())
                        .build();
                events.add(expiredEvent);
                fireEvent(expiredEvent);
                iterator.remove();
            }
        }

        return events;
    }

    /**
     * Returns true if the character has an active effect of the given type.
     */
    public boolean hasEffect(StatusType type) {
        return activeEffects.stream().anyMatch(e -> e.getType() == type);
    }

    /**
     * Removes all effects of the given type.
     */
    public void removeEffect(StatusType type) {
        activeEffects.removeIf(e -> e.getType() == type);
    }

    /**
     * Returns true if the character is currently stunned.
     */
    public boolean isStunned() {
        return hasEffect(StatusType.STUN);
    }

    /**
     * Returns an unmodifiable view of active effects for inspection.
     */
    public List<StatusEffect> getActiveEffects() {
        return List.copyOf(activeEffects);
    }
}
