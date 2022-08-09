package com.dhyun.portal.plugins.impl;

import com.dhyun.portal.Teleport;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import com.dhyun.portal.voice.common.PlayerState;
import com.dhyun.portal.voice.server.Server;

import javax.annotation.Nullable;

public class VoicechatConnectionImpl implements VoicechatConnection {

    private final ServerPlayer player;
    private final net.minecraft.server.level.ServerPlayer serverPlayer;
    private final PlayerState state;
    @Nullable
    private final Group group;

    public VoicechatConnectionImpl(net.minecraft.server.level.ServerPlayer player, PlayerState state) {
        this.serverPlayer = player;
        this.player = new ServerPlayerImpl(player);
        this.state = state;
        this.group = GroupImpl.create(state);
    }

    @Nullable
    public static VoicechatConnectionImpl fromPlayer(net.minecraft.server.level.ServerPlayer player) {
        Server server = Teleport.SERVER.getServer();
        if (server == null) {
            return null;
        }
        PlayerState state = server.getPlayerStateManager().getState(player.getUUID());
        if (state == null) {
            return null;
        }
        return new VoicechatConnectionImpl(player, state);
    }

    @Nullable
    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public boolean isInGroup() {
        return group != null;
    }

    @Override
    public void setGroup(@Nullable Group group) {
        Server server = Teleport.SERVER.getServer();
        if (server == null) {
            return;
        }
        if (group == null) {
            server.getGroupManager().leaveGroup(serverPlayer);
            return;
        }
        if (group instanceof GroupImpl g) {
            com.dhyun.portal.voice.server.Group actualGroup = server.getGroupManager().getGroup(g.getGroup().getId());
            if (actualGroup == null) {
                server.getGroupManager().addGroup(g.getGroup(), serverPlayer);
                actualGroup = g.getGroup();
            }
            server.getGroupManager().joinGroup(actualGroup, serverPlayer, g.getGroup().getPassword());
        }
    }

    @Override
    public boolean isDisabled() {
        return state.isDisabled();
    }

    @Override
    public ServerPlayer getPlayer() {
        return player;
    }

}
