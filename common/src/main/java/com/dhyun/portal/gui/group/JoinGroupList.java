package com.dhyun.portal.gui.group;

import com.dhyun.portal.gui.widgets.ListScreenBase;
import com.dhyun.portal.gui.widgets.ListScreenListBase;
import com.dhyun.portal.voice.client.ClientManager;
import com.dhyun.portal.voice.common.PlayerState;

import java.util.*;
import java.util.stream.Collectors;

public class JoinGroupList extends ListScreenListBase<JoinGroupEntry> {

    protected final ListScreenBase parent;

    public JoinGroupList(ListScreenBase parent, int width, int height, int x, int y, int size) {
        super(width, height, x, y, size);
        this.parent = parent;
        setRenderBackground(false);
        setRenderTopAndBottom(false);
        tick();
    }

    public void tick() {
        Map<UUID, JoinGroupEntry.Group> groups = new HashMap<>();
        Collection<PlayerState> playerStates = ClientManager.getPlayerStateManager().getPlayerStates(true);

        for (PlayerState state : playerStates) {
            if (!state.hasGroup()) {
                continue;
            }

            JoinGroupEntry.Group group = groups.getOrDefault(state.getGroup().getId(), new JoinGroupEntry.Group(state.getGroup()));
            group.getMembers().add(state);
            group.getMembers().sort(Comparator.comparing(PlayerState::getName));
            groups.put(state.getGroup().getId(), group);
        }

        replaceEntries(groups.values().stream().map(group -> new JoinGroupEntry(parent, group)).sorted(Comparator.comparing(o -> o.getGroup().getGroup().getName())).collect(Collectors.toList()));
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }
}
