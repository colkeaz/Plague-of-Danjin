package view.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Generates PCM audio data for 8-bit style waveforms at 22050 Hz mono 16-bit.
 * Converts short[] PCM data into libGDX Sound objects by writing WAV-format
 * data to a temporary file and loading it via Gdx.audio.newSound().
 */
public class SoundGenerator {
    public static final int SAMPLE_RATE = 22050;
    private static int tempFileCounter = 0;

    public enum WaveType {
        SQUARE,
        SAWTOOTH,
        TRIANGLE,
        NOISE
    }

    /**
     * Generates a square wave at the given frequency, duration, and volume.
     * @param frequency frequency in Hz
     * @param duration duration in seconds
     * @param volume amplitude 0.0 to 1.0
     * @return PCM samples as short array
     */
    public static short[] generateSquareWave(float frequency, float duration, float volume) {
        int numSamples = (int) (SAMPLE_RATE * duration);
        short[] samples = new short[numSamples];
        float period = SAMPLE_RATE / frequency;
        short amplitude = (short) (Short.MAX_VALUE * volume);

        for (int i = 0; i < numSamples; i++) {
            float phase = (i % period) / period;
            samples[i] = phase < 0.5f ? amplitude : (short) -amplitude;
        }
        return samples;
    }

    /**
     * Generates a sawtooth wave at the given frequency, duration, and volume.
     */
    public static short[] generateSawtoothWave(float frequency, float duration, float volume) {
        int numSamples = (int) (SAMPLE_RATE * duration);
        short[] samples = new short[numSamples];
        float period = SAMPLE_RATE / frequency;
        short amplitude = (short) (Short.MAX_VALUE * volume);

        for (int i = 0; i < numSamples; i++) {
            float phase = (i % period) / period;
            // Sawtooth: goes from -1 to 1 over one period
            float value = 2.0f * phase - 1.0f;
            samples[i] = (short) (value * amplitude);
        }
        return samples;
    }

    /**
     * Generates a triangle wave at the given frequency, duration, and volume.
     */
    public static short[] generateTriangleWave(float frequency, float duration, float volume) {
        int numSamples = (int) (SAMPLE_RATE * duration);
        short[] samples = new short[numSamples];
        float period = SAMPLE_RATE / frequency;
        short amplitude = (short) (Short.MAX_VALUE * volume);

        for (int i = 0; i < numSamples; i++) {
            float phase = (i % period) / period;
            // Triangle: rises 0->0.5, then falls 0.5->1.0
            float value;
            if (phase < 0.5f) {
                value = 4.0f * phase - 1.0f;
            } else {
                value = 3.0f - 4.0f * phase;
            }
            samples[i] = (short) (value * amplitude);
        }
        return samples;
    }

    /**
     * Generates white noise at the given duration and volume.
     */
    public static short[] generateNoise(float duration, float volume) {
        int numSamples = (int) (SAMPLE_RATE * duration);
        short[] samples = new short[numSamples];
        short amplitude = (short) (Short.MAX_VALUE * volume);

        for (int i = 0; i < numSamples; i++) {
            float value = (float) (Math.random() * 2.0 - 1.0);
            samples[i] = (short) (value * amplitude);
        }
        return samples;
    }

