package view.audio;

import com.badlogic.gdx.audio.Sound;

/**
 * Converts note arrays (frequency + duration + wave type) into playable audio.
 * Provides standard note frequencies and sequence-to-samples conversion.
 */
public class NoteSequencer {

    // Standard note frequencies (Hz)
    public static final float C3 = 131f;
    public static final float D3 = 147f;
    public static final float E3 = 165f;
    public static final float F3 = 175f;
    public static final float G3 = 196f;
    public static final float A3 = 220f;
    public static final float B3 = 247f;

    public static final float C4 = 262f;
    public static final float Cs4 = 277f;
    public static final float D4 = 294f;
    public static final float Ds4 = 311f;
    public static final float E4 = 330f;
    public static final float F4 = 349f;
    public static final float Fs4 = 370f;
    public static final float G4 = 392f;
    public static final float Gs4 = 415f;
    public static final float A4 = 440f;
    public static final float As4 = 466f;
    public static final float B4 = 494f;

    public static final float C5 = 523f;
    public static final float D5 = 587f;
    public static final float E5 = 659f;
    public static final float F5 = 698f;
    public static final float G5 = 784f;
    public static final float A5 = 880f;
    public static final float B5 = 988f;

    public static final float C6 = 1047f;

    /** Represents a single note with frequency, duration, and waveform type. */
    public static class Note {
        public final float frequency;
        public final float duration;
        public final SoundGenerator.WaveType waveType;
        public final float volume;

        public Note(float frequency, float duration, SoundGenerator.WaveType waveType) {
            this(frequency, duration, waveType, 0.5f);
        }

        public Note(float frequency, float duration, SoundGenerator.WaveType waveType, float volume) {
            this.frequency = frequency;
            this.duration = duration;
            this.waveType = waveType;
            this.volume = volume;
        }

        /** Creates a rest (silence) of given duration. */
        public static Note rest(float duration) {
            return new Note(0f, duration, SoundGenerator.WaveType.SQUARE, 0f);
        }
    }

    /**
     * Converts a sequence of notes into concatenated PCM samples.
     * Each note is generated with its specified waveform and duration,
     * then all are joined end-to-end.
     *
     * @param notes array of notes to sequence
     * @return concatenated PCM samples
     */
    public static short[] sequenceToSamples(Note[] notes) {
        return sequenceToSamples(notes, SoundGenerator.SAMPLE_RATE);
    }

    /**
     * Converts a sequence of notes into concatenated PCM samples.
     * @param notes array of notes to sequence
     * @param sampleRate the sample rate to use
     * @return concatenated PCM samples
     */
    public static short[] sequenceToSamples(Note[] notes, int sampleRate) {
        int totalSamples = 0;
        for (Note note : notes) {
            totalSamples += (int) (sampleRate * note.duration);
        }

        short[] result = new short[totalSamples];
        int offset = 0;

        for (Note note : notes) {
            int noteSamples = (int) (sampleRate * note.duration);
            if (note.frequency <= 0 || note.volume <= 0) {
                // Rest - leave samples as 0
                offset += noteSamples;
                continue;
            }

            short[] notePcm;
            switch (note.waveType) {
                case SQUARE:
                    notePcm = SoundGenerator.generateSquareWave(note.frequency, note.duration, note.volume);
                    break;
                case SAWTOOTH:
                    notePcm = SoundGenerator.generateSawtoothWave(note.frequency, note.duration, note.volume);
                    break;
                case TRIANGLE:
                    notePcm = SoundGenerator.generateTriangleWave(note.frequency, note.duration, note.volume);
                    break;
                case NOISE:
                    notePcm = SoundGenerator.generateNoise(note.duration, note.volume);
                    break;
                default:
                    notePcm = new short[noteSamples];
            }

            // Apply a short envelope to avoid clicks between notes
            SoundGenerator.applyEnvelope(notePcm, 2f, 5f);

            int copyLength = Math.min(notePcm.length, totalSamples - offset);
            System.arraycopy(notePcm, 0, result, offset, copyLength);
            offset += noteSamples;
        }

        return result;
    }

    /**
     * Creates a libGDX Sound from a note sequence.
     * @param notes array of notes
     * @return libGDX Sound object
     */
    public static Sound createSound(Note[] notes) {
        short[] samples = sequenceToSamples(notes);
        return SoundGenerator.createSound(samples);
    }

    /**
     * Creates a loopable sound from a note sequence.
     * Applies envelope to prevent clicks at loop boundary.
     * @param notes array of notes
     * @return libGDX Sound object suitable for looping
     */
    public static Sound createLoopableSound(Note[] notes) {
        short[] samples = sequenceToSamples(notes);
        // Apply short fade at start and end for smooth looping
        SoundGenerator.applyEnvelope(samples, 10f, 10f);
        return SoundGenerator.createSound(samples);
    }
}
