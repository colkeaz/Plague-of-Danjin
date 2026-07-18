package model.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an area-specific narrative event that presents the player
 * with choices. Parallels EventRoomManager but is tied to a specific area
 * and encounter index.
 */
public class AreaEvent {
    private final String name;
    private final String description;
    private final List<String> choices;
    private final Area area;
    private final int encounterIndex;

    public AreaEvent(String name, String description, List<String> choices,
                     Area area, int encounterIndex) {
        this.name = name;
        this.description = description;
        this.choices = choices != null
                ? Collections.unmodifiableList(new ArrayList<>(choices))
                : Collections.emptyList();
        this.area = area;
        this.encounterIndex = encounterIndex;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getChoices() {
        return choices;
    }

    public Area getArea() {
        return area;
    }

    public int getEncounterIndex() {
        return encounterIndex;
    }
}
