package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import model.Enemy;
import model.world.Area;
import model.world.AreaData;
import model.world.AreaEvent;
import model.world.Encounter;
import model.world.Encounter.EncounterType;
import model.world.WorldState;

/**
 * Central controller for world navigation in Story Mode.
 * Manages area data, encounter progression, and world state.
 * Provides methods for area selection, encounter generation,
 * and area-specific mechanic application.
 */
public class WorldManager {
    private final Map<Area, AreaData> areaDataMap;
    private final Map<Area, AreaEvent> areaEvents;
    private final WorldState worldState;
    private final AreaEncounterGenerator encounterGenerator;

    public WorldManager() {
        this.areaDataMap = new EnumMap<>(Area.class);
        this.areaEvents = new EnumMap<>(Area.class);
        this.worldState = new WorldState();
        this.encounterGenerator = new AreaEncounterGenerator();
        initializeAreaData();
        initializeAreaEvents();
    }

    /**
     * Creates a WorldManager with a pre-existing WorldState (for save restoration).
     */
    public WorldManager(WorldState restoredState) {
        this.areaDataMap = new EnumMap<>(Area.class);
        this.areaEvents = new EnumMap<>(Area.class);
        this.worldState = restoredState != null ? restoredState : new WorldState();
        this.encounterGenerator = new AreaEncounterGenerator();
        initializeAreaData();
        initializeAreaEvents();
    }

    /**
     * Returns the list of areas currently available to the player.
     */
    public List<Area> getAvailableAreas() {
        List<Area> available = new ArrayList<>();
        for (Area area : Area.values()) {
            if (worldState.isAreaUnlocked(area) && !worldState.isAreaCompleted(area)) {
                available.add(area);
            }
        }
        return available;
    }

    /**
     * Enters the specified area, setting it as the current area.
     * Returns true if entry was successful, false if the area is locked.
     */
    public boolean enterArea(Area area) {
        if (!worldState.isAreaUnlocked(area)) {
            return false;
        }
        worldState.setCurrentArea(area);
        return true;
    }

    /**
     * Returns the next encounter for the current area, or null if the area is complete.
     */
    public Encounter getNextEncounter() {
        Area current = worldState.getCurrentArea();
        if (current == null) return null;

        AreaData data = areaDataMap.get(current);
        if (data == null) return null;

        int index = worldState.getCurrentEncounterIndex(current);
        List<Encounter> encounters = data.getEncounters();

        if (index >= encounters.size()) {
            return null; // Area complete
        }
        return encounters.get(index);
    }

    /**
     * Generates the enemy array for the next encounter in the current area.
     * Returns null if there is no next encounter or it is an event encounter.
     */
    public Enemy[] generateCurrentEncounterEnemies() {
        Encounter encounter = getNextEncounter();
        if (encounter == null) return null;

        Area current = worldState.getCurrentArea();
        return encounterGenerator.generateEnemies(current, encounter);
    }

    /**
     * Advances the encounter progress in the current area after a successful encounter.
     * If the area is complete after this advance, marks it as completed and
     * awards the area key.
     */
    public void advanceAfterEncounter() {
        Area current = worldState.getCurrentArea();
        if (current == null) return;

        worldState.advanceEncounter(current);

        AreaData data = areaDataMap.get(current);
        if (data == null) return;

        int newIndex = worldState.getCurrentEncounterIndex(current);
        if (newIndex >= data.getEncounters().size()) {
            worldState.completeArea(current);
            if (data.getRewardKey() != null && !data.getRewardKey().isEmpty()) {
                worldState.collectKey(data.getRewardKey());
            }
        }
    }

    /**
     * Retreats from the current area back to the hub (Danjin's Core).
     */
    public void retreatToHub() {
        worldState.setCurrentArea(Area.DANJINS_CORE);
    }

    /**
     * Returns whether the specified area has been completed.
     */
    public boolean isAreaComplete(Area area) {
        return worldState.isAreaCompleted(area);
    }

