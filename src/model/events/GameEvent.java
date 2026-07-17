package model.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GameEvent {
    private final GameEventType type;
    private final Map<String, Object> data;

    public GameEvent(GameEventType type, Map<String, Object> data) {
        this.type = type;
        this.data = Collections.unmodifiableMap(new HashMap<>(data));
    }

    public GameEvent(GameEventType type) {
        this.type = type;
        this.data = Collections.emptyMap();
    }

    public GameEventType getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String getString(String key) {
        Object val = data.get(key);
        return val != null ? val.toString() : null;
    }

    public int getInt(String key) {
        Object val = data.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }

    // Builder-style static factory for convenience
    public static Builder builder(GameEventType type) {
        return new Builder(type);
    }

    public static class Builder {
        private final GameEventType type;
        private final Map<String, Object> data = new HashMap<>();

        public Builder(GameEventType type) {
            this.type = type;
        }

        public Builder put(String key, Object value) {
            data.put(key, value);
            return this;
        }

        public GameEvent build() {
            return new GameEvent(type, data);
        }
    }
}
