package engine.core;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

import engine.graphics.GameRenderer;
import engine.input.Keyboard; 
import engine.object.GameObject;


public final class GameLoop implements Runnable {
    private final int targetUps;
    private final GameRenderer renderer;
    private final List<GameObject> renderObjects;
    private final List<GameObject> updateObjects;
    private final Keyboard keyboard; 
    private Thread th;

    private volatile boolean running;

    public GameLoop(int targetUps, GameRenderer renderer, List<GameObject> renderObjects, List<GameObject> updateObjects, Keyboard keyboard) {
        this.targetUps = targetUps;
        this.renderer = renderer;
        this.renderObjects = renderObjects;
        this.updateObjects = updateObjects;
        this.keyboard = keyboard;
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

        if (th != null && Thread.currentThread() != th) {
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
        keyboard.updateSnapshot();

        for (GameObject u : updateObjects) {
            u.onUpdate();
        }
    }

    private void render(double alpha) {
        renderer.render(renderObjects, alpha);
    }
}