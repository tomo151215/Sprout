package engine.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.BitSet;

public final class Keyboard extends KeyAdapter {
    private final BitSet currentPressed = new BitSet();
    private final BitSet pressed = new BitSet();
    private final BitSet previousPressed = new BitSet();

    private final Object lock = new Object();

    public void updateSnapshot() {
        synchronized (lock) {
            copy(pressed, previousPressed);
            copy(currentPressed, pressed);
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        setCurrentState(event.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        setCurrentState(event.getKeyCode(), false);
    }

    public void clear() {
        synchronized (lock) {
            currentPressed.clear();
            pressed.clear();
            previousPressed.clear();
        }
    }

    public boolean isPressed(int keyCode) {
        if (keyCode < 0) {
            return false;
        }

        synchronized (lock) {
            return pressed.get(keyCode);
        }
    }

    public boolean isJustPressed(int keyCode) {
        if (keyCode < 0) {
            return false;
        }

        synchronized (lock) {
            return pressed.get(keyCode) && !previousPressed.get(keyCode);
        }
    }

    public boolean isJustReleased(int keyCode) {
        if (keyCode < 0) {
            return false;
        }

        synchronized (lock) {
            return !pressed.get(keyCode) && previousPressed.get(keyCode);
        }
    }

    private void setCurrentState(int keyCode, boolean isPressed) {
        if (keyCode < 0) {
            return;
        }

        synchronized (lock) {
            currentPressed.set(keyCode, isPressed);
        }
    }

    private void copy(BitSet source, BitSet destination) {
        destination.clear();
        destination.or(source);
    }
}
