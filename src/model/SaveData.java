package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class representing serializable run state.
 * Handles serialization/deserialization to/from JSON using manual StringBuilder construction.
 * No external JSON libraries (Gson/Jackson) are used.
 */
public class SaveData {
    private int waveNumber;
    private String playerName;
    private int hp;
    private int maxHp;
    private int atk;
    private int def;
    private int mp;
    private int maxMp;
    private int mana;
    private List<String> equippedItemNames;
    private List<String> unlockedSkillIds;
    private List<String> activeStatusEffects;
    private List<String> appliedRunModifiers;
    private boolean danjinHeartAbsorbed;
    private boolean danjinHeartShattered;
    private int permanentDamagePerTurn;

    public SaveData() {
        this.equippedItemNames = new ArrayList<>();
        this.unlockedSkillIds = new ArrayList<>();
        this.activeStatusEffects = new ArrayList<>();
        this.appliedRunModifiers = new ArrayList<>();
    }

    // Getters and Setters

    public int getWaveNumber() { return waveNumber; }
    public void setWaveNumber(int waveNumber) { this.waveNumber = waveNumber; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }

    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }

    public int getMp() { return mp; }
    public void setMp(int mp) { this.mp = mp; }

    public int getMaxMp() { return maxMp; }
    public void setMaxMp(int maxMp) { this.maxMp = maxMp; }

    public int getMana() { return mana; }
    public void setMana(int mana) { this.mana = mana; }

    public List<String> getEquippedItemNames() { return equippedItemNames; }
    public void setEquippedItemNames(List<String> equippedItemNames) { this.equippedItemNames = equippedItemNames; }

    public List<String> getUnlockedSkillIds() { return unlockedSkillIds; }
    public void setUnlockedSkillIds(List<String> unlockedSkillIds) { this.unlockedSkillIds = unlockedSkillIds; }

    public List<String> getActiveStatusEffects() { return activeStatusEffects; }
    public void setActiveStatusEffects(List<String> activeStatusEffects) { this.activeStatusEffects = activeStatusEffects; }

    public List<String> getAppliedRunModifiers() { return appliedRunModifiers; }
    public void setAppliedRunModifiers(List<String> appliedRunModifiers) { this.appliedRunModifiers = appliedRunModifiers; }

    public boolean isDanjinHeartAbsorbed() { return danjinHeartAbsorbed; }
    public void setDanjinHeartAbsorbed(boolean danjinHeartAbsorbed) { this.danjinHeartAbsorbed = danjinHeartAbsorbed; }

    public boolean isDanjinHeartShattered() { return danjinHeartShattered; }
    public void setDanjinHeartShattered(boolean danjinHeartShattered) { this.danjinHeartShattered = danjinHeartShattered; }

    public int getPermanentDamagePerTurn() { return permanentDamagePerTurn; }
    public void setPermanentDamagePerTurn(int permanentDamagePerTurn) { this.permanentDamagePerTurn = permanentDamagePerTurn; }

    /**
     * Serializes this SaveData to a JSON string using manual StringBuilder construction.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"waveNumber\": ").append(waveNumber).append(",\n");
        sb.append("  \"playerName\": ").append(escapeJsonString(playerName)).append(",\n");
        sb.append("  \"hp\": ").append(hp).append(",\n");
        sb.append("  \"maxHp\": ").append(maxHp).append(",\n");
        sb.append("  \"atk\": ").append(atk).append(",\n");
        sb.append("  \"def\": ").append(def).append(",\n");
        sb.append("  \"mp\": ").append(mp).append(",\n");
        sb.append("  \"maxMp\": ").append(maxMp).append(",\n");
        sb.append("  \"mana\": ").append(mana).append(",\n");
        sb.append("  \"equippedItemNames\": ").append(listToJson(equippedItemNames)).append(",\n");
        sb.append("  \"unlockedSkillIds\": ").append(listToJson(unlockedSkillIds)).append(",\n");
        sb.append("  \"activeStatusEffects\": ").append(listToJson(activeStatusEffects)).append(",\n");
        sb.append("  \"appliedRunModifiers\": ").append(listToJson(appliedRunModifiers)).append(",\n");
        sb.append("  \"danjinHeartAbsorbed\": ").append(danjinHeartAbsorbed).append(",\n");
        sb.append("  \"danjinHeartShattered\": ").append(danjinHeartShattered).append(",\n");
        sb.append("  \"permanentDamagePerTurn\": ").append(permanentDamagePerTurn).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserializes a JSON string into a SaveData instance.
     * Uses manual parsing with no external libraries.
     */
    public static SaveData fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        SaveData data = new SaveData();
        data.waveNumber = parseIntField(json, "waveNumber");
        data.playerName = parseStringField(json, "playerName");
        data.hp = parseIntField(json, "hp");
        data.maxHp = parseIntField(json, "maxHp");
        data.atk = parseIntField(json, "atk");
        data.def = parseIntField(json, "def");
        data.mp = parseIntField(json, "mp");
        data.maxMp = parseIntField(json, "maxMp");
        data.mana = parseIntField(json, "mana");
        data.equippedItemNames = parseStringList(json, "equippedItemNames");
        data.unlockedSkillIds = parseStringList(json, "unlockedSkillIds");
        data.activeStatusEffects = parseStringList(json, "activeStatusEffects");
        data.appliedRunModifiers = parseStringList(json, "appliedRunModifiers");
        data.danjinHeartAbsorbed = parseBooleanField(json, "danjinHeartAbsorbed");
        data.danjinHeartShattered = parseBooleanField(json, "danjinHeartShattered");
        data.permanentDamagePerTurn = parseIntField(json, "permanentDamagePerTurn");
        return data;
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

    private static boolean parseBooleanField(String json, String field) {
        String key = "\"" + field + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) return false;
        int colonIndex = json.indexOf(':', keyIndex + key.length());
        if (colonIndex < 0) return false;

        int start = colonIndex + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;

        return json.regionMatches(start, "true", 0, 4);
    }

    private static String parseStringField(String json, String field) {
        String key = "\"" + field + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) return null;
        int colonIndex = json.indexOf(':', keyIndex + key.length());
        if (colonIndex < 0) return null;

        int start = colonIndex + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;

        if (start < json.length() && json.regionMatches(start, "null", 0, 4)) {
            return null;
        }

        if (start >= json.length() || json.charAt(start) != '"') return null;
        start++; // skip opening quote

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"': sb.append('"'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case 'n': sb.append('\n'); i++; break;
                    case 'r': sb.append('\r'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    default: sb.append(c); break;
                }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
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

        // Parse individual quoted strings
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
