package com.dhyun.portal.voice.common;

import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import com.dhyun.portal.voice.client.ClientManager;
import com.dhyun.portal.voice.client.ClientVoicechat;
import com.dhyun.portal.voice.client.ClientVoicechatConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Utils {

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

    public static short[] bytesToShorts(byte[] bytes) {
        if (bytes.length % 2 != 0) {
            throw new IllegalArgumentException("Input bytes need to be divisible by 2");
        }
        short[] data = new short[bytes.length / 2];
        for (int i = 0; i < bytes.length; i += 2) {
            data[i / 2] = bytesToShort(bytes[i], bytes[i + 1]);
        }
        return data;
    }

    public static byte[] shortsToBytes(short[] shorts) {
        byte[] data = new byte[shorts.length * 2];
        for (int i = 0; i < shorts.length; i++) {
            byte[] split = shortToBytes(shorts[i]);
            data[i * 2] = split[0];
            data[i * 2 + 1] = split[1];
        }
        return data;
    }

    public static float percentageToDB(float percentage) {
        return (float) (10D * Math.log(percentage));
    }

    public static short bytesToShort(byte b1, byte b2) {
        return (short) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
    }

    public static byte[] shortToBytes(short s) {
        return new byte[]{(byte) (s & 0xFF), (byte) ((s >> 8) & 0xFF)};
    }

    public static short[] floatsToShorts(float[] floats) {
        short[] shorts = new short[floats.length];
        for (int i = 0; i < floats.length; i++) {
            shorts[i] = ((Float) floats[i]).shortValue();
        }
        return shorts;
    }

    public static float[] shortsToFloats(short[] shorts) {
        float[] floats = new float[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            floats[i] = ((Short) shorts[i]).floatValue();
        }
        return floats;
    }

    public static byte[] floatsToBytes(float[] floats) {
        byte[] bytes = new byte[floats.length * 2];
        for (int i = 0; i < floats.length; i++) {
            short x = ((Float) floats[i]).shortValue();
            bytes[i * 2] = (byte) (x & 0x00FF);
            bytes[i * 2 + 1] = (byte) ((x & 0xFF00) >> 8);
        }
        return bytes;
    }

    public static float[] bytesToFloats(byte[] bytes) {
        float[] floats = new float[bytes.length / 2];
        for (int i = 0; i < bytes.length / 2; i++) {
            if ((bytes[i * 2 + 1] & 0x80) != 0) {
                floats[i] = Short.MIN_VALUE + ((bytes[i * 2 + 1] & 0x7F) << 8) | (bytes[i * 2] & 0xFF);
            } else {
                floats[i] = ((bytes[i * 2 + 1] << 8) & 0xFF00) | (bytes[i * 2] & 0xFF);
            }
        }
        return floats;
    }

    public static float normalizeAngle(float angle) {
        angle = angle % 360F;
        if (angle <= -180F) {
            angle += 360F;
        } else if (angle > 180F) {
            angle -= 360F;
        }
        return angle;
    }

    public static float angle(Vec2 vec1, Vec2 vec2) {
        return (float) Math.toDegrees(Math.atan2(vec1.x * vec2.x + vec1.y * vec2.y, vec1.x * vec2.y - vec1.y * vec2.x));
    }

    private static double magnitude(Vec2 vec1) {
        return Math.sqrt(Math.pow(vec1.x, 2) + Math.pow(vec1.y, 2));
    }

    private static float multiply(Vec2 vec1, Vec2 vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y;
    }

    private static Vec2 rotate(Vec2 vec, float angle) {
        return new Vec2(vec.x * Mth.cos(angle) - vec.y * Mth.sin(angle), vec.x * Mth.sin(angle) + vec.y * Mth.cos(angle));
    }

    /**
     * Calculates the audio level of a signal with specific samples.
     *
     * @param samples the samples of the signal to calculate the audio level of
     * @param offset  the offset in samples in which the samples start
     * @param length  the length in bytes of the signal in samples starting at offset
     * @return the audio level of the specified signal in db
     */
    public static double calculateAudioLevel(short[] samples, int offset, int length) {
        double rms = 0D; // root mean square (RMS) amplitude

        for (int i = offset; i < length; i++) {
            double sample = (double) samples[i] / (double) Short.MAX_VALUE;
            rms += sample * sample;
        }

        int sampleCount = length / 2;

        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        double db;

        if (rms > 0D) {
            db = Math.min(Math.max(20D * Math.log10(rms), -127D), 0D);
        } else {
            db = -127D;
        }

        return db;
    }

    /**
     * Calculates the highest audio level in packs of 100
     *
     * @param samples the audio samples
     * @return the audio level in db
     */
    public static double getHighestAudioLevel(short[] samples) {
        double highest = -127D;
        for (int i = 0; i < samples.length; i += 100) {
            double level = Utils.calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));
            if (level > highest) {
                highest = level;
            }
        }
        return highest;
    }

    /**
     * Gets the offset of the highest audio level in packs of 100
     *
     * @param samples         the audio samples
     * @param activationLevel the activation threshold
     * @return the audio level in db
     */
    public static int getActivationOffset(short[] samples, double activationLevel) {
        int highestPos = -1;
        for (int i = 0; i < samples.length; i += 100) {
            double level = Utils.calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));
            if (level >= activationLevel) {
                highestPos = i;
            }
        }
        return highestPos;
    }

    /**
     * Converts a dB value to a percentage value (-127 - 0) - (0 - 1)
     *
     * @param db the decibel value
     * @return the percantage
     */
    public static double dbToPerc(double db) {
        return (db + 127D) / 127D;
    }

    /**
     * Converts a percentage to a dB value (0 - 1) - (-127 - 0)
     *
     * @param perc the percentage
     * @return the decibel value
     */
    public static double percToDb(double perc) {
        return (perc * 127D) - 127D;
    }

    @Nullable
    public static <T> T createSafe(Supplier<T> supplier, @Nullable Consumer<Throwable> onError) {
        AtomicReference<Throwable> exception = new AtomicReference<>();
        AtomicReference<T> obj = new AtomicReference<>();
        Thread t = new Thread(() -> {
            if (onError != null) {
                Thread.setDefaultUncaughtExceptionHandler((t1, e) -> {
                    exception.set(e);
                });
            }
            obj.set(supplier.get());
        });
        t.start();

        try {
            t.join(1000);
        } catch (InterruptedException e) {
            return null;
        }
        Throwable ex = exception.get();
        if (onError != null && ex != null) {
            onError.accept(ex);
        }
        return obj.get();
    }

    @Nullable
    public static <T> T createSafe(Supplier<T> supplier) {
        return createSafe(supplier, null);
    }

    /**
     * Gets the default voice chat distance
     *
     * @return 48 if the voice chat is not connected
     */
    public static float getDefaultDistance() {
        if (TeleportClient.CLIENT_CONFIG == null) {
            return Teleport.SERVER_CONFIG.voiceChatDistance.get().floatValue();
        }

        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return 48F;
        }
        ClientVoicechatConnection connection = client.getConnection();
        if (connection == null) {
            return 48F;
        }
        return (float) connection.getData().getVoiceChatDistance();
    }

}