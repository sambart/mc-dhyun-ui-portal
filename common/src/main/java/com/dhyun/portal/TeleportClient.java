package com.dhyun.portal;

import com.dhyun.portal.config.ClientConfig;
import com.dhyun.portal.config.PlayerVolumeConfig;
import com.dhyun.portal.intercompatibility.ClientCompatibilityManager;
import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.profile.UsernameCache;
import com.dhyun.portal.resourcepacks.VoiceChatResourcePack;
import com.dhyun.portal.voice.client.ClientManager;
import com.dhyun.portal.voice.client.KeyEvents;
import com.dhyun.portal.voice.client.MacOSUtils;
import com.sun.jna.Platform;
import de.maxhenkel.voicechat.macos.PermissionCheck;
import de.maxhenkel.voicechat.macos.VersionCheck;
import de.maxhenkel.voicechat.macos.jna.avfoundation.AVAuthorizationStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public abstract class TeleportClient {

    public static ClientConfig CLIENT_CONFIG;
    public static PlayerVolumeConfig VOLUME_CONFIG;
    public static UsernameCache USERNAME_CACHE;

    public static VoiceChatResourcePack CLASSIC_ICONS;
    public static VoiceChatResourcePack WHITE_ICONS;
    public static VoiceChatResourcePack BLACK_ICONS;

    public TeleportClient() {
        KeyEvents.registerKeyBinds();
    }

    public void initializeClient() {
        fixVolumeConfig();
        VOLUME_CONFIG = new PlayerVolumeConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Teleport.MODID).resolve("voicechat-volumes.properties"));
        USERNAME_CACHE = new UsernameCache(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Teleport.MODID).resolve("username-cache.json").toFile());

        //Load instance
        ClientManager.instance();

        CLASSIC_ICONS = new VoiceChatResourcePack("Classic Icons", "classic_icons");
        WHITE_ICONS = new VoiceChatResourcePack("White Icons", "white_icons");
        BLACK_ICONS = new VoiceChatResourcePack("Black Icons", "black_icons");

        ClientCompatibilityManager.INSTANCE.addResourcePackSource(Minecraft.getInstance().getResourcePackRepository(), (Consumer<Pack> consumer, Pack.PackConstructor packConstructor) -> {
            consumer.accept(Pack.create(CLASSIC_ICONS.getName(), false, () -> CLASSIC_ICONS, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN));
            consumer.accept(Pack.create(WHITE_ICONS.getName(), false, () -> WHITE_ICONS, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN));
            consumer.accept(Pack.create(BLACK_ICONS.getName(), false, () -> BLACK_ICONS, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN));
        });
    }

    private void fixVolumeConfig() {
        Path oldLocation = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("voicechat-volumes.properties");
        Path newLocation = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Teleport.MODID).resolve("voicechat-volumes.properties");
        if (!newLocation.toFile().exists() && oldLocation.toFile().exists()) {
            try {
                Files.move(oldLocation, newLocation, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Teleport.LOGGER.error("Failed to move volumes config: {}", e.getMessage());
            }
        }
    }
}
