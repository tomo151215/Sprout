package engine.input;

import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public final class InputManager<T extends Enum<T>> {
    private final Keyboard keyboard;  //委譲
    private final Map<T, List<Integer>> mappings;

    public InputManager(Keyboard keyboard, Class<T> actionClass) {
        this.keyboard = keyboard;
        this.mappings = new EnumMap<>(actionClass);
        for (T action : actionClass.getEnumConstants()) {
            mappings.put(action, new ArrayList<>());
        }
    }

    // キーマッピングを行う
    public void addMapping(T action, int keycode) {
        mappings.get(action).add(keycode);
    }

    // 指定したアクションが押されているかどうか判定
    public boolean isPressed(T action) {
        for (int keycode : mappings.get(action)) {
            if (keyboard.isPressed(keycode))
                return true;
        }
        return false;
    }

    // 指定したアクションが押された瞬間かどうかを判定
    public boolean isJustPressed(T action) {
        for (int keycode : mappings.get(action)) {
            if (keyboard.isJustPressed(keycode))
                return true;
        }
        return false;
    }

    // 指定したアクションが離された瞬間かどうかを判定
    public boolean isJustReleased(T action) {
        for (int keycode : mappings.get(action)) {
            if (keyboard.isJustReleased(keycode))
                return true;
        }
        return false;
    }
}
