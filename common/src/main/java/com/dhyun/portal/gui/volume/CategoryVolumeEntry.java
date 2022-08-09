package com.dhyun.portal.gui.volume;

import com.dhyun.portal.plugins.impl.VolumeCategoryImpl;
import com.dhyun.portal.voice.client.ClientManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.dhyun.portal.TeleportClient;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CategoryVolumeEntry extends VolumeEntry {

    protected final VolumeCategoryImpl category;
    protected final ResourceLocation texture;

    public CategoryVolumeEntry(VolumeCategoryImpl category, AdjustVolumesScreen screen) {
        super(screen, new CategoryVolumeConfigEntry(category.getId()));
        this.category = category;
        this.texture = ClientManager.getCategoryManager().getTexture(category.getId(), OTHER_VOLUME_ICON);
    }

    public VolumeCategoryImpl getCategory() {
        return category;
    }

    @Override
    public void renderElement(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16, 16, 16);
        minecraft.font.draw(poseStack, Component.literal(category.getName()), (float) textX, (float) textY, PLAYER_NAME_COLOR);
        if (hovered && category.getDescription() != null) {
            screen.postRender(() -> {
                screen.renderTooltip(poseStack, Component.literal(category.getDescription()), mouseX, mouseY);
            });
        }
    }

    private static class CategoryVolumeConfigEntry implements AdjustVolumeSlider.VolumeConfigEntry {

        private final String category;

        public CategoryVolumeConfigEntry(String category) {
            this.category = category;
        }

        @Override
        public void save(double value) {
            TeleportClient.VOLUME_CONFIG.setCategoryVolume(category, value);
            TeleportClient.VOLUME_CONFIG.save();
        }

        @Override
        public double get() {
            return TeleportClient.VOLUME_CONFIG.getCategoryVolume(category);
        }
    }

}
