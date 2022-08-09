package de.maxhenkel.voicechat;

import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.FabricClientConfig;
import de.maxhenkel.voicechat.integration.ClothConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class FabricTeleportClientMod extends TeleportClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CLIENT_CONFIG = ConfigBuilder.build(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Teleport.MODID).resolve("voicechat-client.properties"), true, FabricClientConfig::new);
        initializeClient();
        ClothConfig.init();
    }

}
