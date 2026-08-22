package engine.loop;

import java.util.Objects;
import java.util.concurrent.locks.LockSupport;
import java.util.function.DoubleConsumer;

public final class GameLoop implements Runnable {
    private static final long NANOSECONDS_PER_SECOND = 1_000_000_000L;
    private static final int MAX_UPDATES_PER_FRAME = 5;
    private static final String THREAD_NAME = "game-loop";

    private final long nanosecondsPerUpdate;
    private final long nanosecondsPerFrame;
    private final Runnable updateAction;
    private final DoubleConsumer renderAction;

    private volatile boolean running;
    private Thread loopThread;

    public GameLoop(int targetUps, int targetFps, Runnable updateAction, DoubleConsumer renderAction) {
        if (targetUps <= 0) {
            throw new IllegalArgumentException("targetUps must be greater than 0.");
        }

        this.nanosecondsPerUpdate = NANOSECONDS_PER_SECOND / targetUps;
        this.nanosecondsPerFrame = targetFps > 0 ? NANOSECONDS_PER_SECOND / targetFps : 0L;
        this.updateAction = Objects.requireNonNull(updateAction, "updateAction must not be null.");
        this.renderAction = Objects.requireNonNull(renderAction, "renderAction must not be null.");
    }

    public synchronized void start() {
        if (running || isLoopThreadAlive()) {
            return;
        }

        running = true;
        loopThread = new Thread(this, THREAD_NAME);
        loopThread.start();
    }

    public synchronized void stop() {
        running = false;

        if (!shouldWaitForLoopThread()) {
            return;
        }

        waitForLoopThread();
    }

    @Override
    public void run() {
        try {
            runLoop();
        } finally {
            running = false;
        }
    }

    private void runLoop() {
        long lastTime = System.nanoTime();
        long accumulator = 0L;

        while (running) {
            long now = System.nanoTime();
            long frameTime = now - lastTime;
            lastTime = now;

            accumulator += frameTime;

            boolean isRunning = running;
            int updateCount = 0;

            while (isRunning && accumulator >= nanosecondsPerUpdate && updateCount < MAX_UPDATES_PER_FRAME) {
                updateAction.run();
                accumulator -= nanosecondsPerUpdate;
                updateCount++;
                isRunning = running;
            }

            if (updateCount == MAX_UPDATES_PER_FRAME && accumulator >= nanosecondsPerUpdate) {
                accumulator %= nanosecondsPerUpdate;
            }

            if (!isRunning) {
                return;
            }

            double alpha = (double) accumulator / nanosecondsPerUpdate;
            renderAction.accept(alpha);

            syncFrame(now);
        }
    }

    private void syncFrame(long frameStartTime) {
        if (nanosecondsPerFrame <= 0) {
            Thread.yield();
            return;
        }

        long targetTime = frameStartTime + nanosecondsPerFrame;

        while (true) {
            long now = System.nanoTime();
            long remaining = targetTime - now;

            if (remaining <= 0) {
                break;
            }

            if (remaining > 2_000_000L) {
                LockSupport.parkNanos(1_000_000L);
            } else if (remaining > 10_000L) {
                Thread.yield();
            } else {
                Thread.onSpinWait();
            }
        }
    }

    private boolean isLoopThreadAlive() {
        return loopThread != null && loopThread.isAlive();
    }

    private boolean shouldWaitForLoopThread() {
        return loopThread != null && Thread.currentThread() != loopThread;
    }

    private void waitForLoopThread() {
        try {
            loopThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
