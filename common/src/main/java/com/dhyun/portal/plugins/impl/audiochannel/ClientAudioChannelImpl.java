package com.dhyun.portal.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.audiochannel.ClientAudioChannel;
import com.dhyun.portal.voice.client.ClientManager;
import com.dhyun.portal.voice.client.ClientVoicechat;
import com.dhyun.portal.voice.common.SoundPacket;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class ClientAudioChannelImpl implements ClientAudioChannel {

    protected UUID id;
    @Nullable
    protected String category;

    public ClientAudioChannelImpl(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    protected abstract SoundPacket<?> createSoundPacket(short[] rawAudio);

    @Override
    public void play(short[] rawAudio) {
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.processSoundPacket(createSoundPacket(rawAudio));
        }
    }

    @Nullable
    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public void setCategory(@Nullable String category) {
        this.category = category;
    }
}
