package com.dhyun.portal.voice.client.speaker;

import com.dhyun.portal.voice.client.SoundManager;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL11;

import javax.annotation.Nullable;
import java.util.UUID;

public class MonoALSpeaker extends ALSpeakerBase {

    public MonoALSpeaker(SoundManager soundManager, int sampleRate, int bufferSize, @Nullable UUID audioChannelId) {
        super(soundManager, sampleRate, bufferSize, audioChannelId);
    }

    @Override
    public void play(short[] data, float volume, @Nullable Vec3 position, @Nullable String category, float distance) {
        super.play(data, volume, null, category, distance);
    }

    @Override
    protected int getFormat() {
        return AL11.AL_FORMAT_MONO16;
    }

}
