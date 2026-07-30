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
        List<Integer> keys = getKeyList(action);
        if (!keys.contains(keycode)) {
            keys.add(keycode);
        }
    }

    public boolean isPressed(T action) {
        for (int keycode : getKeyList(action)) {
            if (keyboard.isPressed(keycode))
                return true;
        }
        return false;
    }

    public boolean isJustPressed(T action) {
        for (int keycode : getKeyList(action)) {
            if (keyboard.isJustPressed(keycode))
                return true;
        }
        return false;
    }

    public boolean isJustReleased(T action) {
        for (int keycode : getKeyList(action)) {
            if (keyboard.isJustReleased(keycode))
                return true;
        }
        return false;
    }

    public void removeMapping(T action, int keycode) {
        getKeyList(action).remove(Integer.valueOf(keycode));
    }

    public void clearMapping(T action) {
        getKeyList(action).clear();
    }

    public void clearAllMapping() {
        for (List<Integer> value : mappings.values()) {
            value.clear();
        }
    }

    public List<Integer> getMappings(T action) {
        return List.copyOf(getKeyList(action));
    }

    public boolean hasMapping(T action, int keyCode) {
        return getKeyList(action).contains(keyCode);
    }

    private List<Integer> getKeyList(T action) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null. ");
        }
        List<Integer> keys = mappings.get(action);
        if (keys == null) {
            throw new IllegalArgumentException("Unknown action: " + action);
        }
        return keys;
    }
}
