package model.status;

import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;

/**
 * Represents an active status effect on a character.
 * Each effect has a type, remaining duration, potency (damage/heal per tick),
 * and the name of the source that applied it.
 */
public class StatusEffect extends GameEventDispatcher {
    private final StatusType type;
    private int duration;
    private final int potency;
    private final String sourceName;

    public StatusEffect(StatusType type, int duration, int potency, String sourceName) {
        this.type = type;
        this.duration = duration;
        this.potency = potency;
        this.sourceName = sourceName;
    }

    /**
     * Decrements the duration by 1 and returns the effect value (potency).
     * Fires a STATUS_TICKED event.
     */
    public int tick() {
        duration--;

        fireEvent(GameEvent.builder(GameEventType.STATUS_TICKED)
                .put("statusType", type.name())
                .put("potency", potency)
                .put("remainingDuration", duration)
                .put("sourceName", sourceName)
                .build());

        return potency;
    }

    /**
     * Returns true if this effect has expired (duration <= 0).
     */
    public boolean isExpired() {
        return duration <= 0;
    }

    public StatusType getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public int getPotency() {
        return potency;
    }

    public String getSourceName() {
        return sourceName;
    }
}
