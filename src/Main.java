import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            new GameWindow(set).show();
        });
    }
}
