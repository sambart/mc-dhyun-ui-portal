package de.maxhenkel.voicechat;

import com.dhyun.portal.Teleport;
import de.maxhenkel.voicechat.config.ForgeServerConfig;
import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.permission.PermissionManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Objects;
import java.util.function.Function;

@Mod(ForgeTeleportMod.MODID)
public class ForgeTeleportMod extends Teleport {

    public ForgeTeleportMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        SERVER_CONFIG = registerConfig(ModConfig.Type.SERVER, ForgeServerConfig::new);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ForgeTeleportClientMod::new);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        initialize();
        MinecraftForge.EVENT_BUS.register(CommonCompatibilityManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PermissionManager.INSTANCE);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> {
            return new IExtensionPoint.DisplayTest(() -> String.valueOf(Teleport.COMPATIBILITY_VERSION), (incoming, isNetwork) -> {
                return Objects.equals(incoming, String.valueOf(Teleport.COMPATIBILITY_VERSION));
            });
        });
    }

    public static <T> T registerConfig(ModConfig.Type type, Function<ForgeConfigSpec.Builder, T> consumer) {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        T config = consumer.apply(builder);
        ForgeConfigSpec spec = builder.build();
        ModLoadingContext.get().registerConfig(type, spec);
        return config;
    }
}