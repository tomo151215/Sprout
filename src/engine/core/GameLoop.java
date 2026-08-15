package engine.core;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

import engine.graphics.GameRenderer;
import engine.input.Keyboard;
import engine.input.Mouse;
import engine.object.GameObject;

public final class GameLoop implements Runnable {
    private final int targetUps;
    private final GameRenderer renderer;
    private final List<GameObject> renderObjects;
    private final List<GameObject> updateObjects;
    private final List<GameSystem> systems;
    private final Keyboard keyboard;
    private final Mouse mouse;
    private Thread th;

    private volatile boolean running;

    public GameLoop(int targetUps, GameRenderer renderer, List<GameObject> renderObjects,
            List<GameObject> updateObjects, List<GameSystem> systems, Keyboard keyboard, Mouse mouse) {
        if (targetUps <= 0) {
            throw new IllegalArgumentException(
                    "targetUps must be greater than 0.");
        }

        this.targetUps = targetUps;
        this.renderer = renderer;
        this.renderObjects = renderObjects;
        this.updateObjects = updateObjects;
        this.systems = systems;
        this.keyboard = keyboard;
        this.mouse = mouse;
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
        mouse.updateSnapshot();
        for (GameObject u : updateObjects) {
            u.onUpdate();
        }
        for (GameSystem system : systems) {
            system.update();
        }
    }

    private void render(double alpha) {
        renderer.render(renderObjects, alpha);
    }
}