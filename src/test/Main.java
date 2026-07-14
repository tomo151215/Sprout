package test;

import java.util.List;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import engine.core.GameLoop;
import engine.core.GameSettings;
import engine.graphics.GameRenderer;
import engine.object.GameObject;
import engine.window.GameWindow;

public class Main {
    public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(set);
            window.show();
            GameRenderer renderer = new GameRenderer(window.getCanvas());
            List<GameObject> r = new ArrayList<>();
            List<GameObject> u = new ArrayList<>();
            MovingBox box = new MovingBox(0, 200, 200, 300);
            r.add(box);
            u.add(box);

            GameLoop gameLoop = new GameLoop(200, renderer, r, u);

            gameLoop.start();
        });
    }
}
