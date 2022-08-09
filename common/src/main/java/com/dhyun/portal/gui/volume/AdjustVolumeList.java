package com.dhyun.portal.gui.volume;

import com.dhyun.portal.plugins.impl.VolumeCategoryImpl;
import com.dhyun.portal.voice.client.ClientManager;
import com.dhyun.portal.voice.common.PlayerState;
import com.google.common.collect.Lists;
import com.dhyun.portal.TeleportClient;
import com.dhyun.portal.gui.widgets.ListScreenListBase;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import java.util.*;

public class AdjustVolumeList extends ListScreenListBase<VolumeEntry> {

    protected AdjustVolumesScreen screen;
    protected final List<VolumeEntry> entries;
    protected String filter;

    public AdjustVolumeList(int width, int height, int x, int y, int size, AdjustVolumesScreen screen) {
        super(width, height, x, y, size);
        this.screen = screen;
        this.entries = Lists.newArrayList();
        this.filter = "";
        setRenderBackground(false);
        setRenderTopAndBottom(false);
        updateEntryList();
    }

    public static void update() {
        if (Minecraft.getInstance().screen instanceof AdjustVolumesScreen volumesScreen) {
            volumesScreen.volumeList.updateEntryList();
        }
    }

    public void updateEntryList() {
        Collection<PlayerState> onlinePlayers = ClientManager.getPlayerStateManager().getPlayerStates(false);
        entries.clear();

        for (VolumeCategoryImpl category : ClientManager.getCategoryManager().getCategories()) {
            entries.add(new CategoryVolumeEntry(category, screen));
        }

        for (PlayerState state : onlinePlayers) {
            entries.add(new PlayerVolumeEntry(state, screen));
        }

        if (TeleportClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment.get()) {
            addOfflinePlayers(onlinePlayers);
        }

        updateFilter();
    }

    private void addOfflinePlayers(Collection<PlayerState> onlinePlayers) {
        for (UUID uuid : TeleportClient.VOLUME_CONFIG.getPlayerVolumes().keySet()) {
            if (uuid.equals(Util.NIL_UUID)) {
                continue;
            }
            if (onlinePlayers.stream().anyMatch(state -> uuid.equals(state.getUuid()))) {
                continue;
            }

            String name = TeleportClient.USERNAME_CACHE.getUsername(uuid);

            if (name == null) {
                continue;
            }

            entries.add(new PlayerVolumeEntry(new PlayerState(uuid, name, false, true), screen));
        }
    }

    public void updateFilter() {
        clearEntries();
        List<VolumeEntry> filteredEntries = new ArrayList<>(entries);
        if (!filter.isEmpty()) {
            filteredEntries.removeIf(volumeEntry -> {
                if (volumeEntry instanceof PlayerVolumeEntry playerVolumeEntry) {
                    return playerVolumeEntry.getState() == null || !playerVolumeEntry.getState().getName().toLowerCase(Locale.ROOT).contains(filter);
                } else if (volumeEntry instanceof CategoryVolumeEntry categoryVolumeEntry) {
                    return !categoryVolumeEntry.getCategory().getName().toLowerCase(Locale.ROOT).contains(filter);
                }
                return true;
            });
        }
        filteredEntries.sort((e1, e2) -> {
            if (!e1.getClass().equals(e2.getClass())) {
                if (e1 instanceof PlayerVolumeEntry) {
                    return 1;
                } else {
                    return -1;
                }
            }

            return volumeEntryToString(e1).compareToIgnoreCase(volumeEntryToString(e2));
        });
        if (filter.isEmpty()) {
            filteredEntries.add(0, new PlayerVolumeEntry(null, screen));
        }
        replaceEntries(filteredEntries);
    }

    private String volumeEntryToString(VolumeEntry entry) {
        if (entry instanceof PlayerVolumeEntry playerVolumeEntry) {
            return playerVolumeEntry.getState() == null ? "" : playerVolumeEntry.getState().getName();
        } else if (entry instanceof CategoryVolumeEntry categoryVolumeEntry) {
            return categoryVolumeEntry.getCategory().getName();
        }
        return "";
    }

    public void setFilter(String filter) {
        this.filter = filter;
        updateFilter();
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

}
