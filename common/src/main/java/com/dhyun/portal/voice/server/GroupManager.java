package com.dhyun.portal.voice.server;

import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.permission.PermissionManager;
import com.dhyun.portal.plugins.PluginManager;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.net.JoinedGroupPacket;
import com.dhyun.portal.net.NetManager;
import com.dhyun.portal.voice.common.ClientGroup;
import com.dhyun.portal.voice.common.PlayerState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GroupManager {

    private final Map<UUID, Group> groups;
    private final Server server;

    public GroupManager(Server server) {
        this.server = server;
        groups = new ConcurrentHashMap<>();
        CommonCompatibilityManager.INSTANCE.getNetManager().joinGroupChannel.setServerListener((srv, player, handler, packet) -> {
            if (!Teleport.SERVER_CONFIG.groupsEnabled.get()) {
                return;
            }
            if (!PermissionManager.INSTANCE.GROUPS_PERMISSION.hasPermission(player)) {
                player.displayClientMessage(Component.translatable("message.voicechat.no_group_permission"), true);
                return;
            }
            joinGroup(groups.get(packet.getGroup()), player, packet.getPassword());
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().createGroupChannel.setServerListener((srv, player, handler, packet) -> {
            if (!Teleport.SERVER_CONFIG.groupsEnabled.get()) {
                return;
            }
            if (!PermissionManager.INSTANCE.GROUPS_PERMISSION.hasPermission(player)) {
                player.displayClientMessage(Component.translatable("message.voicechat.no_group_permission"), true);
                return;
            }
            addGroup(new Group(UUID.randomUUID(), packet.getName(), packet.getPassword()), player);
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().leaveGroupChannel.setServerListener((srv, player, handler, packet) -> {
            leaveGroup(player);
        });
    }

    private PlayerStateManager getStates() {
        return server.getPlayerStateManager();
    }

    public void addGroup(Group group, ServerPlayer player) {
        if (PluginManager.instance().onCreateGroup(player, group)) {
            return;
        }
        groups.put(group.getId(), group);

        PlayerStateManager manager = getStates();
        manager.setGroup(player, group.toClientGroup());

        NetManager.sendToClient(player, new JoinedGroupPacket(group.toClientGroup(), false));
    }

    public void joinGroup(@Nullable Group group, ServerPlayer player, @Nullable String password) {
        if (PluginManager.instance().onJoinGroup(player, group)) {
            return;
        }
        if (group == null) {
            NetManager.sendToClient(player, new JoinedGroupPacket(null, false));
            return;
        }
        if (group.getPassword() != null) {
            if (!group.getPassword().equals(password)) {
                NetManager.sendToClient(player, new JoinedGroupPacket(null, true));
                return;
            }
        }

        PlayerStateManager manager = getStates();
        manager.setGroup(player, group.toClientGroup());

        NetManager.sendToClient(player, new JoinedGroupPacket(group.toClientGroup(), false));
    }

    public void leaveGroup(ServerPlayer player) {
        if (PluginManager.instance().onLeaveGroup(player)) {
            return;
        }

        PlayerStateManager manager = getStates();
        manager.setGroup(player, null);
        NetManager.sendToClient(player, new JoinedGroupPacket(null, false));

        cleanEmptyGroups();
    }

    public void cleanEmptyGroups() {
        PlayerStateManager manager = getStates();
        List<UUID> usedGroups = manager.getStates().stream().filter(PlayerState::hasGroup).map(state -> state.getGroup().getId()).distinct().toList();
        List<UUID> groupsToRemove = groups.keySet().stream().filter(uuid -> !usedGroups.contains(uuid)).toList();
        for (UUID uuid : groupsToRemove) {
            groups.remove(uuid);
        }
    }

    @Nullable
    public Group getGroup(UUID groupID) {
        return groups.get(groupID);
    }

    @Nullable
    public Group getPlayerGroup(ServerPlayer player) {
        PlayerState state = server.getPlayerStateManager().getState(player.getUUID());
        if (state == null) {
            return null;
        }
        ClientGroup group = state.getGroup();
        if (group == null) {
            return null;
        }
        return getGroup(group.getId());
    }

}