    /**
     * Returns the area mechanic identifier for the current area.
     * Used by CombatEngine to apply per-turn effects.
     * Returns null if no mechanic applies.
     */
    public String getActiveAreaMechanic() {
        Area current = worldState.getCurrentArea();
        if (current == null) return null;

        AreaData data = areaDataMap.get(current);
        if (data == null) return null;

        return data.getUniqueMechanic();
    }

    /**
     * Returns the AreaData for the specified area.
     */
    public AreaData getAreaData(Area area) {
        return areaDataMap.get(area);
    }

    /**
     * Returns the AreaEvent for the specified area, if any.
     */
    public AreaEvent getAreaEvent(Area area) {
        return areaEvents.get(area);
    }

    /**
     * Returns the current WorldState.
     */
    public WorldState getWorldState() {
        return worldState;
    }

    /**
     * Records the player's choice for the current area's event.
     */
    public void recordEventChoice(String eventName, int choiceIndex) {
        worldState.recordEventChoice(eventName, choiceIndex);
    }

    /**
     * Returns whether the rest shrine has been used this run.
     */
    public boolean isRestShrineUsed() {
        return worldState.isRestShrineUsed();
    }

    /**
     * Marks the rest shrine as used.
     */
    public void useRestShrine() {
        worldState.setRestShrineUsed(true);
    }

    // --- Initialization ---

    private void initializeAreaData() {
        areaDataMap.put(Area.DANJINS_CORE, createDanjinsCore());
        areaDataMap.put(Area.GOBLIN_WARRENS, createGoblinWarrens());
        areaDataMap.put(Area.BONE_CATHEDRAL, createBoneCathedral());
        areaDataMap.put(Area.PLAGUE_GARDENS, createPlagueGardens());
        areaDataMap.put(Area.LICHS_THRONE, createLichsThrone());
    }

    private void initializeAreaEvents() {
        areaEvents.put(Area.GOBLIN_WARRENS, new AreaEvent(
                "Goblin Prisoner",
                "A goblin prisoner cowers in a cage. He offers information about the Goblin King's weakness in exchange for freedom.",
                Arrays.asList("Free the prisoner (reveal boss weakness)", "Leave him"),
                Area.GOBLIN_WARRENS, 2
        ));

        areaEvents.put(Area.BONE_CATHEDRAL, new AreaEvent(
                "Fallen Paladin's Spirit",
                "The ghostly figure of a once-great paladin appears before you, offering a final blessing.",
                Arrays.asList("Accept blessing (HOLY damage +25% in this area)", "Refuse (gain rare item)"),
                Area.BONE_CATHEDRAL, 2
        ));

        areaEvents.put(Area.PLAGUE_GARDENS, new AreaEvent(
                "The Corrupted Tree",
                "A massive tree pulses with plague energy. Its roots spread toxins throughout the garden.",
                Arrays.asList("Purify the tree (removes toxic atmosphere)", "Absorb its power (+20 ATK, take poison 5/turn)"),
                Area.PLAGUE_GARDENS, 2
        ));

        areaEvents.put(Area.LICHS_THRONE, new AreaEvent(
                "The Lich's Offer",
                "The Lich appears in a mirror, offering power in exchange for surrender.",
                Arrays.asList("Accept power (+50 ATK/DEF, Lich heals at 25% HP)", "Refuse (fight at current strength)"),
                Area.LICHS_THRONE, 2
        ));
    }

    private AreaData createDanjinsCore() {
        List<Encounter> encounters = new ArrayList<>();
        encounters.add(Encounter.builder()
                .index(0)
                .description("Patrol: Mixed enemies guard the core")
                .flavorText("Shadows stir in the ancient corridors of Danjin's Core.")
                .type(EncounterType.COMBAT)
                .addEnemyType("Goblin").addEnemyType("Skeleton")
                .difficulty(2)
                .build());
        encounters.add(Encounter.builder()
                .index(1)
                .description("Patrol: Plague-touched wanderers")
                .flavorText("The air grows thick with the scent of decay.")
                .type(EncounterType.COMBAT)
                .addEnemyType("PlagueGoblin").addEnemyType("PlagueGoblin")
                .difficulty(2)
                .build());
        encounters.add(Encounter.builder()
                .index(2)
                .description("Patrol: Skeletal sentinels")
                .flavorText("Bones clatter in the darkness as sentinels emerge from the walls.")
                .type(EncounterType.COMBAT)
                .addEnemyType("Skeleton").addEnemyType("ShieldedSkeleton")
                .difficulty(3)
                .build());

        return new AreaData(Area.DANJINS_CORE,
                "Danjin's Core",
                "The central hub of the dungeon. Patrols wander the corridors.",
                "ancient_stone",
                "The heart of the dungeon beats with dark energy. From here, passages lead to the corrupted depths.",
                null,
                null,
                encounters,
                null);
    }

