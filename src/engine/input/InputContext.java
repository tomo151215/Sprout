package engine.input;

import java.util.List;

public final class InputContext<T extends Enum<T>> {
    private final Mouse mouse;
    private final InputManager<T> inputManager;

    private boolean enabled = true;

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

        this.mouse = mouse;
        this.inputManager = new InputManager<>(keyboard, actionClass);
    }

    public void addMapping(T action, int keyCode) {
        inputManager.addMapping(action, keyCode);
    }

    public void removeMapping(T action, int keyCode) {
        inputManager.removeMapping(action, keyCode);
    }

    public void clearMapping(T action) {
        inputManager.clearMapping(action);
    }

    public void clearAllMappings() {
        inputManager.clearAllMappings();
    }

    public List<Integer> getMappings(T action) {
        return inputManager.getMappings(action);
    }

    public boolean hasMapping(T action, int keyCode) {
        return inputManager.hasMapping(action, keyCode);
    }

    public boolean isPressed(T action) {
        return enabled && inputManager.isPressed(action);
    }

    public boolean isJustPressed(T action) {
        return enabled && inputManager.isJustPressed(action);
    }

    public boolean isJustReleased(T action) {
        return enabled && inputManager.isJustReleased(action);
    }

    public int getMouseX() {
        return mouse.getX();
    }

    public int getMouseY() {
        return mouse.getY();
    }

    public int getMousePreviousX() {
        return mouse.getPreviousX();
    }

    public int getMousePreviousY() {
        return mouse.getPreviousY();
    }

    public int getMouseDeltaX() {
        return mouse.getDeltaX();
    }

    public int getMouseDeltaY() {
        return mouse.getDeltaY();
    }

    public boolean isMousePressed(MouseButton button) {
        return enabled && mouse.isPressed(button);
    }

    public boolean isMouseJustPressed(MouseButton button) {
        return enabled && mouse.isJustPressed(button);
    }

    public boolean isMouseJustReleased(MouseButton button) {
        return enabled && mouse.isJustReleased(button);
    }

    public int getMouseWheelRotation() {
        return enabled ? mouse.getWheelRotation() : 0;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
