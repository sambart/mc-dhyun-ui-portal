package com.dhyun.portal.voice.client.speaker;

import com.dhyun.portal.TeleportClient;
import com.dhyun.portal.voice.client.SoundManager;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpeakerManager {

    public static Speaker createSpeaker(SoundManager soundManager, @Nullable UUID audioChannel) throws SpeakerException {
        ALSpeakerBase speaker = switch (TeleportClient.CLIENT_CONFIG.audioType.get()) {
            case NORMAL -> new ALSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, audioChannel);
            case REDUCED -> new FakeALSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, audioChannel);
            case OFF -> new MonoALSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, audioChannel);
        };
        speaker.open();
        return speaker;
    }

}
