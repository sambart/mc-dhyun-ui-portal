package com.dhyun.portal.voice.server;

import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.net.NetManager;
import com.dhyun.portal.net.PlayerStatePacket;
import com.dhyun.portal.net.PlayerStatesPacket;
import com.dhyun.portal.voice.common.ClientGroup;
import com.dhyun.portal.voice.common.PlayerState;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager {

    private final ConcurrentHashMap<UUID, PlayerState> states;
    private final Server voicechatServer;

    public PlayerStateManager(Server voicechatServer) {
        this.voicechatServer = voicechatServer;
        this.states = new ConcurrentHashMap<>();
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedIn(this::onPlayerLoggedIn);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(this::onPlayerLoggedOut);
        CommonCompatibilityManager.INSTANCE.onServerVoiceChatConnected(this::onPlayerVoicechatConnect);
        CommonCompatibilityManager.INSTANCE.onServerVoiceChatDisconnected(this::onPlayerVoicechatDisconnect);
        CommonCompatibilityManager.INSTANCE.onPlayerCompatibilityCheckSucceeded(this::onPlayerCompatibilityCheckSucceeded);

        CommonCompatibilityManager.INSTANCE.getNetManager().updateStateChannel.setServerListener((server, player, handler, packet) -> {
            PlayerState state = states.get(player.getUUID());

            if (state == null) {
                state = defaultDisconnectedState(player);
            }

            state.setDisabled(packet.isDisabled());

            states.put(player.getUUID(), state);

            broadcastState(state);
            Teleport.logDebug("Got state of {}: {}", player.getDisplayName().getString(), state);
        });
    }

    private void broadcastState(PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        voicechatServer.getServer().getPlayerList().getPlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    private void onPlayerCompatibilityCheckSucceeded(ServerPlayer player) {
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        NetManager.sendToClient(player, packet);
        Teleport.logDebug("Sending initial states to {}", player.getDisplayName().getString());
    }

    private void onPlayerLoggedIn(ServerPlayer player) {
        PlayerState state = defaultDisconnectedState(player);
        states.put(player.getUUID(), state);
        broadcastState(state);
        Teleport.logDebug("Setting default state of {}: {}", player.getDisplayName().getString(), state);
    }

    private void onPlayerLoggedOut(ServerPlayer player) {
        states.remove(player.getUUID());
        broadcastState(new PlayerState(player.getUUID(), player.getGameProfile().getName(), false, true));
        Teleport.logDebug("Removing state of {}", player.getDisplayName().getString());
    }

    private void onPlayerVoicechatDisconnect(UUID uuid) {
        PlayerState state = states.get(uuid);
        if (state == null) {
            return;
        }

        state.setDisconnected(true);

        broadcastState(state);
        Teleport.logDebug("Set state of {} to disconnected: {}", uuid, state);
    }

    private void onPlayerVoicechatConnect(ServerPlayer player) {
        PlayerState state = states.get(player.getUUID());

        if (state == null) {
            state = defaultDisconnectedState(player);
        }

        state.setDisconnected(false);

        states.put(player.getUUID(), state);

        broadcastState(state);
        Teleport.logDebug("Set state of {} to connected: {}", player.getDisplayName().getString(), state);
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public static PlayerState defaultDisconnectedState(ServerPlayer player) {
        return new PlayerState(player.getUUID(), player.getGameProfile().getName(), false, true);
    }

    public void setGroup(ServerPlayer player, @Nullable ClientGroup group) {
        PlayerState state = states.get(player.getUUID());
        if (state == null) {
            state = PlayerStateManager.defaultDisconnectedState(player);
            Teleport.logDebug("Defaulting to default state for {}: {}", player.getDisplayName().getString(), state);
        }
        state.setGroup(group);
        states.put(player.getUUID(), state);
        broadcastState(state);
        Teleport.logDebug("Setting group of {}: {}", player.getDisplayName().getString(), state);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
