import javax.swing.JFrame;

public class GameFrame {
    private final JFrame frame = new JFrame();

    public GameFrame(GameSettings set) {
        frame.setTitle(set.getTitle());
        frame.setSize(set.getWidth(), set.getHeight());

        if (set.isExitOnClose()) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
        
        if (set.isCenterOnScreen()) {
            frame.setLocationRelativeTo(null);
        }

        frame.setVisible(set.isVisible());
    }
}
