package com.dhyun.portal.plugins.impl;

import de.maxhenkel.voicechat.api.Group;
import com.dhyun.portal.voice.common.ClientGroup;

import java.util.UUID;

public class ClientGroupImpl implements Group {

    private final ClientGroup group;

    public ClientGroupImpl(ClientGroup group) {
        this.group = group;
    }

    @Override
    public String getName() {
        return group.getName();
    }

    @Override
    public boolean hasPassword() {
        return group.hasPassword();
    }

    @Override
    public UUID getId() {
        return group.getId();
    }

    public ClientGroup getGroup() {
        return group;
    }

}
