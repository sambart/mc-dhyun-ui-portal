package de.maxhenkel.voicechat;

import com.dhyun.portal.Teleport;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.FabricServerConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FabricTeleportMod extends Teleport implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER_CONFIG = ConfigBuilder.build(server.getServerDirectory().toPath().resolve("config").resolve(MODID).resolve("voicechat-server.properties"), true, FabricServerConfig::new);
        });

        initialize();
    }

}
