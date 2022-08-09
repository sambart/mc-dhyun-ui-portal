package com.dhyun.portal.plugins.impl;

import com.dhyun.portal.plugins.impl.audiochannel.ClientEntityAudioChannelImpl;
import com.dhyun.portal.plugins.impl.audiochannel.ClientLocationalAudioChannelImpl;
import com.dhyun.portal.plugins.impl.audiochannel.ClientStaticAudioChannelImpl;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;
import com.dhyun.portal.voice.client.ClientManager;
import com.dhyun.portal.voice.common.ClientGroup;

import javax.annotation.Nullable;
import java.util.UUID;

public class VoicechatClientApiImpl extends VoicechatApiImpl implements VoicechatClientApi {

    private static final VoicechatClientApiImpl INSTANCE = new VoicechatClientApiImpl();

    private VoicechatClientApiImpl() {

    }

    public static VoicechatClientApiImpl instance() {
        return INSTANCE;
    }

    @Override
    public boolean isMuted() {
        return ClientManager.getPlayerStateManager().isMuted();
    }

    @Override
    public boolean isDisabled() {
        return ClientManager.getPlayerStateManager().isDisabled();
    }

    @Override
    public boolean isDisconnected() {
        return ClientManager.getPlayerStateManager().isDisconnected();
    }

    @Override
    @Nullable
    public Group getGroup() {
        ClientGroup group = ClientManager.getPlayerStateManager().getGroup();
        if (group == null) {
            return null;
        }
        return new ClientGroupImpl(group);
    }

    @Override
    public ClientEntityAudioChannel createEntityAudioChannel(UUID uuid) {
        return new ClientEntityAudioChannelImpl(uuid);
    }

    @Override
    public ClientLocationalAudioChannel createLocationalAudioChannel(UUID uuid, Position position) {
        return new ClientLocationalAudioChannelImpl(uuid, position);
    }

    @Override
    public ClientStaticAudioChannel createStaticAudioChannel(UUID uuid) {
        return new ClientStaticAudioChannelImpl(uuid);
    }

    @Override
    public void unregisterClientVolumeCategory(String categoryId) {
        ClientManager.getCategoryManager().removeCategory(categoryId);
    }

    @Override
    public void registerClientVolumeCategory(VolumeCategory category) {
        if (!(category instanceof VolumeCategoryImpl c)) {
            throw new IllegalArgumentException("VolumeCategory is not an instance of VolumeCategoryImpl");
        }
        ClientManager.getCategoryManager().addCategory(c);
    }
}
