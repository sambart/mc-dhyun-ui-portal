package de.maxhenkel.voicechat;

import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.QuiltClientConfig;
import de.maxhenkel.voicechat.integration.ClothConfig;
import net.minecraft.client.Minecraft;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class QuiltTeleportClientMod extends TeleportClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(ModContainer mod) {
        CLIENT_CONFIG = ConfigBuilder.build(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Teleport.MODID).resolve("voicechat-client.properties"), true, QuiltClientConfig::new);
        initializeClient();
        ClothConfig.init();
    }
}
