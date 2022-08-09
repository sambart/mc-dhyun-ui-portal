package com.dhyun.portal.plugins.impl.opus;

import de.maxhenkel.opus4j.Opus;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import com.dhyun.portal.config.ServerConfig;
import com.dhyun.portal.voice.client.SoundManager;
import com.dhyun.portal.voice.common.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpusManager {

    private static Boolean nativeOpusCompatible;

    public static boolean isNativeOpusCompatible() {
        if (nativeOpusCompatible == null) {
            Boolean isCompatible = Utils.createSafe(OpusManager::isOpusCompatible, e -> {
                Teleport.LOGGER.warn("Failed to load native Opus codec: {}", e.getMessage());
            });
            if (isCompatible == null) {
                Teleport.LOGGER.warn("Failed to load native Opus codec - Falling back to Java Opus implementation");
            }
            nativeOpusCompatible = isCompatible != null && isCompatible;
        }
        return nativeOpusCompatible;
    }

    public static OpusEncoder createEncoder(int sampleRate, int frameSize, int maxPayloadSize, int application) {
        if (useNatives() && isNativeOpusCompatible()) {
            NativeOpusEncoderImpl encoder = NativeOpusEncoderImpl.createEncoder(sampleRate, frameSize, maxPayloadSize, application);
            if (encoder != null) {
                return encoder;
            }
            nativeOpusCompatible = false;
            Teleport.LOGGER.warn("Failed to load native Opus encoder - Falling back to Java Opus implementation");
        }
        return new JavaOpusEncoderImpl(sampleRate, frameSize, maxPayloadSize, application);
    }

    public static OpusEncoder createEncoder(OpusEncoderMode mode) {
        int application = ServerConfig.Codec.VOIP.getOpusValue();
        if (mode != null) {
            application = switch (mode) {
                case VOIP -> ServerConfig.Codec.VOIP.getOpusValue();
                case AUDIO -> ServerConfig.Codec.AUDIO.getOpusValue();
                case RESTRICTED_LOWDELAY -> ServerConfig.Codec.RESTRICTED_LOWDELAY.getOpusValue();
            };
        }
        return createEncoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, 1024, application);
    }

    public static OpusDecoder createDecoder(int sampleRate, int frameSize, int maxPayloadSize) {
        if (useNatives() && isNativeOpusCompatible()) {
            NativeOpusDecoderImpl decoder = NativeOpusDecoderImpl.createDecoder(sampleRate, frameSize, maxPayloadSize);
            if (decoder != null) {
                return decoder;
            }
            nativeOpusCompatible = false;
            Teleport.LOGGER.warn("Failed to load native Opus decoder - Falling back to Java Opus implementation");
        }
        return new JavaOpusDecoderImpl(sampleRate, frameSize, maxPayloadSize);
    }

    public static OpusDecoder createDecoder() {
        return createDecoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, 1024);
    }

    public static Pattern VERSIONING_PATTERN = Pattern.compile("^[^\\d\\.]* ?(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+)){0,1}){0,1}.*$");

    private static boolean isOpusCompatible() {
        String versionString = Opus.INSTANCE.opus_get_version_string();

        Matcher matcher = VERSIONING_PATTERN.matcher(versionString);
        if (!matcher.matches()) {
            Teleport.LOGGER.warn("Failed to parse Opus version '{}'", versionString);
            return false;
        }
        String majorGroup = matcher.group("major");
        String minorGroup = matcher.group("minor");
        String patchGroup = matcher.group("patch");
        int actualMajor = majorGroup == null ? 0 : Integer.parseInt(majorGroup);
        int actualMinor = minorGroup == null ? 0 : Integer.parseInt(minorGroup);
        int actualPatch = patchGroup == null ? 0 : Integer.parseInt(patchGroup);

        if (!isMinimum(actualMajor, actualMinor, actualPatch, 1, 1, 0)) {
            Teleport.LOGGER.warn("Outdated Opus version detected: {}", versionString);
            return false;
        }

        Teleport.LOGGER.info("Using Opus version '{}'", versionString);
        return true;
    }

    private static boolean isMinimum(int actualMajor, int actualMinor, int actualPatch, int major, int minor, int patch) {
        if (major > actualMajor) {
            return false;
        } else if (major == actualMajor) {
            if (minor > actualMinor) {
                return false;
            } else if (minor == actualMinor) {
                return patch <= actualPatch;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private static boolean useNatives() {
        if (TeleportClient.CLIENT_CONFIG == null) {
            return true;
        }
        return TeleportClient.CLIENT_CONFIG.useNatives.get();
    }

}
