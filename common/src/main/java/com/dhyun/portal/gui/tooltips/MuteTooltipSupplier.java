package com.dhyun.portal.gui.tooltips;

import com.dhyun.portal.voice.client.ClientPlayerStateManager;
import com.dhyun.portal.voice.client.MicrophoneActivationType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.dhyun.portal.TeleportClient;
import com.dhyun.portal.gui.widgets.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class MuteTooltipSupplier implements ImageButton.TooltipSupplier {

    private Screen screen;
    private ClientPlayerStateManager stateManager;

    public MuteTooltipSupplier(Screen screen, ClientPlayerStateManager stateManager) {
        this.screen = screen;
        this.stateManager = stateManager;
    }

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        if (!canMuteMic()) {
            tooltip.add(Component.translatable("message.voicechat.mute.disabled_ptt").getVisualOrderText());
        } else if (stateManager.isMuted()) {
            tooltip.add(Component.translatable("message.voicechat.mute.enabled").getVisualOrderText());
        } else {
            tooltip.add(Component.translatable("message.voicechat.mute.disabled").getVisualOrderText());
        }

        screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

    public static boolean canMuteMic() {
        return TeleportClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
    }

}
