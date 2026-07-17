package model.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for objects that dispatch game events to registered listeners.
 * Uses a snapshot approach for safe iteration during event dispatch,
 * preventing ConcurrentModificationException if a listener adds or removes
 * listeners during event processing.
 */
public class GameEventDispatcher {
    private final List<GameEventListener> listeners = new ArrayList<>();

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    protected void fireEvent(GameEvent event) {
        // Snapshot the listener list to avoid ConcurrentModificationException
        // if a listener modifies the list during dispatch (e.g., removes itself).
        GameEventListener[] snapshot = listeners.toArray(new GameEventListener[0]);
        for (GameEventListener listener : snapshot) {
            listener.onEvent(event);
        }
    }
}
