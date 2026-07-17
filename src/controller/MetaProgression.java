package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.events.GameEvent;
import model.events.GameEventListener;
import model.events.GameEventType;

/**
 * Tracks cross-run statistics and unlockable starting bonuses.
 * Implements GameEventListener to track stats in real-time from the CombatEngine.
 * Persists to ~/.plague_of_danjin/meta.json using manual JSON serialization.
 *
 * Defines 6 unlockables:
 * - veteran_blade: first victory -> start with "Veteran's Blade" (+8 ATK weapon)
 * - swift_boots: victory in under 60 turns -> start with +10% crit accessory
 * - iron_constitution: 100 enemies killed total -> +15 max HP
 * - blood_warrior: defeat Lich without casting Holy Light -> +15 ATK, -20 max HP
 * - lucky_coin: complete 5 runs (win or lose) -> chest spawn at 65% instead of 50%
 * - plague_survivor: win with Danjin's Curse active (never shatter heart) -> permanent REGEN 3 HP/turn
 */
public class MetaProgression implements GameEventListener {

    private int totalRuns;
    private int totalVictories;
    private int highestWave;
    private int totalEnemiesKilled;
    private int totalDamageDealt;
    private int totalDamageTaken;
    private int fastestVictoryTurns; // -1 means no victory yet
    private List<String> earnedUnlockIds;

    // Tracking flags for current run (not persisted)
    private transient boolean usedHolyLightThisRun;
    private transient boolean currentRunVictory;
    private transient int currentRunTurns;

    public MetaProgression() {
        this.totalRuns = 0;
        this.totalVictories = 0;
        this.highestWave = 0;
        this.totalEnemiesKilled = 0;
        this.totalDamageDealt = 0;
        this.totalDamageTaken = 0;
        this.fastestVictoryTurns = -1;
        this.earnedUnlockIds = new ArrayList<>();
        this.usedHolyLightThisRun = false;
        this.currentRunVictory = false;
        this.currentRunTurns = 0;
    }

    @Override
    public void onEvent(GameEvent event) {
        GameEventType type = event.getType();
        switch (type) {
            case ENEMY_DEFEATED:
                recordEnemyKill();
                break;
            case DAMAGE_DEALT:
                int finalDamage = event.getInt("finalDamage");
                // Determine if it is player dealing damage or enemy
                String targetName = event.getString("targetName");
                // If target is not the player name, it means player dealt damage to enemy
                // We cannot easily distinguish here, so track all damage dealt events
                recordDamageDealt(finalDamage);
                break;
            case ENEMY_ATTACK:
            case ENEMY_ABILITY_FIRED:
                int enemyDamage = event.getInt("finalDamage");
                if (enemyDamage > 0) {
                    recordDamageTaken(enemyDamage);
                }
                break;
            case SPELL_CAST:
                String spellName = event.getString("spellName");
                if ("Holy Light".equals(spellName)) {
                    usedHolyLightThisRun = true;
                }
                break;
            case PLAYER_BASIC_ATTACK:
                currentRunTurns++;
                break;
            default:
                break;
        }
    }

    /**
     * Called when a new run begins.
     */
    public void recordRunStart() {
        totalRuns++;
        usedHolyLightThisRun = false;
        currentRunVictory = false;
        currentRunTurns = 0;
    }

    /**
     * Called when a run ends (victory or defeat).
     *
     * @param victory true if the player won
     * @param wave the wave reached
     * @param turns the number of turns taken
     */
    public void recordRunEnd(boolean victory, int wave, int turns) {
        if (victory) {
            totalVictories++;
            if (fastestVictoryTurns < 0 || turns < fastestVictoryTurns) {
                fastestVictoryTurns = turns;
            }
        }
        if (wave > highestWave) {
            highestWave = wave;
        }
        currentRunVictory = victory;
        currentRunTurns = turns;
    }

    /**
     * Records an enemy kill.
     */
    public void recordEnemyKill() {
        totalEnemiesKilled++;
    }

    /**
     * Records damage dealt by the player.
     */
    public void recordDamageDealt(int amount) {
        totalDamageDealt += amount;
    }

