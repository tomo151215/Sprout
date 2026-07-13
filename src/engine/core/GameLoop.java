package engine.core;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

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
        final int maxUpdatesPerFrame = 5;

        long lastTime = System.nanoTime();
        double accumulator = 0.0;

        while (running) {
            int updateCount = 0;
            long now = System.nanoTime();
            long elapsed = now - lastTime;
            lastTime = now;
            accumulator += elapsed;
            while (accumulator >= nsPerUpdate && updateCount < maxUpdatesPerFrame) {
                update();
                accumulator -= nsPerUpdate;
                updateCount++;
            }

            if (updateCount == maxUpdatesPerFrame) {
                accumulator = 0.0;
            }

            double alpha = accumulator / nsPerUpdate;
            render(alpha);
            sleep();
        }
    }

    private void sleep() {
        LockSupport.parkNanos(1_000_000);
    }

    private void update() {
        for (Updatable u : updatables) {
            u.update();
        }
    }

    private void render(double alpha) {
        renderer.render(renderables, alpha);
    }
}
