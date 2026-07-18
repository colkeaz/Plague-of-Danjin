package view.audio;

import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

import model.events.GameEvent;
import model.events.GameEventListener;
import model.events.GameEventType;
import view.audio.NoteSequencer.Note;
import view.audio.SoundGenerator.WaveType;

/**
 * Manages all sound effects for the game. Implements GameEventListener to play
 * appropriate SFX in response to game events. All 18 sound effects are generated
 * procedurally on construction and cached as libGDX Sound objects.
 */
public class SFXManager implements GameEventListener {
    private final Map<String, Sound> sfxCache;
    private float masterVolume;
    private float sfxVolume;

    public SFXManager() {
        this.sfxCache = new HashMap<>();
        this.masterVolume = 1.0f;
        this.sfxVolume = 0.8f;
        generateAllSFX();
    }

    private void generateAllSFX() {
        // menu_select: short high-pitched square wave blip, 50ms
        sfxCache.put("menu_select", SoundGenerator.createSound(
            SoundGenerator.generateSquareWave(880f, 0.05f, 0.4f)
        ));

        // menu_navigate: softer triangle wave blip, 30ms
        sfxCache.put("menu_navigate", SoundGenerator.createSound(
            SoundGenerator.generateTriangleWave(660f, 0.03f, 0.3f)
        ));

        // sword_slash: noise burst + falling pitch, 100ms
        short[] slashNoise = SoundGenerator.generateNoise(0.05f, 0.5f);
        short[] slashSweep = SoundGenerator.generateFrequencySweep(600f, 200f, 0.1f, WaveType.SAWTOOTH, 0.4f);
        sfxCache.put("sword_slash", SoundGenerator.createSound(
            SoundGenerator.mixSamples(
                SoundGenerator.concatenate(slashNoise, new short[(int)(SoundGenerator.SAMPLE_RATE * 0.05f)]),
                slashSweep
            )
        ));

        // spell_fire: rising sawtooth + crackle noise, 200ms
        short[] fireRise = SoundGenerator.generateFrequencySweep(200f, 800f, 0.2f, WaveType.SAWTOOTH, 0.4f);
        short[] fireCrackle = SoundGenerator.generateNoise(0.2f, 0.25f);
        sfxCache.put("spell_fire", SoundGenerator.createSound(
            SoundGenerator.mixSamples(fireRise, fireCrackle)
        ));

        // spell_holy: pure high triangle wave chord, 300ms
        short[] holyA = SoundGenerator.generateTriangleWave(880f, 0.3f, 0.3f);
        short[] holyB = SoundGenerator.generateTriangleWave(1100f, 0.3f, 0.25f);
        short[] holyC = SoundGenerator.generateTriangleWave(1320f, 0.3f, 0.2f);
        short[] holyChord = SoundGenerator.mixSamples(SoundGenerator.mixSamples(holyA, holyB), holyC);
        SoundGenerator.applyEnvelope(holyChord, 10f, 50f);
        sfxCache.put("spell_holy", SoundGenerator.createSound(holyChord));

        // spell_dark: low descending sawtooth, 200ms
        short[] darkSweep = SoundGenerator.generateFrequencySweep(200f, 60f, 0.2f, WaveType.SAWTOOTH, 0.5f);
        SoundGenerator.applyEnvelope(darkSweep, 5f, 30f);
        sfxCache.put("spell_dark", SoundGenerator.createSound(darkSweep));

        // spell_poison: bubbling modulated square, 150ms
        short[] poisonBase = SoundGenerator.generateSquareWave(150f, 0.15f, 0.3f);
        short[] poisonMod = SoundGenerator.generateSquareWave(30f, 0.15f, 0.3f);
        sfxCache.put("spell_poison", SoundGenerator.createSound(
            SoundGenerator.mixSamples(poisonBase, poisonMod)
        ));

        // hit_damage: short impact noise, 50ms
        short[] hitNoise = SoundGenerator.generateNoise(0.05f, 0.6f);
        SoundGenerator.applyEnvelope(hitNoise, 1f, 20f);
        sfxCache.put("hit_damage", SoundGenerator.createSound(hitNoise));

        // critical_hit: longer impact + rising pitch, 150ms
        short[] critNoise = SoundGenerator.generateNoise(0.07f, 0.6f);
        short[] critRise = SoundGenerator.generateFrequencySweep(300f, 900f, 0.15f, WaveType.SQUARE, 0.4f);
        sfxCache.put("critical_hit", SoundGenerator.createSound(
            SoundGenerator.mixSamples(
                SoundGenerator.concatenate(critNoise, new short[(int)(SoundGenerator.SAMPLE_RATE * 0.08f)]),
                critRise
            )
        ));

        // enemy_death: descending sweep, 200ms
        short[] deathSweep = SoundGenerator.generateFrequencySweep(500f, 80f, 0.2f, WaveType.SQUARE, 0.4f);
        SoundGenerator.applyEnvelope(deathSweep, 5f, 40f);
        sfxCache.put("enemy_death", SoundGenerator.createSound(deathSweep));

        // chest_open: rising arpeggio, 300ms
        sfxCache.put("chest_open", NoteSequencer.createSound(new Note[] {
            new Note(NoteSequencer.C5, 0.075f, WaveType.TRIANGLE, 0.4f),
            new Note(NoteSequencer.E5, 0.075f, WaveType.TRIANGLE, 0.4f),
            new Note(NoteSequencer.G5, 0.075f, WaveType.TRIANGLE, 0.4f),
            new Note(NoteSequencer.C6, 0.075f, WaveType.TRIANGLE, 0.5f)
        }));

        // item_equip: two-note chime, 150ms
        sfxCache.put("item_equip", NoteSequencer.createSound(new Note[] {
            new Note(NoteSequencer.E5, 0.075f, WaveType.TRIANGLE, 0.4f),
            new Note(NoteSequencer.A5, 0.075f, WaveType.TRIANGLE, 0.5f)
        }));

        // qte_correct: quick ascending blip, 50ms
        short[] qteCorrectSweep = SoundGenerator.generateFrequencySweep(500f, 1000f, 0.05f, WaveType.SQUARE, 0.4f);
        sfxCache.put("qte_correct", SoundGenerator.createSound(qteCorrectSweep));

        // qte_wrong: low buzz, 100ms
        short[] qteWrongBuzz = SoundGenerator.generateSquareWave(80f, 0.1f, 0.5f);
        SoundGenerator.applyEnvelope(qteWrongBuzz, 2f, 20f);
        sfxCache.put("qte_wrong", SoundGenerator.createSound(qteWrongBuzz));

        // qte_success: 3 ascending notes fanfare, 500ms
        sfxCache.put("qte_success", NoteSequencer.createSound(new Note[] {
            new Note(NoteSequencer.C5, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.E5, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.G5, 0.2f, WaveType.SQUARE, 0.5f)
        }));

        // qte_failure: descending minor notes, 400ms
        sfxCache.put("qte_failure", NoteSequencer.createSound(new Note[] {
            new Note(NoteSequencer.E4, 0.1f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.Ds4, 0.1f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.D4, 0.1f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.Cs4, 0.1f, WaveType.SQUARE, 0.5f)
        }));

        // wave_complete: 4 ascending notes victory jingle, 600ms
        sfxCache.put("wave_complete", NoteSequencer.createSound(new Note[] {
            new Note(NoteSequencer.C4, 0.15f, WaveType.TRIANGLE, 0.4f),
            new Note(NoteSequencer.E4, 0.15f, WaveType.TRIANGLE, 0.4f),
            new Note(NoteSequencer.G4, 0.15f, WaveType.TRIANGLE, 0.5f),
            new Note(NoteSequencer.C5, 0.15f, WaveType.TRIANGLE, 0.6f)
        }));

        // player_death: sad descending sequence, 500ms
        sfxCache.put("player_death", NoteSequencer.createSound(new Note[] {
            new Note(NoteSequencer.E4, 0.125f, WaveType.TRIANGLE, 0.5f),
            new Note(NoteSequencer.D4, 0.125f, WaveType.TRIANGLE, 0.45f),
            new Note(NoteSequencer.C4, 0.125f, WaveType.TRIANGLE, 0.4f),
            new Note(NoteSequencer.B3, 0.125f, WaveType.TRIANGLE, 0.35f)
        }));
    }

