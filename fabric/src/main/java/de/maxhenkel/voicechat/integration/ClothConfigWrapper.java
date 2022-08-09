package de.maxhenkel.voicechat.integration;

import de.maxhenkel.configbuilder.ConfigEntry;
import com.dhyun.portal.TeleportClient;
import com.dhyun.portal.voice.client.GroupPlayerIconOrientation;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ClothConfigWrapper {

    static final MutableComponent OTHER_SETTINGS = Component.translatable("cloth_config.voicechat.category.other");

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder
                .create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("cloth_config.voicechat.settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory audio = builder.getOrCreateCategory(Component.translatable("cloth_config.voicechat.category.audio"));
        audio.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.audio_packet_threshold"), TeleportClient.CLIENT_CONFIG.audioPacketThreshold));
        audio.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.deactivation_delay"), TeleportClient.CLIENT_CONFIG.deactivationDelay));
        audio.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.output_buffer_size"), TeleportClient.CLIENT_CONFIG.outputBufferSize));
        audio.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.recording_destination"), TeleportClient.CLIENT_CONFIG.recordingDestination));
        audio.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.run_local_server"), TeleportClient.CLIENT_CONFIG.runLocalServer));
        audio.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.offline_player_volume_adjustment"), TeleportClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment));

        ConfigCategory hudIcons = builder.getOrCreateCategory(Component.translatable("cloth_config.voicechat.category.hud_icons"));
        hudIcons.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.hud_icon_scale"), TeleportClient.CLIENT_CONFIG.hudIconScale));
        hudIcons.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.hud_icon_x"), TeleportClient.CLIENT_CONFIG.hudIconPosX));
        hudIcons.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.hud_icon_y"), TeleportClient.CLIENT_CONFIG.hudIconPosY));

        ConfigCategory groupIcons = builder.getOrCreateCategory(Component.translatable("cloth_config.voicechat.category.group_chat_icons"));
        groupIcons.addEntry(entryBuilder
                .startEnumSelector(Component.translatable("cloth_config.voicechat.config.group_player_icon_orientation"), GroupPlayerIconOrientation.class, TeleportClient.CLIENT_CONFIG.groupPlayerIconOrientation.get())
                .setDefaultValue(TeleportClient.CLIENT_CONFIG.groupPlayerIconOrientation::getDefault)
                .setSaveConsumer(e -> TeleportClient.CLIENT_CONFIG.groupPlayerIconOrientation.set(e).save())
                .build()
        );
        groupIcons.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.group_hud_icon_scale"), TeleportClient.CLIENT_CONFIG.groupHudIconScale));
        groupIcons.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.group_player_icon_pos_x"), TeleportClient.CLIENT_CONFIG.groupPlayerIconPosX));
        groupIcons.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.group_player_icon_pos_y"), TeleportClient.CLIENT_CONFIG.groupPlayerIconPosY));
        groupIcons.addEntry(fromConfigEntry(entryBuilder, Component.translatable("cloth_config.voicechat.config.show_own_group_icon"), TeleportClient.CLIENT_CONFIG.showOwnGroupIcon));

        builder.getOrCreateCategory(OTHER_SETTINGS);
        return builder.build();
    }

    private static <T> AbstractConfigListEntry<T> fromConfigEntry(ConfigEntryBuilder entryBuilder, Component name, ConfigEntry<T> entry) {
        if (entry instanceof de.maxhenkel.configbuilder.ConfigBuilder.DoubleConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startDoubleField(name, e.get())
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> {
                        e.set(d);
                        e.save();
                    })
                    .build();
        } else if (entry instanceof de.maxhenkel.configbuilder.ConfigBuilder.IntegerConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startIntField(name, e.get())
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        } else if (entry instanceof de.maxhenkel.configbuilder.ConfigBuilder.BooleanConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startBooleanToggle(name, e.get())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        } else if (entry instanceof de.maxhenkel.configbuilder.ConfigBuilder.StringConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startStrField(name, e.get())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        }

        return null;
    }

}
