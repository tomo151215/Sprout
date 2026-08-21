package engine.input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;

public final class Mouse extends MouseAdapter {
    private static final int BUTTON_COUNT = MouseButton.values().length;

    private final boolean[] currentPressed = new boolean[BUTTON_COUNT];
    private final boolean[] pressed = new boolean[BUTTON_COUNT];
    private final boolean[] previousPressed = new boolean[BUTTON_COUNT];

    private int currentX;
    private int currentY;
    private int x;
    private int y;
    private int previousX;
    private int previousY;

    private int currentWheelRotation;
    private int wheelRotation;

    private final Object lock = new Object();

    public void updateSnapshot() {
        synchronized (lock) {
            previousX = x;
            previousY = y;
            System.arraycopy(pressed, 0, previousPressed, 0, BUTTON_COUNT);

            x = currentX;
            y = currentY;
            System.arraycopy(currentPressed, 0, pressed, 0, BUTTON_COUNT);

            wheelRotation = currentWheelRotation;
            currentWheelRotation = 0;
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
        updateButtonState(event, true);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        updateButtonState(event, false);
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        updateCurrentPosition(event);
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        updateCurrentPosition(event);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent event) {
        synchronized (lock) {
            currentWheelRotation += event.getWheelRotation();
            setCurrentPosition(event);
        }
    }

    public int getX() {
        synchronized (lock) {
            return x;
        }
    }

    public int getY() {
        synchronized (lock) {
            return y;
        }
    }

    public int getPreviousX() {
        synchronized (lock) {
            return previousX;
        }
    }

    public int getPreviousY() {
        synchronized (lock) {
            return previousY;
        }
    }

    public int getDeltaX() {
        synchronized (lock) {
            return x - previousX;
        }
    }

    public int getDeltaY() {
        synchronized (lock) {
            return y - previousY;
        }
    }

    public boolean isPressed(MouseButton button) {
        int index = index(button);

        synchronized (lock) {
            return pressed[index];
        }
    }

    public boolean isJustPressed(MouseButton button) {
        int index = index(button);

        synchronized (lock) {
            return pressed[index] && !previousPressed[index];
        }
    }

    public boolean isJustReleased(MouseButton button) {
        int index = index(button);

        synchronized (lock) {
            return !pressed[index] && previousPressed[index];
        }
    }

    public int getWheelRotation() {
        synchronized (lock) {
            return wheelRotation;
        }
    }

    public void clear() {
        synchronized (lock) {
            Arrays.fill(currentPressed, false);
            Arrays.fill(pressed, false);
            Arrays.fill(previousPressed, false);
            currentWheelRotation = 0;
            wheelRotation = 0;
        }
    }

    private void updateButtonState(MouseEvent event, boolean isPressed) {
        MouseButton button = toMouseButton(event.getButton());
        if (button == null) {
            return;
        }

        synchronized (lock) {
            currentPressed[index(button)] = isPressed;
            setCurrentPosition(event);
        }
    }

    private void updateCurrentPosition(MouseEvent event) {
        synchronized (lock) {
            setCurrentPosition(event);
        }
    }

    private void setCurrentPosition(MouseEvent event) {
        currentX = event.getX();
        currentY = event.getY();
    }

    private MouseButton toMouseButton(int awtButton) {
        return switch (awtButton) {
            case MouseEvent.BUTTON1 -> MouseButton.LEFT;
            case MouseEvent.BUTTON2 -> MouseButton.MIDDLE;
            case MouseEvent.BUTTON3 -> MouseButton.RIGHT;
            default -> null;
        };
    }

    private int index(MouseButton button) {
        if (button == null) {
            throw new IllegalArgumentException("button must not be null.");
        }
        return button.ordinal();
    }
}
