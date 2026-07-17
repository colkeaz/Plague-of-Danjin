package model.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single combat or event encounter within an area.
 * Each encounter has a type, enemy composition, difficulty, and flavor text.
 */
public class Encounter {
    private final int index;
    private final String description;
    private final String flavorText;
    private final EncounterType type;
    private final List<String> enemyTypes;
    private final int difficulty;
    private final boolean ambush;

    /**
     * The type of encounter the player will face.
     */
    public enum EncounterType {
        COMBAT,
        EVENT,
        BOSS
    }

    private Encounter(Builder builder) {
        this.index = builder.index;
        this.description = builder.description;
        this.flavorText = builder.flavorText;
        this.type = builder.type;
        this.enemyTypes = builder.enemyTypes != null
                ? Collections.unmodifiableList(new ArrayList<>(builder.enemyTypes))
                : Collections.emptyList();
        this.difficulty = builder.difficulty;
        this.ambush = builder.ambush;
    }

    /**
     * Creates a new Builder for constructing Encounter instances.
     */
    public static Builder builder() {
        return new Builder();
    }

    public int getIndex() {
        return index;
    }

    public String getDescription() {
        return description;
    }

    public String getFlavorText() {
        return flavorText;
    }

    public EncounterType getType() {
        return type;
    }

    public List<String> getEnemyTypes() {
        return enemyTypes;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public boolean isAmbush() {
        return ambush;
    }

    /**
     * Builder for constructing Encounter instances with a fluent API.
     */
    public static class Builder {
        private int index;
        private String description = "";
        private String flavorText = "";
        private EncounterType type = EncounterType.COMBAT;
        private List<String> enemyTypes = new ArrayList<>();
        private int difficulty = 1;
        private boolean ambush = false;

        public Builder index(int index) {
            this.index = index;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder flavorText(String flavorText) {
            this.flavorText = flavorText;
            return this;
        }

        public Builder type(EncounterType type) {
            this.type = type;
            return this;
        }

        public Builder enemyTypes(List<String> enemyTypes) {
            this.enemyTypes = enemyTypes;
            return this;
        }

        public Builder addEnemyType(String enemyType) {
            this.enemyTypes.add(enemyType);
            return this;
        }

        public Builder difficulty(int difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder ambush(boolean ambush) {
            this.ambush = ambush;
            return this;
        }

        public Encounter build() {
            return new Encounter(this);
        }
    }
}
