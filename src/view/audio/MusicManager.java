package view.audio;

import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

import view.audio.NoteSequencer.Note;
import view.audio.SoundGenerator.WaveType;

/**
 * Manages background chiptune music loops. Generates 5 tracks procedurally.
 * Supports crossfading between tracks (0.5s fade out, fade in new track).
 * Uses libGDX Sound objects with looping playback.
 *
 * NOTE: libGDX's OpenAL backend (LWJGL3) buffers Sound objects fully in memory.
 * Some platforms cap Sound duration at approximately 10 seconds. The current tracks
 * are 3-5 seconds which is safe, but extending track duration beyond 10 seconds
 * may result in silent truncation on certain backends. If longer music is needed,
 * consider splitting into multiple Sound segments or switching to a streaming approach.
 */
public class MusicManager {
    private final Map<String, Sound> tracks;
    private float musicVolume;

    private String currentTrack;
    private String targetTrack;
    private long currentPlayId;
    private long targetPlayId;

    // Crossfade state
    private boolean crossfading;
    private float crossfadeTimer;
    private static final float CROSSFADE_DURATION = 0.5f;
    private float currentFadeVolume;
    private float targetFadeVolume;

    public MusicManager() {
        this.tracks = new HashMap<>();
        this.musicVolume = 0.5f;
        this.currentTrack = null;
        this.targetTrack = null;
        this.currentPlayId = -1;
        this.targetPlayId = -1;
        this.crossfading = false;
        this.crossfadeTimer = 0f;
        this.currentFadeVolume = 1f;
        this.targetFadeVolume = 0f;
        generateAllTracks();
    }

    private void generateAllTracks() {
        // dungeon_theme: calm, ominous 8-bar loop (square + triangle)
        tracks.put("dungeon_theme", createDungeonTheme());

        // combat_theme: mid-tempo battle loop
        tracks.put("combat_theme", createCombatTheme());

        // boss_theme: intense, faster tempo
        tracks.put("boss_theme", createBossTheme());

        // event_room_theme: mysterious, slower, minor key
        tracks.put("event_room_theme", createEventRoomTheme());

        // victory_theme: triumphant fanfare loop
        tracks.put("victory_theme", createVictoryTheme());
    }

    private Sound createDungeonTheme() {
        // Calm, ominous 8-bar loop using square and triangle waves
        Note[] melody = new Note[] {
            new Note(NoteSequencer.E3, 0.3f, WaveType.SQUARE, 0.3f),
            new Note(NoteSequencer.G3, 0.3f, WaveType.SQUARE, 0.25f),
            new Note(NoteSequencer.A3, 0.3f, WaveType.TRIANGLE, 0.3f),
            new Note(NoteSequencer.G3, 0.3f, WaveType.TRIANGLE, 0.25f),
            new Note(NoteSequencer.E3, 0.6f, WaveType.SQUARE, 0.3f),
            Note.rest(0.2f),
            new Note(NoteSequencer.D3, 0.3f, WaveType.TRIANGLE, 0.25f),
            new Note(NoteSequencer.E3, 0.3f, WaveType.TRIANGLE, 0.3f),
            new Note(NoteSequencer.C3, 0.6f, WaveType.SQUARE, 0.3f),
            Note.rest(0.2f),
            new Note(NoteSequencer.D3, 0.3f, WaveType.SQUARE, 0.25f),
            new Note(NoteSequencer.E3, 0.3f, WaveType.TRIANGLE, 0.3f),
            new Note(NoteSequencer.G3, 0.3f, WaveType.SQUARE, 0.25f),
            new Note(NoteSequencer.E3, 0.5f, WaveType.TRIANGLE, 0.3f),
            Note.rest(0.3f)
        };
        short[] samples = NoteSequencer.sequenceToSamples(melody);
        SoundGenerator.applyEnvelope(samples, 10f, 10f);
        return SoundGenerator.createSound(samples);
    }

    private Sound createCombatTheme() {
        // Mid-tempo energetic battle loop
        Note[] melody = new Note[] {
            new Note(NoteSequencer.A3, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.A3, 0.15f, WaveType.SQUARE, 0.35f),
            new Note(NoteSequencer.C4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.D4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.E4, 0.3f, WaveType.SQUARE, 0.45f),
            new Note(NoteSequencer.D4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.C4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.A3, 0.3f, WaveType.SQUARE, 0.4f),
            Note.rest(0.1f),
            new Note(NoteSequencer.G3, 0.15f, WaveType.TRIANGLE, 0.35f),
            new Note(NoteSequencer.A3, 0.15f, WaveType.TRIANGLE, 0.35f),
            new Note(NoteSequencer.C4, 0.3f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.A3, 0.15f, WaveType.SQUARE, 0.35f),
            new Note(NoteSequencer.G3, 0.3f, WaveType.TRIANGLE, 0.35f),
            new Note(NoteSequencer.E3, 0.3f, WaveType.SQUARE, 0.4f),
            Note.rest(0.15f),
            new Note(NoteSequencer.A3, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.C4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.D4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.E4, 0.15f, WaveType.SQUARE, 0.45f),
            new Note(NoteSequencer.G4, 0.3f, WaveType.SQUARE, 0.5f),
            new Note(NoteSequencer.E4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.D4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.A3, 0.4f, WaveType.SQUARE, 0.4f),
            Note.rest(0.1f)
        };
        short[] samples = NoteSequencer.sequenceToSamples(melody);
        SoundGenerator.applyEnvelope(samples, 10f, 10f);
        return SoundGenerator.createSound(samples);
    }

