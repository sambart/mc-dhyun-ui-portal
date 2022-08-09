package com.dhyun.portal.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import com.dhyun.portal.plugins.impl.VoicechatConnectionImpl;
import com.dhyun.portal.plugins.impl.VoicechatServerApiImpl;
import com.dhyun.portal.voice.common.GroupSoundPacket;
import com.dhyun.portal.voice.server.Server;

import java.util.UUID;

public class StaticAudioChannelImpl extends AudioChannelImpl implements StaticAudioChannel {

    protected VoicechatConnectionImpl connection;

    public StaticAudioChannelImpl(UUID channelId, Server server, VoicechatConnectionImpl connection) {
        super(channelId, server);
        this.connection = connection;
    }

    @Override
    public void send(byte[] opusData) {
        broadcast(new GroupSoundPacket(channelId, opusData, sequenceNumber.getAndIncrement(), category));
    }

    @Override
    public void send(MicrophonePacket packet) {
        send(packet.getOpusEncodedData());
    }

    @Override
    public void flush() {
        GroupSoundPacket packet = new GroupSoundPacket(channelId, new byte[0], sequenceNumber.getAndIncrement(), category);
        broadcast(packet);
    }

    private void broadcast(GroupSoundPacket packet) {
        VoicechatServerApiImpl.sendPacket(connection, packet);
    }

}