    /**
     * Records damage taken by the player.
     */
    public void recordDamageTaken(int amount) {
        totalDamageTaken += amount;
    }

    /**
     * Checks conditions and awards any newly earned unlockables.
     * Should be called after recordRunEnd().
     *
     * @param danjinHeartAbsorbed whether the heart was absorbed this run
     * @param danjinHeartShattered whether the heart was shattered this run
     * @param defeatedLich whether the Lich was defeated this run
     */
    public void checkAndAwardUnlocks(boolean danjinHeartAbsorbed,
                                     boolean danjinHeartShattered, boolean defeatedLich) {
        // veteran_blade: first victory
        if (currentRunVictory && !earnedUnlockIds.contains("veteran_blade")) {
            earnedUnlockIds.add("veteran_blade");
        }

        // swift_boots: victory in under 60 turns
        if (currentRunVictory && currentRunTurns < 60 && !earnedUnlockIds.contains("swift_boots")) {
            earnedUnlockIds.add("swift_boots");
        }

        // iron_constitution: 100 enemies killed total
        if (totalEnemiesKilled >= 100 && !earnedUnlockIds.contains("iron_constitution")) {
            earnedUnlockIds.add("iron_constitution");
        }

        // blood_warrior: defeat Lich without casting Holy Light
        if (currentRunVictory && defeatedLich && !usedHolyLightThisRun
                && !earnedUnlockIds.contains("blood_warrior")) {
            earnedUnlockIds.add("blood_warrior");
        }

        // lucky_coin: complete 5 runs (win or lose)
        if (totalRuns >= 5 && !earnedUnlockIds.contains("lucky_coin")) {
            earnedUnlockIds.add("lucky_coin");
        }

        // plague_survivor: win with Danjin's Curse active (never shatter heart)
        // The heart must have been absorbed (curse active) but never shattered
        if (currentRunVictory && danjinHeartAbsorbed && !danjinHeartShattered
                && !earnedUnlockIds.contains("plague_survivor")) {
            earnedUnlockIds.add("plague_survivor");
        }
    }

    /**
     * Returns the list of earned unlock IDs.
     */
    public List<String> getActiveUnlocks() {
        return Collections.unmodifiableList(earnedUnlockIds);
    }

    /**
     * Returns whether a specific unlock has been earned.
     */
    public boolean hasUnlock(String unlockId) {
        return earnedUnlockIds.contains(unlockId);
    }

    // Getters for stats display

    public int getTotalRuns() { return totalRuns; }
    public int getTotalVictories() { return totalVictories; }
    public int getHighestWave() { return highestWave; }
    public int getTotalEnemiesKilled() { return totalEnemiesKilled; }
    public int getTotalDamageDealt() { return totalDamageDealt; }
    public int getTotalDamageTaken() { return totalDamageTaken; }
    public int getFastestVictoryTurns() { return fastestVictoryTurns; }
    public int getCurrentRunTurns() { return currentRunTurns; }
    public boolean hasUsedHolyLightThisRun() { return usedHolyLightThisRun; }

    /**
     * Returns unlockable description for display.
     */
    public static String getUnlockName(String unlockId) {
        switch (unlockId) {
            case "veteran_blade": return "Veteran's Blade";
            case "swift_boots": return "Swift Boots";
            case "iron_constitution": return "Iron Constitution";
            case "blood_warrior": return "Blood Warrior";
            case "lucky_coin": return "Lucky Coin";
            case "plague_survivor": return "Plague Survivor";
            default: return unlockId;
        }
    }

    /**
     * Returns the condition description for an unlockable.
     */
    public static String getUnlockCondition(String unlockId) {
        switch (unlockId) {
            case "veteran_blade": return "Win your first run";
            case "swift_boots": return "Win in under 60 turns";
            case "iron_constitution": return "Kill 100 enemies total";
            case "blood_warrior": return "Beat Lich without Holy Light";
            case "lucky_coin": return "Complete 5 runs";
            case "plague_survivor": return "Win with curse (never shatter)";
            default: return "Unknown";
        }
    }