    private Sound createBossTheme() {
        // Intense, faster tempo
        Note[] melody = new Note[] {
            new Note(NoteSequencer.E3, 0.1f, WaveType.SQUARE, 0.5f),
            new Note(NoteSequencer.E3, 0.1f, WaveType.SQUARE, 0.45f),
            new Note(NoteSequencer.E4, 0.1f, WaveType.SQUARE, 0.5f),
            new Note(NoteSequencer.E3, 0.1f, WaveType.SQUARE, 0.45f),
            new Note(NoteSequencer.G3, 0.1f, WaveType.SAWTOOTH, 0.45f),
            new Note(NoteSequencer.A3, 0.1f, WaveType.SAWTOOTH, 0.5f),
            new Note(NoteSequencer.B3, 0.2f, WaveType.SQUARE, 0.5f),
            new Note(NoteSequencer.A3, 0.1f, WaveType.SQUARE, 0.45f),
            new Note(NoteSequencer.G3, 0.1f, WaveType.SQUARE, 0.45f),
            new Note(NoteSequencer.E3, 0.2f, WaveType.SAWTOOTH, 0.5f),
            Note.rest(0.05f),
            new Note(NoteSequencer.D3, 0.1f, WaveType.SQUARE, 0.45f),
            new Note(NoteSequencer.E3, 0.1f, WaveType.SQUARE, 0.5f),
            new Note(NoteSequencer.G3, 0.1f, WaveType.SAWTOOTH, 0.5f),
            new Note(NoteSequencer.A3, 0.2f, WaveType.SQUARE, 0.5f),
            new Note(NoteSequencer.B3, 0.1f, WaveType.SQUARE, 0.5f),
            new Note(NoteSequencer.C4, 0.2f, WaveType.SAWTOOTH, 0.55f),
            new Note(NoteSequencer.B3, 0.1f, WaveType.SQUARE, 0.5f),
            new Note(NoteSequencer.A3, 0.1f, WaveType.SQUARE, 0.45f),
            new Note(NoteSequencer.E3, 0.3f, WaveType.SAWTOOTH, 0.5f),
            Note.rest(0.1f)
        };
        short[] samples = NoteSequencer.sequenceToSamples(melody);
        SoundGenerator.applyEnvelope(samples, 10f, 10f);
        return SoundGenerator.createSound(samples);
    }

    private Sound createEventRoomTheme() {
        // Mysterious, slower, minor key
        Note[] melody = new Note[] {
            new Note(NoteSequencer.D3, 0.4f, WaveType.TRIANGLE, 0.3f),
            new Note(NoteSequencer.F3, 0.4f, WaveType.TRIANGLE, 0.25f),
            new Note(NoteSequencer.E3, 0.4f, WaveType.TRIANGLE, 0.3f),
            Note.rest(0.2f),
            new Note(NoteSequencer.D3, 0.3f, WaveType.SQUARE, 0.25f),
            new Note(NoteSequencer.C3, 0.5f, WaveType.TRIANGLE, 0.3f),
            Note.rest(0.3f),
            new Note(NoteSequencer.E3, 0.4f, WaveType.TRIANGLE, 0.25f),
            new Note(NoteSequencer.D3, 0.4f, WaveType.TRIANGLE, 0.3f),
            new Note(NoteSequencer.C3, 0.3f, WaveType.SQUARE, 0.25f),
            new Note(NoteSequencer.D3, 0.6f, WaveType.TRIANGLE, 0.3f),
            Note.rest(0.3f)
        };
        short[] samples = NoteSequencer.sequenceToSamples(melody);
        SoundGenerator.applyEnvelope(samples, 10f, 10f);
        return SoundGenerator.createSound(samples);
    }

