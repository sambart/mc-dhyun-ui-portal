package com.dhyun.portal.gui.audiodevice;

import com.dhyun.portal.voice.client.ClientManager;
import com.dhyun.portal.voice.client.ClientVoicechat;
import com.dhyun.portal.voice.client.SoundManager;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class SelectSpeakerScreen extends SelectDeviceScreen {

    protected static final ResourceLocation SPEAKER_ICON = new ResourceLocation(Teleport.MODID, "textures/icons/speaker.png");
    protected static final Component TITLE = Component.translatable("gui.voicechat.select_speaker.title");
    protected static final Component NO_SPEAKER = Component.translatable("message.voicechat.no_speaker").withStyle(ChatFormatting.GRAY);

    public SelectSpeakerScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    public List<String> getDevices() {
        return SoundManager.getAllSpeakers();
    }

    @Override
    public String getSelectedDevice() {
        return TeleportClient.CLIENT_CONFIG.speaker.get();
    }

    @Override
    public ResourceLocation getIcon(String device) {
        return SPEAKER_ICON;
    }

    @Override
    public Component getEmptyListComponent() {
        return NO_SPEAKER;
    }

    @Override
    public String getVisibleName(String device) {
        return SoundManager.cleanDeviceName(device);
    }

    @Override
    public void onSelect(String device) {
        TeleportClient.CLIENT_CONFIG.speaker.set(device).save();
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.reloadAudio();
        }
    }
}
