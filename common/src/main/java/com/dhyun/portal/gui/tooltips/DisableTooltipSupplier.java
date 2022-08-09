package com.dhyun.portal.gui.tooltips;

import com.dhyun.portal.voice.client.ClientPlayerStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.dhyun.portal.gui.widgets.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class DisableTooltipSupplier implements ImageButton.TooltipSupplier {

    private final Screen screen;
    private final ClientPlayerStateManager stateManager;

    public DisableTooltipSupplier(Screen screen, ClientPlayerStateManager stateManager) {
        this.screen = screen;
        this.stateManager = stateManager;
    }

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (!stateManager.canEnable()) {
            tooltip.add(Component.translatable("message.voicechat.disable.no_speaker").getVisualOrderText());
        } else if (stateManager.isDisabled()) {
            tooltip.add(Component.translatable("message.voicechat.disable.enabled").getVisualOrderText());
        } else {
            tooltip.add(Component.translatable("message.voicechat.disable.disabled").getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

}
