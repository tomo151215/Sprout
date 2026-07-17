package engine.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {

    private static final int KEY_COUNT = 512;
    private final boolean[] pressed = new boolean[KEY_COUNT];

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (isWithinBounds(code)) {
            pressed[code] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (isWithinBounds(code)) {
            pressed[code] = false;
        }
    }

    public boolean isPresed(int keyCode) {
        return isWithinBounds(keyCode) && pressed[keyCode];
    }

    private boolean isWithinBounds(int keyCode) {
        return keyCode >= 0 && keyCode < pressed.length;
    }

}
