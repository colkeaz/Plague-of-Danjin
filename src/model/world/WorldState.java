package model.world;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks the player's exploration progress through the Dungeon of Danjin.
 * Manages completed areas, collected keys, encounter progress, and event choices.
 */
public class WorldState {
    private final Set<Area> completedAreas;
    private final Set<String> keysCollected;
    private final Map<Area, Integer> encounterProgress;
    private Area currentArea;
    private boolean restShrineUsed;
    private final Map<String, Integer> eventChoices;

    public WorldState() {
        this.completedAreas = EnumSet.noneOf(Area.class);
        this.keysCollected = new HashSet<>();
        this.encounterProgress = new EnumMap<>(Area.class);
        this.currentArea = Area.DANJINS_CORE;
        this.restShrineUsed = false;
        this.eventChoices = new HashMap<>();
    }

    /**
     * Returns whether the given area is unlocked for exploration.
     * DANJINS_CORE is always accessible. LICHS_THRONE requires 3 area keys.
     * All other areas are always unlocked.
     */
    public boolean isAreaUnlocked(Area area) {
        if (area == Area.DANJINS_CORE) {
            return true;
        }
        if (area == Area.LICHS_THRONE) {
            return isLichsThroneUnlocked();
        }
        // Goblin Warrens, Bone Cathedral, Plague Gardens are always accessible
        return true;
    }

    /**
     * Returns whether the Lich's Throne is accessible.
     * Requires all 3 area keys to be collected.
     */
    public boolean isLichsThroneUnlocked() {
        return keysCollected.size() >= 3;
    }

    /**
     * Returns the current encounter index for the given area.
     */
    public int getCurrentEncounterIndex(Area area) {
        return encounterProgress.getOrDefault(area, 0);
    }

    /**
     * Advances the encounter progress for the given area by one.
     */
    public void advanceEncounter(Area area) {
        int current = encounterProgress.getOrDefault(area, 0);
        encounterProgress.put(area, current + 1);
    }

    /**
     * Marks the given area as completed.
     */
    public void completeArea(Area area) {
        completedAreas.add(area);
    }

    /**
     * Collects a key by name (e.g., "Goblin King's Key", "Cathedral Key", "Garden Key").
     */
    public void collectKey(String key) {
        keysCollected.add(key);
    }

    /**
     * Records a moral/narrative event choice for tracking purposes.
     * These choices affect the ending.
     */
    public void recordEventChoice(String eventName, int choiceIndex) {
        eventChoices.put(eventName, choiceIndex);
    }

    // --- Getters and setters ---

    public Set<Area> getCompletedAreas() {
        return completedAreas;
    }

    public Set<String> getKeysCollected() {
        return keysCollected;
    }

    public Map<Area, Integer> getEncounterProgress() {
        return encounterProgress;
    }

    public Area getCurrentArea() {
        return currentArea;
    }

    public void setCurrentArea(Area currentArea) {
        this.currentArea = currentArea;
    }

    public boolean isRestShrineUsed() {
        return restShrineUsed;
    }

    public void setRestShrineUsed(boolean restShrineUsed) {
        this.restShrineUsed = restShrineUsed;
    }

    public Map<String, Integer> getEventChoices() {
        return eventChoices;
    }

    /**
     * Returns whether the given area has been completed.
     */
    public boolean isAreaCompleted(Area area) {
        return completedAreas.contains(area);
    }
}
