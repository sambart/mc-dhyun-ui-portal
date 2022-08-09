package com.dhyun.portal.plugins.impl.opus;

import com.sun.jna.ptr.PointerByReference;
import de.maxhenkel.opus4j.Opus;
import com.dhyun.portal.Teleport;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import com.dhyun.portal.voice.common.Utils;

import javax.annotation.Nullable;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class NativeOpusDecoderImpl implements OpusDecoder {

    protected PointerByReference opusDecoder;
    protected int sampleRate;
    protected int frameSize;
    protected int maxPayloadSize;

    private NativeOpusDecoderImpl(int sampleRate, int frameSize, int maxPayloadSize) {
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.maxPayloadSize = maxPayloadSize;
        open();
    }

    private void open() {
        if (opusDecoder != null) {
            return;
        }
        IntBuffer error = IntBuffer.allocate(1);
        opusDecoder = Opus.INSTANCE.opus_decoder_create(sampleRate, 1, error);
        if (error.get() != Opus.OPUS_OK && opusDecoder == null) {
            throw new IllegalStateException("Opus decoder error " + error.get());
        }
        Teleport.LOGGER.info("Initializing Opus decoder with sample rate " + sampleRate + " Hz, frame size " + frameSize + " bytes and max payload size " + maxPayloadSize + " bytes");
    }

    @Override
    public short[] decode(@Nullable byte[] data) {
        if (isClosed()) {
            throw new IllegalStateException("Decoder is closed");
        }
        int result;
        ShortBuffer decoded = ShortBuffer.allocate(4096);
        if (data == null || data.length == 0) {
            result = Opus.INSTANCE.opus_decode(opusDecoder, null, 0, decoded, frameSize, 0);
        } else {
            result = Opus.INSTANCE.opus_decode(opusDecoder, data, data.length, decoded, frameSize, 0);
        }

        if (result < 0) {
            throw new RuntimeException("Failed to decode audio data");
        }

        short[] audio = new short[result];
        decoded.get(audio);

        return audio;
    }

    @Override
    public boolean isClosed() {
        return opusDecoder == null;
    }

    @Override
    public void close() {
        if (opusDecoder == null) {
            return;
        }
        Opus.INSTANCE.opus_decoder_destroy(opusDecoder);
        opusDecoder = null;
    }

    @Override
    public void resetState() {
        if (isClosed()) {
            throw new IllegalStateException("Decoder is closed");
        }
        Opus.INSTANCE.opus_decoder_ctl(opusDecoder, Opus.INSTANCE.OPUS_RESET_STATE);
    }

    @Nullable
    public static NativeOpusDecoderImpl createDecoder(int sampleRate, int frameSize, int maxPayloadSize) {
        return Utils.createSafe(() -> new NativeOpusDecoderImpl(sampleRate, frameSize, maxPayloadSize), e -> {
            Teleport.LOGGER.warn("Failed to load native Opus decoder: {}", e.getMessage());
        });
    }

}
