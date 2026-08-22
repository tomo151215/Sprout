package engine.graphics;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;

import engine.graphics.camera.Camera2D;

public final class GameRenderer {
    private static final int BUFFER_COUNT = 3;

    private final Canvas canvas;
    private final BufferStrategy bufferStrategy;
    private final RendererConfig config;
    private final Camera2D camera;

    public GameRenderer(Canvas canvas, RendererConfig config) {
        if (canvas == null) {
            throw new IllegalArgumentException("canvas must not be null.");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }

        this.canvas = canvas;
        this.config = config;
        this.camera = new Camera2D();

        canvas.createBufferStrategy(BUFFER_COUNT);
        this.bufferStrategy = canvas.getBufferStrategy();

        if (bufferStrategy == null) {
            throw new IllegalStateException("Failed to create BufferStrategy.");
        }
    }

    public void render(Iterable<? extends Renderable> renderables, double alpha) {
        if (renderables == null) {
            throw new IllegalArgumentException("renderables must not be null.");
        }

        do {
            drawUntilBufferIsStable(renderables, alpha);
            bufferStrategy.show();
        } while (bufferStrategy.contentsLost());
    }

    public Camera2D getCamera() {
        return camera;
    }

    private void drawUntilBufferIsStable(Iterable<? extends Renderable> renderables, double alpha) {
        do {
            drawFrame(renderables, alpha);
        } while (bufferStrategy.contentsRestored());
    }

    private void drawFrame(Iterable<? extends Renderable> renderables, double alpha) {
        Graphics2D graphics = (Graphics2D) bufferStrategy.getDrawGraphics();

        try {
            clearScreen(graphics);
            applyRenderingHints(graphics);
            renderWorld(graphics, renderables, renderAlpha(alpha));
        } finally {
            graphics.dispose();
        }
    }

    private double renderAlpha(double alpha) {
        return config.isInterpolation() ? alpha : 1.0;
    }

    private void clearScreen(Graphics2D graphics) {
        graphics.setColor(config.getBackgroundColor());
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void applyRenderingHints(Graphics2D graphics) {
        Object antiAliasingValue = config.isAntiAliasing()
                ? RenderingHints.VALUE_ANTIALIAS_ON
                : RenderingHints.VALUE_ANTIALIAS_OFF;

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasingValue);
    }

    private void renderWorld(
            Graphics2D graphics,
            Iterable<? extends Renderable> renderables,
            double alpha) {

        Graphics2D worldGraphics = (Graphics2D) graphics.create();

        try {
            camera.apply(worldGraphics, alpha);
            for (Renderable renderable : renderables) {
                drawRenderable(worldGraphics, renderable, alpha);
            }
        } finally {
            worldGraphics.dispose();
        }
    }

    private void drawRenderable(Graphics2D worldGraphics, Renderable renderable, double alpha) {
        Graphics2D objectGraphics = (Graphics2D) worldGraphics.create();

        try {
            renderable.draw(objectGraphics, alpha);
        } finally {
            objectGraphics.dispose();
        }
    }
}