    @Override
    public void onEvent(GameEvent event) {
        GameEventType type = event.getType();
        switch (type) {
            case PLAYER_BASIC_ATTACK:
                play("sword_slash");
                break;
            case DAMAGE_DEALT:
                play("hit_damage");
                break;
            case CRITICAL_HIT:
                play("critical_hit");
                break;
            case SPELL_CAST:
                playSpellSound(event);
                break;
            case ENEMY_DEFEATED:
                play("enemy_death");
                break;
            case CHEST_FOUND:
            case CHEST_LEGENDARY:
            case CHEST_EPIC:
            case CHEST_RARE:
            case CHEST_COMMON:
                play("chest_open");
                break;
            case ITEM_EQUIPPED:
                play("item_equip");
                break;
            case QTE_SUCCESS:
                play("qte_success");
                break;
            case QTE_FAILURE:
                play("qte_failure");
                break;
            case WAVE_COMPLETE:
                play("wave_complete");
                break;
            case PLAYER_DEFEATED:
                play("player_death");
                break;
            default:
                break;
        }
    }

    private void playSpellSound(GameEvent event) {
        String element = event.getString("element");
        if (element == null) {
            play("spell_fire");
            return;
        }
        switch (element.toLowerCase()) {
            case "holy":
            case "light":
                play("spell_holy");
                break;
            case "dark":
            case "shadow":
                play("spell_dark");
                break;
            case "poison":
            case "nature":
                play("spell_poison");
                break;
            default:
                play("spell_fire");
                break;
        }
    }

    /**
     * Plays a named sound effect at the current volume.
     * @param name the SFX name
     */
    public void play(String name) {
        Sound sound = sfxCache.get(name);
        if (sound != null) {
            float volume = masterVolume * sfxVolume;
            sound.play(volume);
        }
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float masterVolume) {
        this.masterVolume = Math.max(0f, Math.min(1f, masterVolume));
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, sfxVolume));
    }

    /**
     * Disposes all cached Sound objects.
     */
    public void dispose() {
        for (Sound sound : sfxCache.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }
        sfxCache.clear();
    }
}
