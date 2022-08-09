package com.dhyun.portal.voice.server;

import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.permission.PermissionManager;
import com.dhyun.portal.plugins.PluginManager;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import com.dhyun.portal.net.NetManager;
import com.dhyun.portal.net.SecretPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerVoiceEvents {

    private final Map<UUID, Integer> clientCompatibilities;
    private Server server;

    public ServerVoiceEvents() {
        clientCompatibilities = new ConcurrentHashMap<>();
        PluginManager.instance().init();
        CommonCompatibilityManager.INSTANCE.onServerStarting(this::serverStarting);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedIn(this::playerLoggedIn);
        CommonCompatibilityManager.INSTANCE.onPlayerLoggedOut(this::playerLoggedOut);
        CommonCompatibilityManager.INSTANCE.onServerStopping(this::serverStopping);

        CommonCompatibilityManager.INSTANCE.getNetManager().requestSecretChannel.setServerListener((server, player, handler, packet) -> {
            Teleport.LOGGER.info("Received secret request of {} ({})", player.getDisplayName().getString(), packet.getCompatibilityVersion());
            clientCompatibilities.put(player.getUUID(), packet.getCompatibilityVersion());
            if (packet.getCompatibilityVersion() != Teleport.COMPATIBILITY_VERSION) {
                Teleport.LOGGER.warn("Connected client {} has incompatible voice chat version (server={}, client={})", player.getName().getString(), Teleport.COMPATIBILITY_VERSION, packet.getCompatibilityVersion());
                player.sendSystemMessage(getIncompatibleMessage(packet.getCompatibilityVersion()));
            } else {
                initializePlayerConnection(player);
            }
        });
    }

    public Component getIncompatibleMessage(int clientCompatibilityVersion) {
        if (clientCompatibilityVersion <= 6) {
            return Component.literal("Your voice chat version is not compatible with the servers version.\nPlease install version ")
                    .append(Component.literal(CommonCompatibilityManager.INSTANCE.getModVersion()).withStyle(ChatFormatting.BOLD))
                    .append(" of ")
                    .append(Component.literal(CommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD))
                    .append(".");
        } else {
            return Component.translatable("message.voicechat.incompatible_version",
                    Component.literal(CommonCompatibilityManager.INSTANCE.getModVersion()).withStyle(ChatFormatting.BOLD),
                    Component.literal(CommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD));
        }
    }

    public boolean isCompatible(ServerPlayer player) {
        return clientCompatibilities.getOrDefault(player.getUUID(), -1) == Teleport.COMPATIBILITY_VERSION;
    }

    public void serverStarting(MinecraftServer mcServer) {
        if (server != null) {
            server.close();
            server = null;
        }

        if (!(mcServer instanceof DedicatedServer) && TeleportClient.CLIENT_CONFIG != null && !TeleportClient.CLIENT_CONFIG.runLocalServer.get()) {
            Teleport.LOGGER.info("Disabling voice chat in singleplayer");
            return;
        }

        if (mcServer instanceof DedicatedServer) {
            if (!mcServer.usesAuthentication()) {
                Teleport.LOGGER.warn("Running in offline mode - Voice chat encryption is not secure!");
            }
        }

        try {
            server = new Server(mcServer);
            server.start();
            PluginManager.instance().onServerStarted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializePlayerConnection(ServerPlayer player) {
        if (server == null) {
            return;
        }
        CommonCompatibilityManager.INSTANCE.emitPlayerCompatibilityCheckSucceeded(player);

        if (!PermissionManager.INSTANCE.CONNECT_PERMISSION.hasPermission(player)) {
            Teleport.LOGGER.info("Player {} has no permission to connect to the voice chat", player.getDisplayName().getString());
            return;
        }

        UUID secret = server.getSecret(player.getUUID());
        NetManager.sendToClient(player, new SecretPacket(player, secret, server.getPort(), Teleport.SERVER_CONFIG));
        Teleport.LOGGER.info("Sent secret to {}", player.getDisplayName().getString());
    }

    public void playerLoggedIn(ServerPlayer serverPlayer) {
        if (!Teleport.SERVER_CONFIG.forceVoiceChat.get()) {
            return;
        }

        Timer timer = new Timer("%s-login-timer".formatted(serverPlayer.getGameProfile().getName()), true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                timer.purge();
                if (!serverPlayer.server.isRunning()) {
                    return;
                }
                if (!serverPlayer.connection.connection.isConnected()) {
                    return;
                }
                if (!isCompatible(serverPlayer)) {
                    serverPlayer.server.execute(() -> {
                        serverPlayer.connection.disconnect(
                                Component.literal("You need %s %s to play on this server".formatted(
                                        CommonCompatibilityManager.INSTANCE.getModName(),
                                        CommonCompatibilityManager.INSTANCE.getModVersion()
                                ))
                        );
                    });
                }
            }
        }, Teleport.SERVER_CONFIG.loginTimeout.get());
    }

    public void playerLoggedOut(ServerPlayer player) {
        clientCompatibilities.remove(player.getUUID());
        if (server == null) {
            return;
        }

        server.disconnectClient(player.getUUID());
        Teleport.LOGGER.info("Disconnecting client " + player.getDisplayName().getString());
    }

    @Nullable
    public Server getServer() {
        return server;
    }

    public void serverStopping(MinecraftServer mcServer) {
        if (server != null) {
            server.close();
            server = null;
        }
    }

}
