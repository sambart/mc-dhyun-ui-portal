package com.dhyun.portal.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.ServerEvent;
import com.dhyun.portal.plugins.impl.VoicechatServerApiImpl;

public class ServerEventImpl extends EventImpl implements ServerEvent {

    @Override
    public VoicechatServerApi getVoicechat() {
        return VoicechatServerApiImpl.instance();
    }

}
