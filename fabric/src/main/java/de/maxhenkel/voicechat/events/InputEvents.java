package de.maxhenkel.voicechat.events;

import com.dhyun.portal.intercompatibility.ClientCompatibilityManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class InputEvents {

    public static final Event<ClientCompatibilityManager.KeyboardEvent> KEYBOARD_KEY = EventFactory.createArrayBacked(ClientCompatibilityManager.KeyboardEvent.class, (listeners) -> (window, key, scancode) -> {
        for (ClientCompatibilityManager.KeyboardEvent event : listeners) {
            event.onKeyboardEvent(window, key, scancode);
        }
    });

    public static final Event<ClientCompatibilityManager.MouseEvent> MOUSE_KEY = EventFactory.createArrayBacked(ClientCompatibilityManager.MouseEvent.class, (listeners) -> (window, button, action, mods) -> {
        for (ClientCompatibilityManager.MouseEvent event : listeners) {
            event.onMouseEvent(window, button, action, mods);
        }
    });

    public static final Event<Runnable> HANDLE_KEYBINDS = EventFactory.createArrayBacked(Runnable.class, (listeners) -> () -> {
        for (Runnable event : listeners) {
            event.run();
        }
    });

}
