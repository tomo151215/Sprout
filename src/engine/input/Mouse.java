package engine.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;

public final class Mouse implements MouseListener, MouseMotionListener, MouseWheelListener {
    private final int BUTTON_COUNT = MouseButton.values().length;

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
            System.arraycopy(
                    pressed,
                    0,
                    previousPressed,
                    0,
                    BUTTON_COUNT);

            x = currentX;
            y = currentY;
            System.arraycopy(
                    currentPressed,
                    0,
                    pressed,
                    0,
                    BUTTON_COUNT);
            wheelRotation = currentWheelRotation;
            currentWheelRotation = 0;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        MouseButton button = toMouseButton(e.getButton());
        if (button != null) {
            synchronized (lock) {
                currentPressed[index(button)] = true;
            }
            updateCurrentPosition(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        MouseButton button = toMouseButton(e.getButton());
        if (button != null) {
            synchronized (lock) {
                currentPressed[index(button)] = false;
            }
            updateCurrentPosition(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateCurrentPosition(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        updateCurrentPosition(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        synchronized (lock) {
            currentWheelRotation += e.getWheelRotation();
        }
        updateCurrentPosition(e);
    }

    private void updateCurrentPosition(MouseEvent e) {
        synchronized (lock) {
            currentX = e.getX();
            currentY = e.getY();
        }
    }

    private MouseButton toMouseButton(int awtButtton) {
        return switch (awtButtton) {
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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPreviousX() {
        return previousX;
    }

    public int getPreviousY() {
        return previousY;
    }

    public int getDeltaX() {
        return x - previousX;
    }

    public int getDeltaY() {
        return y - previousY;
    }

    public boolean isPressed(MouseButton button) {
        int i = index(button);
        synchronized (lock) {
            return pressed[i];
        }
    }

    public boolean isJustPressed(MouseButton button) {
        int i = index(button);
        synchronized (lock) {
            return pressed[i] && !previousPressed[i];
        }
    }

    public boolean isJustReleased(MouseButton button) {
        int i = index(button);
        synchronized (lock) {
            return !pressed[i] && previousPressed[i];
        }
    }

    public int getWheelRotation() {
        return wheelRotation;
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
}