    private AreaData createGoblinWarrens() {
        List<Encounter> encounters = new ArrayList<>();

        // Encounter 1: 2 Goblins (easy warmup)
        encounters.add(Encounter.builder()
                .index(0)
                .description("2 Goblins")
                .flavorText("A pair of goblins snarl at you from behind crude barricades.")
                .type(EncounterType.COMBAT)
                .addEnemyType("Goblin").addEnemyType("Goblin")
                .difficulty(1)
                .build());

        // Encounter 2: 3 Goblins (one is PlagueGoblin)
        encounters.add(Encounter.builder()
                .index(1)
                .description("3 Goblins (one Plague Goblin)")
                .flavorText("The stench of plague fills this passage. One goblin glows with sickly green light.")
                .type(EncounterType.COMBAT)
                .addEnemyType("Goblin").addEnemyType("Goblin").addEnemyType("PlagueGoblin")
                .difficulty(2)
                .build());

        // Encounter 3: EVENT - Goblin Prisoner
        encounters.add(Encounter.builder()
                .index(2)
                .description("Event: Goblin Prisoner")
                .flavorText("A goblin prisoner cowers in a cage, begging for mercy.")
                .type(EncounterType.EVENT)
                .difficulty(0)
                .build());

        // Encounter 4: 2 PlagueGoblins + 1 GoblinChieftain (mini-boss)
        encounters.add(Encounter.builder()
                .index(3)
                .description("2 Plague Goblins + Goblin Chieftain")
                .flavorText("The Chieftain commands his plague-touched guards with brutal efficiency.")
                .type(EncounterType.COMBAT)
                .addEnemyType("PlagueGoblin").addEnemyType("PlagueGoblin").addEnemyType("GoblinChieftain")
                .difficulty(4)
                .build());

        // Encounter 5: 3 PlagueGoblins (ambush)
        encounters.add(Encounter.builder()
                .index(4)
                .description("3 Plague Goblins (Ambush!)")
                .flavorText("The ceiling erupts! Plague goblins drop from above in a coordinated ambush!")
                .type(EncounterType.COMBAT)
                .addEnemyType("PlagueGoblin").addEnemyType("PlagueGoblin").addEnemyType("PlagueGoblin")
                .difficulty(3)
                .ambush(true)
                .build());

        // Encounter 6: BOSS - GoblinKing
        encounters.add(Encounter.builder()
                .index(5)
                .description("BOSS: The Goblin King")
                .flavorText("The Goblin King sits upon his throne of stolen treasures, flanked by torchlight.")
                .type(EncounterType.BOSS)
                .addEnemyType("GoblinKing")
                .difficulty(5)
                .build());

        return new AreaData(Area.GOBLIN_WARRENS,
                "Goblin Warrens",
                "A maze of tunnels overrun by goblin tribes corrupted by the plague.",
                "goblin_cave",
                "The warrens echo with guttural shouts and the scraping of crude weapons. The goblins here have been twisted by exposure to the plague.",
                "GOBLIN_AMBUSH",
                "Goblin King's Key",
                encounters,
                "The Goblin King rules from his throne of stolen gold, his body bloated with plague energy.");
    }

