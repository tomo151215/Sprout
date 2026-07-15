package test;

import javax.swing.SwingUtilities;
import engine.core.GameSettings;
import engine.window.GameWindow;

public class Main {
    public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(set);
            window.show();
        });
    }
}
