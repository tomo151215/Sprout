package engine.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {

    private static final int KEY_COUNT = 512;
    private final boolean[] currentPressed = new boolean[KEY_COUNT];
    private final boolean[] pressed = new boolean[KEY_COUNT];
    private final boolean[] previousPressed = new boolean[KEY_COUNT];

    private final Object lock = new Object();

    public void updateSnapshot() {
        System.arraycopy(
                pressed, 
                0,
                previousPressed, 
                0,
                KEY_COUNT
            );

        synchronized (lock) {
            System.arraycopy(
                    currentPressed, 
                    0,
                    pressed, 
                    0,
                    KEY_COUNT
                );
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (isWithinBounds(code)) {
            synchronized (lock) {
                currentPressed[code] = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (isWithinBounds(code)) {
            synchronized (lock) {
                currentPressed[code] = false;
            }
        }
    }

    public boolean isPressed(int keyCode) {
        return isWithinBounds(keyCode) && pressed[keyCode];
    }

    private boolean isWithinBounds(int keyCode) {
        return keyCode >= 0 && keyCode < KEY_COUNT;
    }

    public boolean isJustPressed(int keyCode) {
        return isPressed(keyCode) && !previousPressed[keyCode];
    }

    public boolean isJustReleased(int keyCode) {
        return !isPressed(keyCode) && previousPressed[keyCode];
    }
}