    private AreaData createBoneCathedral() {
        List<Encounter> encounters = new ArrayList<>();

        // Encounter 1: 2 Skeletons
        encounters.add(Encounter.builder()
                .index(0)
                .description("2 Skeletons")
                .flavorText("Skeletal warriors rise from the cathedral pews, their eyes glowing with unholy light.")
                .type(EncounterType.COMBAT)
                .addEnemyType("Skeleton").addEnemyType("Skeleton")
                .difficulty(2)
                .build());

        // Encounter 2: 2 ShieldedSkeletons
        encounters.add(Encounter.builder()
                .index(1)
                .description("2 Shielded Skeletons")
                .flavorText("Heavy shields clatter as armored undead march down the nave.")
                .type(EncounterType.COMBAT)
                .addEnemyType("ShieldedSkeleton").addEnemyType("ShieldedSkeleton")
                .difficulty(3)
                .build());

        // Encounter 3: EVENT - Fallen Paladin's Spirit
        encounters.add(Encounter.builder()
                .index(2)
                .description("Event: Fallen Paladin's Spirit")
                .flavorText("A ghostly light emanates from the altar. A spirit reaches out to you.")
                .type(EncounterType.EVENT)
                .difficulty(0)
                .build());

        // Encounter 4: 3 Skeletons + 1 ShieldedSkeleton
        encounters.add(Encounter.builder()
                .index(3)
                .description("3 Skeletons + 1 Shielded Skeleton")
                .flavorText("The dead pour from the crypts beneath the cathedral floor.")
                .type(EncounterType.COMBAT)
                .addEnemyType("Skeleton").addEnemyType("Skeleton").addEnemyType("Skeleton").addEnemyType("ShieldedSkeleton")
                .difficulty(4)
                .build());

        // Encounter 5: 2 ShieldedSkeletons (consecrated ground active)
        encounters.add(Encounter.builder()
                .index(4)
                .description("2 Shielded Skeletons (Consecrated Ground)")
                .flavorText("Holy symbols glow on the floor. The undead seem weakened here.")
                .type(EncounterType.COMBAT)
                .addEnemyType("ShieldedSkeleton").addEnemyType("ShieldedSkeleton")
                .difficulty(3)
                .build());

        // Encounter 6: BOSS - BoneColossus
        encounters.add(Encounter.builder()
                .index(5)
                .description("BOSS: Bone Colossus")
                .flavorText("A towering construct of fused bones rises from the cathedral's crypt.")
                .type(EncounterType.BOSS)
                .addEnemyType("BoneColossus")
                .difficulty(5)
                .build());

        return new AreaData(Area.BONE_CATHEDRAL,
                "Bone Cathedral",
                "A vast underground cathedral built from the bones of fallen warriors.",
                "undead_cathedral",
                "Pillars of fused bone reach into the darkness above. The cathedral echoes with the prayers of the dead.",
                "CONSECRATED_GROUND",
                "Cathedral Key",
                encounters,
                "The Bone Colossus guards the cathedral's deepest crypt, a towering horror assembled from a thousand skeletons.");
    }