    /**
     * Generates a frequency sweep (pitch slide) from startFreq to endFreq.
     * @param startFreq starting frequency in Hz
     * @param endFreq ending frequency in Hz
     * @param duration duration in seconds
     * @param type waveform type to use
     * @param volume amplitude 0.0 to 1.0
     * @return PCM samples
     */
    public static short[] generateFrequencySweep(float startFreq, float endFreq, float duration,
                                                  WaveType type, float volume) {
        int numSamples = (int) (SAMPLE_RATE * duration);
        short[] samples = new short[numSamples];
        short amplitude = (short) (Short.MAX_VALUE * volume);

        float phase = 0f;
        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / numSamples;
            float freq = startFreq + (endFreq - startFreq) * t;
            float period = SAMPLE_RATE / freq;
            float phaseInPeriod = phase / period;

            float value;
            switch (type) {
                case SQUARE:
                    value = phaseInPeriod < 0.5f ? 1.0f : -1.0f;
                    break;
                case SAWTOOTH:
                    value = 2.0f * phaseInPeriod - 1.0f;
                    break;
                case TRIANGLE:
                    if (phaseInPeriod < 0.5f) {
                        value = 4.0f * phaseInPeriod - 1.0f;
                    } else {
                        value = 3.0f - 4.0f * phaseInPeriod;
                    }
                    break;
                case NOISE:
                    value = (float) (Math.random() * 2.0 - 1.0);
                    break;
                default:
                    value = 0f;
            }
            samples[i] = (short) (value * amplitude);

            phase += 1.0f;
            if (phase >= period) {
                phase -= period;
            }
        }
        return samples;
    }

    /**
     * Mixes two sample arrays together (additive, clamped).
     * The result length is the maximum of both arrays.
     */
    public static short[] mixSamples(short[] a, short[] b) {
        int length = Math.max(a.length, b.length);
        short[] result = new short[length];
        for (int i = 0; i < length; i++) {
            int sampleA = i < a.length ? a[i] : 0;
            int sampleB = i < b.length ? b[i] : 0;
            int mixed = sampleA + sampleB;
            // Clamp to short range
            if (mixed > Short.MAX_VALUE) mixed = Short.MAX_VALUE;
            if (mixed < Short.MIN_VALUE) mixed = Short.MIN_VALUE;
            result[i] = (short) mixed;
        }
        return result;
    }

    /**
     * Concatenates multiple sample arrays in sequence.
     */
    public static short[] concatenate(short[]... arrays) {
        int totalLength = 0;
        for (short[] arr : arrays) {
            totalLength += arr.length;
        }
        short[] result = new short[totalLength];
        int offset = 0;
        for (short[] arr : arrays) {
            System.arraycopy(arr, 0, result, offset, arr.length);
            offset += arr.length;
        }
        return result;
    }

    /**
     * Applies a volume envelope (fade in/out) to samples.
     * @param samples the PCM data to modify in-place
     * @param attackMs fade-in time in milliseconds
     * @param releaseMs fade-out time in milliseconds
     */
    public static void applyEnvelope(short[] samples, float attackMs, float releaseMs) {
        int attackSamples = (int) (SAMPLE_RATE * attackMs / 1000f);
        int releaseSamples = (int) (SAMPLE_RATE * releaseMs / 1000f);

        for (int i = 0; i < samples.length; i++) {
            float envelope = 1.0f;
            if (i < attackSamples) {
                envelope = (float) i / attackSamples;
            } else if (i >= samples.length - releaseSamples) {
                int fromEnd = samples.length - 1 - i;
                envelope = (float) fromEnd / releaseSamples;
            }
            samples[i] = (short) (samples[i] * envelope);
        }
    }

    /**
     * Converts PCM short[] data to a libGDX Sound object by writing a WAV file
     * to local storage and loading it.
     * @param samples PCM 16-bit signed data
     * @return libGDX Sound object, or null if creation fails
     */
    public static Sound createSound(short[] samples) {
        if (samples == null || samples.length == 0) {
            return null;
        }

        try {
            byte[] wavData = createWavBytes(samples);
            String fileName = "sfx_temp_" + (tempFileCounter++) + ".wav";
            FileHandle file = Gdx.files.local(fileName);
            file.writeBytes(wavData, false);
            Sound sound = Gdx.audio.newSound(file);
            // Delete temp file after loading
            file.delete();
            return sound;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates a complete WAV file byte array from PCM samples.
     * Format: 22050 Hz, mono, 16-bit signed little-endian.
     */
    private static byte[] createWavBytes(short[] samples) {
        int dataSize = samples.length * 2; // 16-bit = 2 bytes per sample
        int fileSize = 44 + dataSize;

        ByteBuffer buffer = ByteBuffer.allocate(fileSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // RIFF header
        buffer.put((byte) 'R');
        buffer.put((byte) 'I');
        buffer.put((byte) 'F');
        buffer.put((byte) 'F');
        buffer.putInt(fileSize - 8); // File size minus RIFF header (8 bytes)

        // WAVE format
        buffer.put((byte) 'W');
        buffer.put((byte) 'A');
        buffer.put((byte) 'V');
        buffer.put((byte) 'E');

        // fmt subchunk
        buffer.put((byte) 'f');
        buffer.put((byte) 'm');
        buffer.put((byte) 't');
        buffer.put((byte) ' ');
        buffer.putInt(16);          // Subchunk1Size (PCM = 16)
        buffer.putShort((short) 1); // AudioFormat (PCM = 1)
        buffer.putShort((short) 1); // NumChannels (Mono = 1)
        buffer.putInt(SAMPLE_RATE); // SampleRate
        buffer.putInt(SAMPLE_RATE * 2); // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
        buffer.putShort((short) 2); // BlockAlign (NumChannels * BitsPerSample/8)
        buffer.putShort((short) 16); // BitsPerSample

        // data subchunk
        buffer.put((byte) 'd');
        buffer.put((byte) 'a');
        buffer.put((byte) 't');
        buffer.put((byte) 'a');
        buffer.putInt(dataSize);    // Subchunk2Size

        // PCM data
        for (short sample : samples) {
            buffer.putShort(sample);
        }

        return buffer.array();
    }
}
