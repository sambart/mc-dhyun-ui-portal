package com.dhyun.portal.voice.client;

import com.dhyun.portal.intercompatibility.ClientCompatibilityManager;
import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.plugins.CategoryManager;
import com.dhyun.portal.plugins.impl.VolumeCategoryImpl;
import com.mojang.blaze3d.platform.NativeImage;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.gui.volume.AdjustVolumeList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientCategoryManager extends CategoryManager {

    protected final Map<String, ResourceLocation> images;

    public ClientCategoryManager() {
        images = new ConcurrentHashMap<>();
        CommonCompatibilityManager.INSTANCE.getNetManager().addCategoryChannel.setClientListener((client, handler, packet) -> {
            addCategory(packet.getCategory());
            Teleport.logDebug("Added category {}", packet.getCategory().getId());
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().removeCategoryChannel.setClientListener((client, handler, packet) -> {
            removeCategory(packet.getCategoryId());
            Teleport.logDebug("Removed category {}", packet.getCategoryId());
        });
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::clear);
    }

    @Override
    public void addCategory(VolumeCategoryImpl category) {
        super.addCategory(category);

        if (category.getIcon() != null) {
            registerImage(category.getId(), fromIntArray(category.getIcon()));
        }
        AdjustVolumeList.update();
    }

    @Override
    public void removeCategory(String categoryId) {
        super.removeCategory(categoryId);
        unRegisterImage(categoryId);
        AdjustVolumeList.update();
    }

    public void clear() {
        categories.keySet().forEach(this::unRegisterImage);
        categories.clear();
    }

    private void registerImage(String id, NativeImage image) {
        ResourceLocation resourceLocation = Minecraft.getInstance().getEntityRenderDispatcher().textureManager.register(id, new DynamicTexture(image));
        images.put(id, resourceLocation);
    }

    private void unRegisterImage(String id) {
        ResourceLocation resourceLocation = images.get(id);
        if (resourceLocation != null) {
            Minecraft.getInstance().getEntityRenderDispatcher().textureManager.release(resourceLocation);
            images.remove(id);
        }
    }

    private NativeImage fromIntArray(int[][] icon) {
        if (icon.length != 16) {
            throw new IllegalStateException("Icon is not 16x16");
        }
        NativeImage nativeImage = new NativeImage(16, 16, true);
        for (int x = 0; x < icon.length; x++) {
            if (icon[x].length != 16) {
                nativeImage.close();
                throw new IllegalStateException("Icon is not 16x16");
            }
            for (int y = 0; y < icon.length; y++) {
                nativeImage.setPixelRGBA(x, y, icon[x][y]);
            }
        }
        return nativeImage;
    }

    public ResourceLocation getTexture(String id, ResourceLocation defaultImage) {
        return images.getOrDefault(id, defaultImage);
    }

}
