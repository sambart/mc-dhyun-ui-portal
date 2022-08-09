package com.dhyun.portal.voice.client.speaker;

import com.dhyun.portal.voice.client.SoundManager;
import org.lwjgl.openal.AL11;

import javax.annotation.Nullable;
import java.util.UUID;

public class ALSpeaker extends ALSpeakerBase {

    public ALSpeaker(SoundManager soundManager, int sampleRate, int bufferSize, @Nullable UUID audioChannelId) {
        super(soundManager, sampleRate, bufferSize, audioChannelId);
    }

    @Override
    protected int getFormat() {
        return AL11.AL_FORMAT_MONO16;
    }

}
