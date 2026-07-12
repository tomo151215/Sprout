package engine.core;
import java.util.List;

import engine.graphics.GameRenderer;
import engine.graphics.Renderable;
import engine.update.Updatable;

public final class GameLoop implements Runnable {
    private final int targetUps;
    private final GameRenderer renderer;
    private final List<Renderable> renderables;
    private final List<Updatable> updatables;
    private Thread th;

    private volatile boolean running;

    public GameLoop(int targetUps, GameRenderer renderer, List<Renderable> renderables, List<Updatable> updatables) {
        this.targetUps = targetUps;
        this.renderer = renderer;
        this.renderables = renderables;
        this.updatables = updatables;
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        th = new Thread(this);
        th.start();
    }

    public synchronized void stop() {
        running = false;

        if (th != null && Thread.currentThread() != th) { // デッドロック回避
            try {
                th.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        final double nsPerUpdate = 1_000_000_000.0 / targetUps;
        long lastTime = System.nanoTime();
        double accumulator = 0.0;

        while (running) {
            long now = System.nanoTime();
            long elapsed = now - lastTime;
            lastTime = now;
            accumulator += elapsed;
            boolean updated = false;

            while (accumulator >= nsPerUpdate) {
                update();
                accumulator -= nsPerUpdate;
                updated = true;
            }

            if (updated) {
                render();
            } else {
                sleep();
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void update() {
        for (Updatable u : updatables) {
            u.update();
        }
    }

    private void render() {
        renderer.render(renderables);
    }
}
