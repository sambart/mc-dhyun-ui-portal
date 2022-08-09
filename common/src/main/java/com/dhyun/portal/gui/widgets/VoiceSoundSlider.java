package com.dhyun.portal.gui.widgets;

import com.dhyun.portal.TeleportClient;
import net.minecraft.network.chat.Component;

public class VoiceSoundSlider extends DebouncedSlider {

    public VoiceSoundSlider(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), TeleportClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() / 2F);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(getMsg());
    }

    public Component getMsg() {
        return Component.translatable("message.voicechat.voice_chat_volume", Math.round(value * 200F) + "%");
    }

    @Override
    public void applyDebounced() {
        TeleportClient.CLIENT_CONFIG.voiceChatVolume.set(value * 2F).save();
    }
}
