package com.dhyun.portal.voice.client;

import com.dhyun.portal.intercompatibility.ClientCompatibilityManager;
import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.plugins.PluginManager;
import com.dhyun.portal.plugins.impl.events.ClientVoicechatConnectionEventImpl;
import com.dhyun.portal.plugins.impl.events.MicrophoneMuteEventImpl;
import com.dhyun.portal.plugins.impl.events.VoicechatDisableEventImpl;
import com.dhyun.portal.voice.common.ClientGroup;
import com.dhyun.portal.voice.common.PlayerState;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import de.maxhenkel.voicechat.api.events.ClientVoicechatConnectionEvent;
import de.maxhenkel.voicechat.api.events.MicrophoneMuteEvent;
import de.maxhenkel.voicechat.api.events.VoicechatDisableEvent;
import com.dhyun.portal.gui.CreateGroupScreen;
import com.dhyun.portal.gui.EnterPasswordScreen;
import com.dhyun.portal.gui.group.GroupScreen;
import com.dhyun.portal.gui.group.JoinGroupScreen;
import com.dhyun.portal.gui.volume.AdjustVolumeList;
import com.dhyun.portal.net.NetManager;
import com.dhyun.portal.net.UpdateStatePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ClientPlayerStateManager {

    private boolean disconnected;
    @Nullable
    private ClientGroup group;

    private Map<UUID, PlayerState> states;

    public ClientPlayerStateManager() {
        this.disconnected = true;
        this.group = null;

        states = new HashMap<>();

        CommonCompatibilityManager.INSTANCE.getNetManager().playerStateChannel.setClientListener((client, handler, packet) -> {
            states.put(packet.getPlayerState().getUuid(), packet.getPlayerState());
            Teleport.logDebug("Got state for {}: {}", packet.getPlayerState().getName(), packet.getPlayerState());
            TeleportClient.USERNAME_CACHE.updateUsernameAndSave(packet.getPlayerState().getUuid(), packet.getPlayerState().getName());
            AdjustVolumeList.update();
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().playerStatesChannel.setClientListener((client, handler, packet) -> {
            states = packet.getPlayerStates();
            Teleport.logDebug("Received {} states", states.size());
            for (PlayerState state : states.values()) {
                TeleportClient.USERNAME_CACHE.updateUsername(state.getUuid(), state.getName());
            }
            TeleportClient.USERNAME_CACHE.save();
            AdjustVolumeList.update();
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().joinedGroupChannel.setClientListener((client, handler, packet) -> {
            Screen screen = Minecraft.getInstance().screen;
            this.group = packet.getGroup();
            if (packet.isWrongPassword()) {
                if (screen instanceof JoinGroupScreen || screen instanceof CreateGroupScreen || screen instanceof EnterPasswordScreen) {
                    Minecraft.getInstance().setScreen(null);
                }
                client.player.displayClientMessage(Component.translatable("message.voicechat.wrong_password").withStyle(ChatFormatting.DARK_RED), true);
            } else if (group != null && screen instanceof JoinGroupScreen || screen instanceof CreateGroupScreen || screen instanceof EnterPasswordScreen) {
                Minecraft.getInstance().setScreen(new GroupScreen(group));
            }
        });
        ClientCompatibilityManager.INSTANCE.onVoiceChatConnected(this::onVoiceChatConnected);
        ClientCompatibilityManager.INSTANCE.onVoiceChatDisconnected(this::onVoiceChatDisconnected);
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::onDisconnect);
    }

    private void resetOwnState() {
        disconnected = true;
        group = null;
    }

    /**
     * Called when the voicechat client gets disconnected or the player logs out
     */
    public void onVoiceChatDisconnected() {
        disconnected = true;
        syncOwnState();
        PluginManager.instance().dispatchEvent(ClientVoicechatConnectionEvent.class, new ClientVoicechatConnectionEventImpl(false));
    }

    /**
     * Called when the voicechat client gets (re)connected
     */
    public void onVoiceChatConnected(ClientVoicechatConnection client) {
        disconnected = false;
        syncOwnState();
        PluginManager.instance().dispatchEvent(ClientVoicechatConnectionEvent.class, new ClientVoicechatConnectionEventImpl(true));
    }

    private void onDisconnect() {
        clearStates();
        resetOwnState();
    }

    public boolean isPlayerDisabled(Player player) {
        PlayerState playerState = states.get(player.getUUID());
        if (playerState == null) {
            return false;
        }

        return playerState.isDisabled();
    }

    public boolean isPlayerDisconnected(Player player) {
        PlayerState playerState = states.get(player.getUUID());
        if (playerState == null) {
            return TeleportClient.CLIENT_CONFIG.showFakePlayersDisconnected.get();
        }

        return playerState.isDisconnected();
    }

    public void syncOwnState() {
        NetManager.sendToServer(new UpdateStatePacket(isDisabled()));
        Teleport.logDebug("Sent own state to server: disabled={}", isDisabled());
    }

    public boolean isDisabled() {
        if (!canEnable()) {
            return true;
        }
        return TeleportClient.CLIENT_CONFIG.disabled.get();
    }

    public boolean canEnable() {
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return false;
        }
        return client.getSoundManager() != null;
    }

    public void setDisabled(boolean disabled) {
        TeleportClient.CLIENT_CONFIG.disabled.set(disabled).save();
        syncOwnState();
        PluginManager.instance().dispatchEvent(VoicechatDisableEvent.class, new VoicechatDisableEventImpl(disabled));
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public boolean isMuted() {
        return TeleportClient.CLIENT_CONFIG.muted.get();
    }

    public void setMuted(boolean muted) {
        TeleportClient.CLIENT_CONFIG.muted.set(muted).save();
        PluginManager.instance().dispatchEvent(MicrophoneMuteEvent.class, new MicrophoneMuteEventImpl(muted));
    }

    public boolean isInGroup() {
        return getGroup() != null;
    }

    public boolean isInGroup(Player player) {
        PlayerState state = states.get(player.getUUID());
        if (state == null) {
            return false;
        }
        return state.hasGroup();
    }

    @Nullable
    public ClientGroup getGroup(Player player) {
        PlayerState state = states.get(player.getUUID());
        if (state == null) {
            return null;
        }
        return state.getGroup();
    }

    @Nullable
    public ClientGroup getGroup() {
        return group;
    }

    public List<PlayerState> getPlayerStates(boolean includeSelf) {
        if (includeSelf) {
            return new ArrayList<>(states.values());
        } else {
            return states.values().stream().filter(playerState -> !playerState.getUuid().equals(getOwnID())).collect(Collectors.toList());
        }
    }

    public UUID getOwnID() {
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            ClientVoicechatConnection connection = client.getConnection();
            if (connection != null) {
                return connection.getData().getPlayerUUID();
            }
        }
        return Minecraft.getInstance().getUser().getGameProfile().getId();
    }

    @Nullable
    public PlayerState getState(UUID player) {
        return states.get(player);
    }

    public void clearStates() {
        states.clear();
    }
}
