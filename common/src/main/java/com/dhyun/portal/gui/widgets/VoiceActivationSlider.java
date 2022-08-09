package com.dhyun.portal.gui.widgets;

import com.dhyun.portal.voice.common.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER = new ResourceLocation(Teleport.MODID, "textures/gui/voice_activation_slider.png");
    private static final Component NO_ACTIVATION = Component.translatable("message.voicechat.voice_activation.disabled").withStyle(ChatFormatting.RED);

    private double micValue;

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), Utils.dbToPerc(TeleportClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int i, int j) {
        RenderSystem.setShaderTexture(0, SLIDER);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        int width = (int) (226D * micValue);
        blit(poseStack, x + 1, y + 1, 0, 0, width, 18);
        super.renderBg(poseStack, minecraft, i, j);
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(Utils.percToDb(value));
        MutableComponent component = Component.translatable("message.voicechat.voice_activation", db);

        if (db >= -10L) {
            component.withStyle(ChatFormatting.RED);
        }

        setMessage(component);
    }

    @Nullable
    public Component getTooltip() {
        if (value >= 1D) {
            return NO_ACTIVATION;
        }
        return null;
    }

    public boolean isHovered() {
        return isHovered;
    }

    @Override
    public void applyDebounced() {
        TeleportClient.CLIENT_CONFIG.voiceActivationThreshold.set(Utils.percToDb(value)).save();
    }

    @Override
    public void onMicValue(double percentage) {
        this.micValue = percentage;
    }
}
