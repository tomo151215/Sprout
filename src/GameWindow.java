import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;

public final class GameWindow {
    private final JFrame frame = new JFrame();
    private final Canvas canvas = new Canvas();
    private final GameSettings set;

    public GameWindow(GameSettings set) {
        this.set = set;

        // Canvasの準備
        canvas.setPreferredSize(new Dimension(set.getWidth(), set.getHeight()));
        canvas.setFocusable(true);

        // ウィンドウの基本設定
        frame.setTitle(set.getTitle());
        frame.setResizable(set.isResizable());

        if (set.isExitOnClose()) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }

        // コンポーネントの配置
        frame.add(canvas);
        frame.pack();

        // サイズ確定後に中央寄せ
        if (set.isCenterOnScreen()) {
            frame.setLocationRelativeTo(null);
        }
    }

    public void show() {
        frame.setVisible(set.isVisible());
        if (set.isVisible()) {
            canvas.requestFocusInWindow();
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
