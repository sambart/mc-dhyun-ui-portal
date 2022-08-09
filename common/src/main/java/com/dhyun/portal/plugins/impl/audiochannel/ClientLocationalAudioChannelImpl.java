package com.dhyun.portal.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import com.dhyun.portal.voice.common.LocationSoundPacket;
import com.dhyun.portal.voice.common.SoundPacket;
import com.dhyun.portal.voice.common.Utils;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ClientLocationalAudioChannelImpl extends ClientAudioChannelImpl implements ClientLocationalAudioChannel {

    private Position position;
    private float distance;

    public ClientLocationalAudioChannelImpl(UUID id, Position position) {
        super(id);
        this.position = position;
        this.distance = Utils.getDefaultDistance();
    }

    @Override
    protected SoundPacket<?> createSoundPacket(short[] rawAudio) {
        return new LocationSoundPacket(id, rawAudio, new Vec3(position.getX(), position.getY(), position.getZ()), distance, category);
    }

    @Override
    public void setLocation(Position position) {
        this.position = position;
    }

    @Override
    public Position getLocation() {
        return position;
    }

    @Override
    public float getDistance() {
        return distance;
    }

    @Override
    public void setDistance(float distance) {
        this.distance = distance;
    }

}
