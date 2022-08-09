package com.dhyun.portal.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;
import com.dhyun.portal.voice.common.GroupSoundPacket;
import com.dhyun.portal.voice.common.SoundPacket;

import java.util.UUID;

public class ClientStaticAudioChannelImpl extends ClientAudioChannelImpl implements ClientStaticAudioChannel {

    public ClientStaticAudioChannelImpl(UUID id) {
        super(id);
    }

    @Override
    protected SoundPacket<?> createSoundPacket(short[] rawAudio) {
        return new GroupSoundPacket(id, rawAudio, category);
    }

}
