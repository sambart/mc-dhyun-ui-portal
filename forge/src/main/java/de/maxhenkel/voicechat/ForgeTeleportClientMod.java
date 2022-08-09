package de.maxhenkel.voicechat;

import com.dhyun.portal.TeleportClient;
import com.sun.jna.Platform;
import de.maxhenkel.voicechat.config.ForgeClientConfig;
import com.dhyun.portal.gui.VoiceChatSettingsScreen;
import com.dhyun.portal.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeClientCompatibilityManager;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ForgeTeleportClientMod extends TeleportClient {

    public ForgeTeleportClientMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(((ForgeClientCompatibilityManager) ClientCompatibilityManager.INSTANCE)::onRegisterKeyBinds);
        CLIENT_CONFIG = ForgeTeleportMod.registerConfig(ModConfig.Type.CLIENT, ForgeClientConfig::new);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        initializeClient();
        MinecraftForge.EVENT_BUS.register(ClientCompatibilityManager.INSTANCE);
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
            return new VoiceChatSettingsScreen(parent);
        }));

        if (Platform.isMac() && !CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            CLIENT_CONFIG.javaMicrophoneImplementation.set(true).save();
        }
    }
}
