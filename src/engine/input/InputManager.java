package engine.input;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InputManager<T extends Enum<T>> {
    private final Keyboard keyboard;
    private final Map<T, Set<Integer>> mappings;

    public InputManager(Keyboard keyboard, Class<T> actionClass) {
        if (keyboard == null) {
            throw new IllegalArgumentException("keyboard must not be null.");
        }
        if (actionClass == null) {
            throw new IllegalArgumentException("actionClass must not be null.");
        }

        T[] actions = actionClass.getEnumConstants();
        if (actions == null) {
            throw new IllegalArgumentException("actionClass must represent an enum type.");
        }

        this.keyboard = keyboard;
        this.mappings = createMappings(actionClass, actions);
    }

    public void addMapping(T action, int keyCode) {
        if (keyCode < 0) {
            throw new IllegalArgumentException("keyCode must be greater than or equal to 0.");
        }
        getKeySet(action).add(keyCode);
    }

    public void removeMapping(T action, int keyCode) {
        getKeySet(action).remove(keyCode);
    }

    public void clearMapping(T action) {
        getKeySet(action).clear();
    }

    public void clearAllMappings() {
        mappings.values().forEach(Set::clear);
    }

    public List<Integer> getMappings(T action) {
        return List.copyOf(getKeySet(action));
    }

    public boolean hasMapping(T action, int keyCode) {
        return getKeySet(action).contains(keyCode);
    }

    public boolean isPressed(T action) {
        return anyMappedKeyMatches(action, keyboard::isPressed);
    }

    public boolean isJustPressed(T action) {
        return anyMappedKeyMatches(action, keyboard::isJustPressed);
    }

    public boolean isJustReleased(T action) {
        return anyMappedKeyMatches(action, keyboard::isJustReleased);
    }

    private boolean anyMappedKeyMatches(T action, KeyStateQuery query) {
        for (int keyCode : getKeySet(action)) {
            if (query.test(keyCode)) {
                return true;
            }
        }
        return false;
    }

    private Map<T, Set<Integer>> createMappings(Class<T> actionClass, T[] actions) {
        Map<T, Set<Integer>> result = new EnumMap<>(actionClass);
        for (T action : actions) {
            result.put(action, new LinkedHashSet<>());
        }
        return result;
    }

    private Set<Integer> getKeySet(T action) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null.");
        }

        Set<Integer> keys = mappings.get(action);
        if (keys == null) {
            throw new IllegalArgumentException("Unknown action: " + action);
        }
        return keys;
    }

    @FunctionalInterface
    private interface KeyStateQuery {
        boolean test(int keyCode);
    }
}
