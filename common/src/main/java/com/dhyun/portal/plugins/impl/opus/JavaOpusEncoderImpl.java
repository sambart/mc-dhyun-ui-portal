package com.dhyun.portal.plugins.impl.opus;

import de.maxhenkel.opus4j.Opus;
import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;

public class JavaOpusEncoderImpl implements de.maxhenkel.voicechat.api.opus.OpusEncoder {

    protected OpusEncoder opusEncoder;
    protected byte[] buffer;
    protected int sampleRate;
    protected int frameSize;
    protected int maxPayloadSize;
    protected int application;

    public JavaOpusEncoderImpl(int sampleRate, int frameSize, int maxPayloadSize, int application) {
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.maxPayloadSize = maxPayloadSize;
        this.application = application;
        this.buffer = new byte[maxPayloadSize];
        open();
    }

    private void open() {
        if (opusEncoder != null) {
            return;
        }
        try {
            opusEncoder = new OpusEncoder(sampleRate, 1, getApplication(application));
        } catch (Exception e) {
            throw new IllegalStateException("Opus encoder error " + e.getMessage());
        }
    }

    @Override
    public byte[] encode(short[] rawAudio) {
        if (isClosed()) {
            throw new IllegalStateException("Encoder is closed");
        }

        int result;
        try {
            result = opusEncoder.encode(rawAudio, 0, frameSize, buffer, 0, buffer.length);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode audio data: " + e.getMessage());
        }

        if (result < 0) {
            throw new RuntimeException("Failed to encode audio data");
        }

        byte[] audio = new byte[result];
        System.arraycopy(buffer, 0, audio, 0, result);
        return audio;
    }

    @Override
    public void resetState() {
        if (isClosed()) {
            throw new IllegalStateException("Encoder is closed");
        }
        opusEncoder.resetState();
    }

    @Override
    public boolean isClosed() {
        return opusEncoder == null;
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        opusEncoder = null;
    }

    public static OpusApplication getApplication(int application) {
        switch (application) {
            case Opus.OPUS_APPLICATION_VOIP:
            default:
                return OpusApplication.OPUS_APPLICATION_VOIP;
            case Opus.OPUS_APPLICATION_AUDIO:
                return OpusApplication.OPUS_APPLICATION_AUDIO;
            case Opus.OPUS_APPLICATION_RESTRICTED_LOWDELAY:
                return OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY;
        }
    }
}
