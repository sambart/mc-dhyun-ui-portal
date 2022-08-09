package com.dhyun.portal.voice.client;

import com.dhyun.portal.intercompatibility.ClientCompatibilityManager;
import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.voice.server.Server;
import com.sun.jna.Platform;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import de.maxhenkel.voicechat.macos.PermissionCheck;
import de.maxhenkel.voicechat.macos.VersionCheck;
import de.maxhenkel.voicechat.macos.jna.avfoundation.AVAuthorizationStatus;
import com.dhyun.portal.net.NetManager;
import com.dhyun.portal.net.RequestSecretPacket;
import com.dhyun.portal.net.SecretPacket;
import io.netty.channel.local.LocalAddress;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ClientManager {

    @Nullable
    private ClientVoicechat client;
    private final ClientPlayerStateManager playerStateManager;
    private final ClientCategoryManager categoryManager;
    private final PTTKeyHandler pttKeyHandler;
    private final RenderEvents renderEvents;
    private final KeyEvents keyEvents;
    private final Minecraft minecraft;

    private ClientManager() {
        playerStateManager = new ClientPlayerStateManager();
        categoryManager = new ClientCategoryManager();
        pttKeyHandler = new PTTKeyHandler();
        renderEvents = new RenderEvents();
        keyEvents = new KeyEvents();
        minecraft = Minecraft.getInstance();

        ClientCompatibilityManager.INSTANCE.onJoinWorld(this::onJoinWorld);
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::onDisconnect);
        ClientCompatibilityManager.INSTANCE.onPublishServer(this::onPublishServer);

        ClientCompatibilityManager.INSTANCE.onVoiceChatConnected(connection -> {
            if (client != null) {
                client.onVoiceChatConnected(connection);
            }
        });
        ClientCompatibilityManager.INSTANCE.onVoiceChatDisconnected(() -> {
            if (client != null) {
                client.onVoiceChatDisconnected();
            }
        });

        CommonCompatibilityManager.INSTANCE.getNetManager().secretChannel.setClientListener((client, handler, packet) -> authenticate(packet));
    }

    private void authenticate(SecretPacket secretPacket) {
        if (client == null) {
            Teleport.LOGGER.error("Received secret without a client being present");
            return;
        }
        Teleport.LOGGER.info("Received secret");
        if (client.getConnection() != null) {
            ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
        }
        ClientPacketListener connection = minecraft.getConnection();
        if (connection != null) {
            try {
                SocketAddress socketAddress = ClientCompatibilityManager.INSTANCE.getSocketAddress(connection.getConnection());
                if (socketAddress instanceof InetSocketAddress address) {
                    client.connect(new InitializationData(address.getHostString(), secretPacket));
                } else if (socketAddress instanceof LocalAddress) {
                    client.connect(new InitializationData("127.0.0.1", secretPacket));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onJoinWorld() {
        if (client != null) {
            Teleport.LOGGER.info("Disconnecting from previous connection due to server change");
            onDisconnect();
        }
        Teleport.LOGGER.info("Sending secret request to the server");
        NetManager.sendToServer(new RequestSecretPacket(Teleport.COMPATIBILITY_VERSION));
        client = new ClientVoicechat();

        checkMicrophonePermissions();
    }

    public static void sendPlayerError(String translationKey, @Nullable Exception e) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.sendSystemMessage(
                ComponentUtils.wrapInSquareBrackets(Component.literal(CommonCompatibilityManager.INSTANCE.getModName()))
                        .withStyle(ChatFormatting.GREEN)
                        .append(" ")
                        .append(Component.translatable(translationKey).withStyle(ChatFormatting.RED))
                        .withStyle(style -> {
                            if (e != null) {
                                return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(e.getMessage()).withStyle(ChatFormatting.RED)));
                            }
                            return style;
                        })
        );
    }

    private void onDisconnect() {
        if (client != null) {
            client.close();
            client = null;
        }
        ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
    }

    private void onPublishServer(int port) {
        Server server = Teleport.SERVER.getServer();
        if (server == null) {
            return;
        }
        try {
            Teleport.LOGGER.info("Changing voice chat port to {}", port);
            server.changePort(port);
            ClientVoicechat client = ClientManager.getClient();
            if (client != null) {
                ClientVoicechatConnection connection = client.getConnection();
                if (connection != null) {
                    Teleport.LOGGER.info("Force disconnecting due to port change");
                    connection.disconnect();
                }
            }
            NetManager.sendToServer(new RequestSecretPacket(Teleport.COMPATIBILITY_VERSION));
        } catch (Exception e) {
            Teleport.LOGGER.error("Failed to change voice chat port: {}", e.getMessage());
        }
        Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("message.voicechat.server_port", server.getPort()));
    }

    public void checkMicrophonePermissions() {
        if (!TeleportClient.CLIENT_CONFIG.macosMicrophoneWorkaround.get()) {
            return;
        }
        if (Platform.isMac() && VersionCheck.isCompatible()) {
            AVAuthorizationStatus status = PermissionCheck.getMicrophonePermissions();
            if (!status.equals(AVAuthorizationStatus.AUTHORIZED)) {
                sendPlayerError("message.voicechat.macos_no_mic_permission", null);
                Teleport.LOGGER.warn("User hasn't granted microphone permissions: {}", status.name());
            }
        }
    }

    @Nullable
    public static ClientVoicechat getClient() {
        return instance().client;
    }

    public static ClientPlayerStateManager getPlayerStateManager() {
        return instance().playerStateManager;
    }

    public static ClientCategoryManager getCategoryManager() {
        return instance().categoryManager;
    }

    public static PTTKeyHandler getPttKeyHandler() {
        return instance().pttKeyHandler;
    }

    public static RenderEvents getRenderEvents() {
        return instance().renderEvents;
    }

    public KeyEvents getKeyEvents() {
        return keyEvents;
    }

    private static ClientManager instance;

    public static ClientManager instance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

}