    private AreaData createPlagueGardens() {
        List<Encounter> encounters = new ArrayList<>();

        // Encounter 1: 2 PlagueGoblins (toxic atmosphere)
        encounters.add(Encounter.builder()
                .index(0)
                .description("2 Plague Goblins (Toxic Atmosphere)")
                .flavorText("Poisonous mist rolls across the ground. Everything here breathes plague.")
                .type(EncounterType.COMBAT)
                .addEnemyType("PlagueGoblin").addEnemyType("PlagueGoblin")
                .difficulty(3)
                .build());

        // Encounter 2: 1 PlagueElemental
        encounters.add(Encounter.builder()
                .index(1)
                .description("1 Plague Elemental")
                .flavorText("A creature of pure plague energy coalesces from the toxic mist.")
                .type(EncounterType.COMBAT)
                .addEnemyType("PlagueElemental")
                .difficulty(3)
                .build());

        // Encounter 3: EVENT - The Corrupted Tree
        encounters.add(Encounter.builder()
                .index(2)
                .description("Event: The Corrupted Tree")
                .flavorText("A massive tree pulses with plague energy, its roots spreading toxins through the soil.")
                .type(EncounterType.EVENT)
                .difficulty(0)
                .build());

        // Encounter 4: 2 PlagueElementals + 1 PlagueGoblin
        encounters.add(Encounter.builder()
                .index(3)
                .description("2 Plague Elementals + 1 Plague Goblin")
                .flavorText("The garden's guardians emerge from the corrupted flora.")
                .type(EncounterType.COMBAT)
                .addEnemyType("PlagueElemental").addEnemyType("PlagueElemental").addEnemyType("PlagueGoblin")
                .difficulty(4)
                .build());

        // Encounter 5: 3 PlagueGoblins
        encounters.add(Encounter.builder()
                .index(4)
                .description("3 Plague Goblins")
                .flavorText("A squad of plague-touched goblins blocks the path to the garden's heart.")
                .type(EncounterType.COMBAT)
                .addEnemyType("PlagueGoblin").addEnemyType("PlagueGoblin").addEnemyType("PlagueGoblin")
                .difficulty(3)
                .build());

        // Encounter 6: BOSS - Thornmother
        encounters.add(Encounter.builder()
                .index(5)
                .description("BOSS: Thornmother")
                .flavorText("The garden's corrupted heart pulses as the Thornmother awakens from her root-throne.")
                .type(EncounterType.BOSS)
                .addEnemyType("Thornmother")
                .difficulty(5)
                .build());

        return new AreaData(Area.PLAGUE_GARDENS,
                "Plague Gardens",
                "Once beautiful gardens now twisted by concentrated plague energy.",
                "toxic_garden",
                "What were once pristine gardens are now a rotting labyrinth of toxic flora. The air itself is poison here.",
                "TOXIC_ATMOSPHERE",
                "Garden Key",
                encounters,
                "The Thornmother commands the corrupted gardens, her vine tendrils reaching into every dark corner.");
    }

    private AreaData createLichsThrone() {
        List<Encounter> encounters = new ArrayList<>();

        // Encounter 1: Elite 1 ShieldedSkeleton + 1 PlagueGoblin + 1 Skeleton (buffed +50%)
        encounters.add(Encounter.builder()
                .index(0)
                .description("Elite Guards: Shielded Skeleton + Plague Goblin + Skeleton")
                .flavorText("The Lich's elite guard stands watch. Each warrior radiates dark power.")
                .type(EncounterType.COMBAT)
                .addEnemyType("ShieldedSkeleton").addEnemyType("PlagueGoblin").addEnemyType("Skeleton")
                .difficulty(5)
                .build());

        // Encounter 2: Elite 2 Skeletons (150 HP each, handled by buff)
        encounters.add(Encounter.builder()
                .index(1)
                .description("Elite Sentinels: 2 Empowered Skeletons")
                .flavorText("Massive skeletal sentinels block the way, their bones crackling with necrotic energy.")
                .type(EncounterType.COMBAT)
                .addEnemyType("Skeleton").addEnemyType("Skeleton")
                .difficulty(5)
                .build());

        // Encounter 3: EVENT - The Lich's Offer
        encounters.add(Encounter.builder()
                .index(2)
                .description("Event: The Lich's Offer")
                .flavorText("A mirror of dark glass reveals the Lich's visage. He speaks with honeyed words.")
                .type(EncounterType.EVENT)
                .difficulty(0)
                .build());

        // Encounter 4: FINAL BOSS - Lich
        encounters.add(Encounter.builder()
                .index(3)
                .description("FINAL BOSS: The Necromancer Lich")
                .flavorText("The throne room echoes with the Lich's laughter. This is the final battle.")
                .type(EncounterType.BOSS)
                .addEnemyType("Lich")
                .difficulty(6)
                .build());

        return new AreaData(Area.LICHS_THRONE,
                "Lich's Throne",
                "The final sanctum where the Necromancer Lich awaits.",
                "dark_throne",
                "Three keys open the way to the throne room. The Lich awaits, his power drawn from the suffering of all Morthga.",
                "VOID_INSTABILITY",
                null,
                encounters,
                "The Necromancer Lich commands death itself from his throne of black iron.");
    }
}
