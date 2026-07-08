import java.awt.Canvas;
import java.awt.Graphics;
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

    public void render(List<Renderable> renderables) {
        Graphics g = bs.getDrawGraphics();
        try {
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            // 描画内容
            for (Renderable r : renderables) {
                r.draw(g);
            }
        } finally {
            // リソース解放
            g.dispose();
        }

        // フリッピング(画面反映)
        bs.show();
    }
}
