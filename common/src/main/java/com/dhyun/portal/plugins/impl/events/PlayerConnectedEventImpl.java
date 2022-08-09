package com.dhyun.portal.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import com.dhyun.portal.plugins.impl.VoicechatConnectionImpl;

public class PlayerConnectedEventImpl extends ServerEventImpl implements PlayerConnectedEvent {

    protected VoicechatConnectionImpl connection;

    public PlayerConnectedEventImpl(VoicechatConnectionImpl connection) {
        this.connection = connection;
    }

    @Override
    public VoicechatConnection getConnection() {
        return connection;
    }
}
