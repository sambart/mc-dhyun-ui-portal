package com.dhyun.portal.plugins.impl;

import com.dhyun.portal.Teleport;
import de.maxhenkel.voicechat.api.Group;
import com.dhyun.portal.voice.common.ClientGroup;
import com.dhyun.portal.voice.common.PlayerState;
import com.dhyun.portal.voice.server.Server;

import javax.annotation.Nullable;
import java.util.UUID;

public class GroupImpl implements Group {

    private final com.dhyun.portal.voice.server.Group group;

    public GroupImpl(com.dhyun.portal.voice.server.Group group) {
        this.group = group;
    }

    @Override
    public String getName() {
        return group.getName();
    }

    @Override
    public boolean hasPassword() {
        return group.getPassword() != null;
    }

    @Override
    public UUID getId() {
        return group.getId();
    }

    public com.dhyun.portal.voice.server.Group getGroup() {
        return group;
    }

    @Nullable
    public static GroupImpl create(PlayerState state) {
        ClientGroup group = state.getGroup();
        Server server = Teleport.SERVER.getServer();
        if (server != null && group != null) {
            com.dhyun.portal.voice.server.Group g = server.getGroupManager().getGroup(group.getId());
            if (g != null) {
                return new GroupImpl(g);
            }
        }
        return null;
    }

}
