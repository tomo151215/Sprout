package sample;

import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;
import engine.core.GameEngine;
import engine.core.GameSettings;
import engine.input.InputManager;

public class Main {
    public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            GameEngine engine = new GameEngine(set, 120);
            InputManager<Action> input = new InputManager<>(engine.getKeyboard(), Action.class);

            input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_LEFT);
            input.addMapping(Action.MOVE_UP, KeyEvent.VK_UP);
            input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_RIGHT);
            input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_DOWN);

            engine.addObject(new Block(input, 100, 200, 2));
            engine.start();
        });
    }
}
