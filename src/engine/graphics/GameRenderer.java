package engine.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.util.List;

import engine.object.GameObject;

public class GameRenderer {
    private final Canvas canvas;
    private final BufferStrategy bs;
    private final RendererConfig config;

    public GameRenderer(Canvas canvas, RendererConfig config) {
        if (canvas == null) {
            throw new IllegalArgumentException("canvas must not be null.");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        this.canvas = canvas;
        this.config = config;
        this.canvas.createBufferStrategy(3);
        this.bs = this.canvas.getBufferStrategy();
    }

    public void render(List<GameObject> objects, double alpha) {
        Graphics g = bs.getDrawGraphics();

        try {
            Graphics2D g2 = (Graphics2D) g;
            clearScreen(g2);
            applyRenderingHints(g2);

            for (GameObject o : objects) {
                o.onDraw(g2, alpha);
            }

            if (config.isDebugRender()) {
                renderDebug(g2);
            }
        } finally {
            g.dispose();
        }
        bs.show();
    }

    private void renderDebug(Graphics2D g) {
    }

    private void clearScreen(Graphics2D g) {
        g.setColor(config.getBackgroundColor());
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void applyRenderingHints(Graphics2D g) {
        Object antiAliasingValue = config.isAntiAliasing() ? RenderingHints.VALUE_ANTIALIAS_ON
                : RenderingHints.VALUE_ANTIALIAS_OFF;
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, antiAliasingValue);
    }
}
