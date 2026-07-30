package engine.input;

import java.util.List;

public final class InputContext<T extends Enum<T>> {
    private final Keyboard keyboard;
    private final Mouse mouse;
    private final InputManager<T> inputManager;

    public InputContext(Keyboard keyboard, Mouse mouse, Class<T> actionClass) {
        if (keyboard == null) {
            throw new IllegalArgumentException("keyboard must not be null.");
        }
        if (mouse == null) {
            throw new IllegalArgumentException("mouse must not be null.");
        }
        if (actionClass == null) {
            throw new IllegalArgumentException("actionClass must not be null.");
        }

        this.keyboard = keyboard;
        this.mouse = mouse;
        this.inputManager = new InputManager<>(keyboard, actionClass);
    }

    // InputManagerのラッピング

    public void addMapping(T action, int keycode) {
        this.inputManager.addMapping(action, keycode);
    }

    public boolean isPressed(T action) {
        return this.inputManager.isPressed(action);
    }

    public boolean isJustPressed(T action) {
        return this.inputManager.isJustPressed(action);
    }

    public boolean isJustReleased(T action) {
        return this.inputManager.isJustReleased(action);
    }

    public void removeMapping(T action, int keycode) {
        this.inputManager.removeMapping(action, keycode);
    }

    public void clearMapping(T action) {
        this.inputManager.clearAllMapping();
    }

    public void clearAllMapping() {
        this.inputManager.clearAllMapping();
    }

    public List<Integer> getMappings(T action) {
        return this.inputManager.getMappings(action);
    }

    public boolean hasMapping(T action, int keyCode) {
        return this.inputManager.hasMapping(action, keyCode);
    }

    // Mouseのラップ
    public int getMouseX() {
        return this.mouse.getX();
    }

    public int getMouseY() {
        return this.mouse.getY();
    }

    public int getMousePreviousX() {
        return this.mouse.getPreviousX();
    }

    public int getMousePreviousY() {
        return this.mouse.getPreviousY();
    }

    public int getMouseDeltaX() {
        return this.mouse.getDeltaX();
    }

    public int getMouseDeltaY() {
        return this.mouse.getDeltaY();
    }

    public boolean isMousePressed(MouseButton button) {
        return this.mouse.isPressed(button);
    }

    public boolean isMouseJustPressed(MouseButton button) {
        return this.mouse.isJustPressed(button);
    }

    public boolean isMouseJustReleased(MouseButton button) {
        return this.mouse.isJustReleased(button);
    }

    public int getMouseWheelRotation() {
        return this.mouse.getWheelRotation();
    }

    // getter
    public Keyboard getKeyboard() {
        return keyboard;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public InputManager<T> getInputManager() {
        return inputManager;
    }

    // 入力状態リセット
    public void clear() {
        keyboard.clear();
        mouse.clear();
    }
}
