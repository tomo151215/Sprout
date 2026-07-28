package engine.input;

import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public final class InputManager<T extends Enum<T>> {
    private final Keyboard keyboard;  
    private final Map<T, List<Integer>> mappings;

    public InputManager(Keyboard keyboard, Class<T> actionClass) {
        this.keyboard = keyboard;
        this.mappings = new EnumMap<>(actionClass);
        for (T action : actionClass.getEnumConstants()) {
            mappings.put(action, new ArrayList<>());
        }
    }

    public void addMapping(T action, int keycode) {
        mappings.get(action).add(keycode);
    }

    public boolean isPressed(T action) {
        for (int keycode : mappings.get(action)) {
            if (keyboard.isPressed(keycode))
                return true;
        }
        return false;
    }

    public boolean isJustPressed(T action) {
        for (int keycode : mappings.get(action)) {
            if (keyboard.isJustPressed(keycode))
                return true;
        }
        return false;
    }

    public boolean isJustReleased(T action) {
        for (int keycode : mappings.get(action)) {
            if (keyboard.isJustReleased(keycode))
                return true;
        }
        return false;
    }
}
