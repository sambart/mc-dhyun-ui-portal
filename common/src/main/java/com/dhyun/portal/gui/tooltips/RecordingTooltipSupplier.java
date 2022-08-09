package com.dhyun.portal.gui.tooltips;

import com.dhyun.portal.voice.client.ClientManager;
import com.dhyun.portal.voice.client.ClientVoicechat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.dhyun.portal.gui.widgets.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class RecordingTooltipSupplier implements ImageButton.TooltipSupplier {

    private final Screen screen;

    public RecordingTooltipSupplier(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return;
        }

        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (client.getRecorder() == null) {
            tooltip.add(Component.translatable("message.voicechat.recording.disabled").getVisualOrderText());
        } else {
            tooltip.add(Component.translatable("message.voicechat.recording.enabled").getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

}
