package model.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds metadata for a dungeon area including its theme, story text,
 * unique mechanic, encounters, and reward key.
 */
public class AreaData {
    private final Area area;
    private final String name;
    private final String description;
    private final String theme;
    private final String storyText;
    private final String uniqueMechanic;
    private final String rewardKey;
    private final List<Encounter> encounters;
    private final String bossDescription;

    public AreaData(Area area, String name, String description, String theme,
                    String storyText, String uniqueMechanic, String rewardKey,
                    List<Encounter> encounters, String bossDescription) {
        this.area = area;
        this.name = name;
        this.description = description;
        this.theme = theme;
        this.storyText = storyText;
        this.uniqueMechanic = uniqueMechanic;
        this.rewardKey = rewardKey;
        this.encounters = encounters != null
                ? Collections.unmodifiableList(new ArrayList<>(encounters))
                : Collections.emptyList();
        this.bossDescription = bossDescription;
    }

    public Area getArea() {
        return area;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTheme() {
        return theme;
    }

    public String getStoryText() {
        return storyText;
    }

    public String getUniqueMechanic() {
        return uniqueMechanic;
    }

    public String getRewardKey() {
        return rewardKey;
    }

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public String getBossDescription() {
        return bossDescription;
    }
}
