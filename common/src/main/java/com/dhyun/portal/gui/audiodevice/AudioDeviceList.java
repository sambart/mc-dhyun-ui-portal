package com.dhyun.portal.gui.audiodevice;

import com.dhyun.portal.gui.widgets.ListScreenListBase;

import java.util.Collection;

public class AudioDeviceList extends ListScreenListBase<AudioDeviceEntry> {

    public AudioDeviceList(int width, int height, int x, int y, int size) {
        super(width, height, x, y, size);
        setRenderBackground(false);
        setRenderTopAndBottom(false);
    }

    @Override
    public void replaceEntries(Collection<AudioDeviceEntry> entries) {
        super.replaceEntries(entries);
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

}
