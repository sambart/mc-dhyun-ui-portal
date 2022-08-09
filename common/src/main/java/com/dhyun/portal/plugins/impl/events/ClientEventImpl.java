package com.dhyun.portal.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.ClientEvent;
import com.dhyun.portal.plugins.impl.VoicechatClientApiImpl;

public class ClientEventImpl extends EventImpl implements ClientEvent {

    @Override
    public VoicechatClientApi getVoicechat() {
        return VoicechatClientApiImpl.instance();
    }
}
