package test;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import engine.core.GameLoop;
import engine.core.GameSettings;
import engine.graphics.GameRenderer;
import engine.input.InputManager;
import engine.input.Keyboard;
import engine.object.GameObject;
import engine.window.GameWindow;

public class Main {
    public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            Keyboard k = new Keyboard();
            InputManager<Action> input = new InputManager<>(k, Action.class);
            input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_LEFT);
            input.addMapping(Action.MOVE_UP, KeyEvent.VK_UP);
            input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_RIGHT);
            input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_DOWN);

            GameWindow window = new GameWindow(set, k);
            window.show();
            GameRenderer renderer = new GameRenderer(window.getCanvas());
            Block block = new Block(input, 100, 50, 2);
            List<GameObject> r = new ArrayList<>();
            List<GameObject> u = new ArrayList<>();
            r.add(block);
            u.add(block);
            GameLoop loop = new GameLoop(120, renderer, r, u, k);
            loop.start();
        });
    }
}
