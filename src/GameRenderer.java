import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.util.List;

public class GameRenderer {
    private final Canvas canvas;
    private final BufferStrategy bs;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.canvas.createBufferStrategy(3);
        this.bs = this.canvas.getBufferStrategy();
    }

    public void render(List<Renderable> renderables, double alpha) {
        Graphics g = bs.getDrawGraphics();
        try {
            g.setColor(Color.WHITE);  // 背景クリアの色指定
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            // 描画内容
            for (Renderable r : renderables) {
                r.draw(g, alpha);
            }
        } finally {
            // リソース解放
            g.dispose();
        }

        // フリッピング(画面反映)
        bs.show();
        Toolkit.getDefaultToolkit().sync(); // OSの描画キューと同期させてカクつき防止
    }
}
