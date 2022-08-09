package com.dhyun.portal.gui.widgets;

import com.dhyun.portal.TeleportClient;
import net.minecraft.network.chat.Component;

public class MicAmplificationSlider extends DebouncedSlider {

    private static final float MAXIMUM = 4F;

    public MicAmplificationSlider(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, Component.empty(), TeleportClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue() / MAXIMUM);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        long amp = Math.round(value * MAXIMUM * 100F - 100F);
        setMessage(Component.translatable("message.voicechat.microphone_amplification", (amp > 0F ? "+" : "") + amp + "%"));
    }

    @Override
    public void applyDebounced() {
        TeleportClient.CLIENT_CONFIG.microphoneAmplification.set(value * MAXIMUM).save();
    }
}
