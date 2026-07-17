package controller;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import model.Player;
import model.CharacterClass;
import model.SaveData;
import model.items.Item;
import model.items.ItemSlot;
import model.skills.Skill;
import model.status.StatusEffect;

/**
 * Handles file I/O for save data, meta-progression, and settings.
 * Save directory: ~/.plague_of_danjin/ using System.getProperty("user.home").
 * Uses Gdx.files.absolute() for file operations.
 */
public class SaveManager {
    private static final String SAVE_DIR_NAME = ".plague_of_danjin";
    private static final String SAVE_FILE = "save.json";
    private static final String META_FILE = "meta.json";
    private static final String SETTINGS_FILE = "settings.json";

    private final String saveDir;

    public SaveManager() {
        this.saveDir = System.getProperty("user.home") + "/" + SAVE_DIR_NAME + "/";
    }

    /**
     * Ensures the save directory exists.
     */
    private void ensureDirectory() {
        FileHandle dir = Gdx.files.absolute(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Saves the current run state from the CombatEngine.
     */
    public void saveRun(CombatEngine engine) {
        if (engine == null || engine.getPlayer() == null) return;

        SaveData data = createSaveData(engine);
        String json = data.toJson();

        ensureDirectory();
        FileHandle file = Gdx.files.absolute(saveDir + SAVE_FILE);
        file.writeString(json, false);
    }

    /**
     * Loads the saved run data.
     * Returns null if no save file exists or if parsing fails.
     */
    public SaveData loadRun() {
        FileHandle file = Gdx.files.absolute(saveDir + SAVE_FILE);
        if (!file.exists()) return null;

        try {
            String json = file.readString();
            return SaveData.fromJson(json);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Deletes the run save file.
     */
    public void deleteSave() {
        FileHandle file = Gdx.files.absolute(saveDir + SAVE_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Returns true if a save file exists.
     */
    public boolean hasSave() {
        FileHandle file = Gdx.files.absolute(saveDir + SAVE_FILE);
        return file.exists();
    }

    /**
     * Saves meta-progression data.
     */
    public void saveMetaProgression(MetaProgression meta) {
        if (meta == null) return;

        String json = meta.toJson();
        ensureDirectory();
        FileHandle file = Gdx.files.absolute(saveDir + META_FILE);
        file.writeString(json, false);
    }

    /**
     * Loads meta-progression data.
     * Returns a new MetaProgression if no file exists or parsing fails.
     */
    public MetaProgression loadMetaProgression() {
        FileHandle file = Gdx.files.absolute(saveDir + META_FILE);
        if (!file.exists()) return new MetaProgression();

        try {
            String json = file.readString();
            return MetaProgression.fromJson(json);
        } catch (Exception e) {
            return new MetaProgression();
        }
    }

    /**
     * Saves audio volume settings.
     *
     * @param masterVol master volume (0.0 - 1.0)
     * @param sfxVol SFX volume (0.0 - 1.0)
     * @param musicVol music volume (0.0 - 1.0)
     */
    public void saveSettings(float masterVol, float sfxVol, float musicVol) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"masterVolume\": ").append(masterVol).append(",\n");
        sb.append("  \"sfxVolume\": ").append(sfxVol).append(",\n");
        sb.append("  \"musicVolume\": ").append(musicVol).append("\n");
        sb.append("}");

        ensureDirectory();
        FileHandle file = Gdx.files.absolute(saveDir + SETTINGS_FILE);
        file.writeString(sb.toString(), false);
    }

    /**
     * Loads audio volume settings.
     * Returns float[3] = {masterVolume, sfxVolume, musicVolume}.
     * Defaults to {1.0, 1.0, 1.0} if no settings file exists.
     */
    public float[] loadSettings() {
        float[] defaults = {1.0f, 1.0f, 1.0f};
        FileHandle file = Gdx.files.absolute(saveDir + SETTINGS_FILE);
        if (!file.exists()) return defaults;

        try {
            String json = file.readString();
            float master = parseFloatField(json, "masterVolume", 1.0f);
            float sfx = parseFloatField(json, "sfxVolume", 1.0f);
            float music = parseFloatField(json, "musicVolume", 1.0f);
            return new float[]{master, sfx, music};
        } catch (Exception e) {
            return defaults;
        }
    }

    /**
     * Creates a SaveData instance from the current CombatEngine state.
     */
    private SaveData createSaveData(CombatEngine engine) {
        Player player = engine.getPlayer();
        SaveData data = new SaveData();

        data.setWaveNumber(engine.getCurrentWave());
        data.setPlayerName(player.getName());
        data.setHp(player.getHp());
        data.setMaxHp(player.getMaxHp());
        data.setAtk(player.getAttackPower());
        data.setDef(player.getDefense());
        data.setMp(player.getMana());
        data.setMaxMp(player.getMaxMana());
        data.setMana(player.getMana());

        // Equipped items
        List<String> itemNames = new ArrayList<>();
        Item weapon = player.getInventory().getEquipped(ItemSlot.WEAPON);
        itemNames.add(weapon != null ? weapon.getName() : "");
        Item armor = player.getInventory().getEquipped(ItemSlot.ARMOR);
        itemNames.add(armor != null ? armor.getName() : "");
        Item accessory = player.getInventory().getEquipped(ItemSlot.ACCESSORY);
        itemNames.add(accessory != null ? accessory.getName() : "");
        data.setEquippedItemNames(itemNames);

        // Unlocked skills
        List<String> skillIds = new ArrayList<>();
        for (Skill skill : player.getSkillTree().getUnlockedSkills()) {
            skillIds.add(skill.getId());
        }
        data.setUnlockedSkillIds(skillIds);

        // Active status effects (format: type:duration:potency)
        List<String> statusEffects = new ArrayList<>();
        for (StatusEffect effect : player.getStatusManager().getActiveEffects()) {
            statusEffects.add(effect.getType().name() + ":" + effect.getDuration() + ":" + effect.getPotency());
        }
        data.setActiveStatusEffects(statusEffects);

        // Run modifiers
        RunModifiers mods = engine.getRunModifiers();
        data.setAppliedRunModifiers(new ArrayList<>(mods.getAppliedModifiers()));
        data.setDanjinHeartAbsorbed(mods.isDanjinHeartAbsorbed());
        data.setDanjinHeartShattered(mods.isDanjinHeartShattered());
        data.setPermanentDamagePerTurn(mods.getPermanentDamagePerTurn());

        // Character class
        if (engine.getCharacterClass() != null) {
            data.setCharacterClass(engine.getCharacterClass().name());
        }

        return data;
    }

    /**
     * Parses a float field from JSON.
     */
    private static float parseFloatField(String json, String field, float defaultValue) {
        String key = "\"" + field + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) return defaultValue;
        int colonIndex = json.indexOf(':', keyIndex + key.length());
        if (colonIndex < 0) return defaultValue;

        int start = colonIndex + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end))
                || json.charAt(end) == '.' || json.charAt(end) == '-')) end++;

        if (start == end) return defaultValue;
        try {
            return Float.parseFloat(json.substring(start, end));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