    private Sound createVictoryTheme() {
        // Triumphant fanfare loop
        Note[] melody = new Note[] {
            new Note(NoteSequencer.C4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.E4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.G4, 0.15f, WaveType.SQUARE, 0.45f),
            new Note(NoteSequencer.C5, 0.3f, WaveType.SQUARE, 0.5f),
            Note.rest(0.1f),
            new Note(NoteSequencer.G4, 0.15f, WaveType.TRIANGLE, 0.4f),
            new Note(NoteSequencer.A4, 0.15f, WaveType.TRIANGLE, 0.4f),
            new Note(NoteSequencer.B4, 0.15f, WaveType.TRIANGLE, 0.45f),
            new Note(NoteSequencer.C5, 0.4f, WaveType.SQUARE, 0.5f),
            Note.rest(0.1f),
            new Note(NoteSequencer.E4, 0.15f, WaveType.SQUARE, 0.4f),
            new Note(NoteSequencer.G4, 0.15f, WaveType.SQUARE, 0.45f),
            new Note(NoteSequencer.C5, 0.15f, WaveType.SQUARE, 0.5f),
            new Note(NoteSequencer.E5, 0.4f, WaveType.SQUARE, 0.55f),
            new Note(NoteSequencer.C5, 0.3f, WaveType.TRIANGLE, 0.45f),
            Note.rest(0.2f)
        };
        short[] samples = NoteSequencer.sequenceToSamples(melody);
        SoundGenerator.applyEnvelope(samples, 10f, 10f);
        return SoundGenerator.createSound(samples);
    }

    /**
     * Plays the named track, crossfading from the current track.
     * If the same track is already playing, does nothing.
     * If a crossfade is already targeting this track, does nothing.
     * @param trackName one of: dungeon_theme, combat_theme, boss_theme, event_room_theme, victory_theme
     */
    public void play(String trackName) {
        if (trackName == null) return;
        if (trackName.equals(currentTrack) && !crossfading) return;
        // Prevent spawning new loops if crossfade is already targeting this track
        if (crossfading && trackName.equals(targetTrack)) return;

        Sound track = tracks.get(trackName);
        if (track == null) return;

        if (currentTrack == null) {
            // No current track, just start playing
            currentTrack = trackName;
            currentPlayId = track.loop(musicVolume);
            currentFadeVolume = 1f;
        } else {
            // If we're already crossfading, cancel the in-progress crossfade first
            if (crossfading) {
                // Stop the old target that was fading in
                if (targetTrack != null && targetPlayId != -1) {
                    Sound oldTarget = tracks.get(targetTrack);
                    if (oldTarget != null) {
                        oldTarget.stop();
                    }
                }
            }
            // Start crossfade
            targetTrack = trackName;
            targetPlayId = track.loop(0f);
            targetFadeVolume = 0f;
            crossfading = true;
            crossfadeTimer = 0f;
        }
    }

    /**
     * Stops all music immediately.
     */
    public void stop() {
        if (currentTrack != null) {
            Sound current = tracks.get(currentTrack);
            if (current != null) {
                current.stop();
            }
            currentTrack = null;
            currentPlayId = -1;
        }
        if (targetTrack != null) {
            Sound target = tracks.get(targetTrack);
            if (target != null) {
                target.stop();
            }
            targetTrack = null;
            targetPlayId = -1;
        }
        crossfading = false;
    }

    /**
     * Updates the crossfade logic. Call every frame.
     * @param delta time in seconds since last frame
     */
    public void update(float delta) {
        if (!crossfading) return;

        crossfadeTimer += delta;
        float progress = Math.min(crossfadeTimer / CROSSFADE_DURATION, 1f);

        // Fade out current
        currentFadeVolume = 1f - progress;
        // Fade in target
        targetFadeVolume = progress;

        // Apply volumes
        if (currentTrack != null && currentPlayId != -1) {
            Sound current = tracks.get(currentTrack);
            if (current != null) {
                current.setVolume(currentPlayId, currentFadeVolume * musicVolume);
            }
        }
        if (targetTrack != null && targetPlayId != -1) {
            Sound target = tracks.get(targetTrack);
            if (target != null) {
                target.setVolume(targetPlayId, targetFadeVolume * musicVolume);
            }
        }

        // Crossfade complete
        if (progress >= 1f) {
            // Stop old track
            if (currentTrack != null) {
                Sound current = tracks.get(currentTrack);
                if (current != null) {
                    current.stop();
                }
            }
            // New track becomes current
            currentTrack = targetTrack;
            currentPlayId = targetPlayId;
            currentFadeVolume = 1f;
            targetTrack = null;
            targetPlayId = -1;
            crossfading = false;
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = Math.max(0f, Math.min(1f, musicVolume));
        // Update current playing track volume
        if (currentTrack != null && currentPlayId != -1) {
            Sound current = tracks.get(currentTrack);
            if (current != null) {
                current.setVolume(currentPlayId, currentFadeVolume * this.musicVolume);
            }
        }
    }

    /**
     * Returns the name of the currently playing track, or null if none.
     */
    public String getCurrentTrack() {
        return currentTrack;
    }

    /**
     * Disposes all generated music tracks.
     */
    public void dispose() {
        stop();
        for (Sound sound : tracks.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }
        tracks.clear();
    }
}
