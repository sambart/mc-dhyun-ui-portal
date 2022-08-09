package com.dhyun.portal.voice.server;

import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.plugins.CategoryManager;
import com.dhyun.portal.plugins.impl.VolumeCategoryImpl;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.net.AddCategoryPacket;
import com.dhyun.portal.net.NetManager;
import com.dhyun.portal.net.RemoveCategoryPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ServerCategoryManager extends CategoryManager {

    private final Server server;

    public ServerCategoryManager(Server server) {
        this.server = server;
        CommonCompatibilityManager.INSTANCE.onPlayerCompatibilityCheckSucceeded(this::onPlayerCompatibilityCheckSucceeded);
    }

    private void onPlayerCompatibilityCheckSucceeded(ServerPlayer player) {
        Teleport.logDebug("Synchronizing {} volume categories with {}", categories.size(), player.getDisplayName().getString());
        for (VolumeCategoryImpl category : getCategories()) {
            broadcastAddCategory(server.getServer(), category);
        }
    }

    @Override
    public void addCategory(VolumeCategoryImpl category) {
        super.addCategory(category);
        Teleport.logDebug("Synchronizing volume category {} with all players", category.getId());
        broadcastAddCategory(server.getServer(), category);
    }

    @Override
    public void removeCategory(String categoryId) {
        super.removeCategory(categoryId);
        Teleport.logDebug("Removing volume category {} for all players", categoryId);
        broadcastRemoveCategory(server.getServer(), categoryId);
    }

    private void broadcastAddCategory(MinecraftServer server, VolumeCategoryImpl category) {
        AddCategoryPacket packet = new AddCategoryPacket(category);
        server.getPlayerList().getPlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    private void broadcastRemoveCategory(MinecraftServer server, String categoryId) {
        RemoveCategoryPacket packet = new RemoveCategoryPacket(categoryId);
        server.getPlayerList().getPlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

}
