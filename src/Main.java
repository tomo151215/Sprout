import java.util.List;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(set);
            window.show();
            GameRenderer renderer = new GameRenderer(window.getCanvas());
            List<Renderable> r = new ArrayList<>();
            List<Updatable> u = new ArrayList<>();
            MovingBox box = new MovingBox();
            r.add(box);
            u.add(box);
            GameLoop loop = new GameLoop(100, renderer, r, u);
            loop.start();
        });
    }
}
