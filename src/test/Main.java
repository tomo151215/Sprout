package test;

import java.util.List;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import engine.core.GameLoop;
import engine.core.GameSettings;
import engine.graphics.GameRenderer;
import engine.graphics.Renderable;
import engine.update.Updatable;
import engine.window.GameWindow;

public class Main {
    public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(set);
            window.show();
            GameRenderer renderer = new GameRenderer(window.getCanvas());
            List<Renderable> r = new ArrayList<>();
            List<Updatable> u = new ArrayList<>();
            MovingBox box = new MovingBox(0, 0, 100, 100);
            MovingBox box2 = new MovingBox(100, 200, 100, 100);
            r.add(box);
            r.add(box2);
            u.add(box);
            u.add(box2);

            GameLoop gameLoop = new GameLoop(300, renderer, r, u);

            gameLoop.start();
        });
    }
}