    /**
     * Returns the reward description for an unlockable.
     */
    public static String getUnlockReward(String unlockId) {
        switch (unlockId) {
            case "veteran_blade": return "+8 ATK weapon at start";
            case "swift_boots": return "+10% crit accessory at start";
            case "iron_constitution": return "+15 max HP at start";
            case "blood_warrior": return "+15 ATK, -20 max HP at start";
            case "lucky_coin": return "Chest spawn 65% (was 50%)";
            case "plague_survivor": return "Permanent REGEN 3 HP/turn";
            default: return "Unknown";
        }
    }

    /**
     * Returns all possible unlock IDs in order.
     */
    public static String[] getAllUnlockIds() {
        return new String[]{
            "veteran_blade", "swift_boots", "iron_constitution",
            "blood_warrior", "lucky_coin", "plague_survivor"
        };
    }

    /**
     * Serializes this MetaProgression to a JSON string.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"totalRuns\": ").append(totalRuns).append(",\n");
        sb.append("  \"totalVictories\": ").append(totalVictories).append(",\n");
        sb.append("  \"highestWave\": ").append(highestWave).append(",\n");
        sb.append("  \"totalEnemiesKilled\": ").append(totalEnemiesKilled).append(",\n");
        sb.append("  \"totalDamageDealt\": ").append(totalDamageDealt).append(",\n");
        sb.append("  \"totalDamageTaken\": ").append(totalDamageTaken).append(",\n");
        sb.append("  \"fastestVictoryTurns\": ").append(fastestVictoryTurns).append(",\n");
        sb.append("  \"earnedUnlockIds\": ").append(listToJson(earnedUnlockIds)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserializes a JSON string into a MetaProgression instance.
     */
    public static MetaProgression fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new MetaProgression();
        }

        MetaProgression meta = new MetaProgression();
        meta.totalRuns = parseIntField(json, "totalRuns");
        meta.totalVictories = parseIntField(json, "totalVictories");
        meta.highestWave = parseIntField(json, "highestWave");
        meta.totalEnemiesKilled = parseIntField(json, "totalEnemiesKilled");
        meta.totalDamageDealt = parseIntField(json, "totalDamageDealt");
        meta.totalDamageTaken = parseIntField(json, "totalDamageTaken");
        meta.fastestVictoryTurns = parseIntField(json, "fastestVictoryTurns");
        meta.earnedUnlockIds = parseStringList(json, "earnedUnlockIds");
        return meta;
    }

    // --- JSON Utility Methods ---

    private static String escapeJsonString(String value) {
        if (value == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private static String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(escapeJsonString(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static int parseIntField(String json, String field) {
        String key = "\"" + field + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) return 0;
        int colonIndex = json.indexOf(':', keyIndex + key.length());
        if (colonIndex < 0) return 0;

        int start = colonIndex + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;

        if (start == end) return 0;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static List<String> parseStringList(String json, String field) {
        List<String> result = new ArrayList<>();
        String key = "\"" + field + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) return result;
        int colonIndex = json.indexOf(':', keyIndex + key.length());
        if (colonIndex < 0) return result;

        int bracketStart = json.indexOf('[', colonIndex);
        if (bracketStart < 0) return result;
        int bracketEnd = json.indexOf(']', bracketStart);
        if (bracketEnd < 0) return result;

        String arrayContent = json.substring(bracketStart + 1, bracketEnd).trim();
        if (arrayContent.isEmpty()) return result;

        int i = 0;
        while (i < arrayContent.length()) {
            int quoteStart = arrayContent.indexOf('"', i);
            if (quoteStart < 0) break;

            StringBuilder sb = new StringBuilder();
            int j = quoteStart + 1;
            while (j < arrayContent.length()) {
                char c = arrayContent.charAt(j);
                if (c == '\\' && j + 1 < arrayContent.length()) {
                    char next = arrayContent.charAt(j + 1);
                    switch (next) {
                        case '"': sb.append('"'); j += 2; break;
                        case '\\': sb.append('\\'); j += 2; break;
                        case 'n': sb.append('\n'); j += 2; break;
                        case 'r': sb.append('\r'); j += 2; break;
                        case 't': sb.append('\t'); j += 2; break;
                        default: sb.append(c); j++; break;
                    }
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                    j++;
                }
            }
            result.add(sb.toString());
            i = j + 1;
        }

        return result;
    }
}
