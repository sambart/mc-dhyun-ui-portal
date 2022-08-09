package com.dhyun.portal.gui.audiodevice;

import com.dhyun.portal.voice.client.ClientManager;
import com.dhyun.portal.voice.client.ClientVoicechat;
import com.dhyun.portal.voice.client.SoundManager;
import com.dhyun.portal.voice.client.microphone.MicrophoneManager;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class SelectMicrophoneScreen extends SelectDeviceScreen {

    protected static final ResourceLocation MICROPHONE_ICON = new ResourceLocation(Teleport.MODID, "textures/icons/microphone.png");
    protected static final Component TITLE = Component.translatable("gui.voicechat.select_microphone.title");
    protected static final Component NO_MICROPHONE = Component.translatable("message.voicechat.no_microphone").withStyle(ChatFormatting.GRAY);

    public SelectMicrophoneScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    public List<String> getDevices() {
        return MicrophoneManager.deviceNames();
    }

    @Override
    public String getSelectedDevice() {
        return TeleportClient.CLIENT_CONFIG.microphone.get();
    }

    @Override
    public ResourceLocation getIcon(String device) {
        return MICROPHONE_ICON;
    }

    @Override
    public Component getEmptyListComponent() {
        return NO_MICROPHONE;
    }

    @Override
    public String getVisibleName(String device) {
        return SoundManager.cleanDeviceName(device);
    }

    @Override
    public void onSelect(String device) {
        TeleportClient.CLIENT_CONFIG.microphone.set(device).save();
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.reloadAudio();
        }
    }
}
