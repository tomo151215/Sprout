package engine.loop;

import java.util.Objects;
import java.util.concurrent.locks.LockSupport;
import java.util.function.DoubleConsumer;

public final class GameLoop implements Runnable {
    private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0;
    private static final int MAX_UPDATES_PER_FRAME = 5;
    private static final long IDLE_PARK_NANOS = 1_000_000L;
    private static final String THREAD_NAME = "game-loop";

    private final double nanosecondsPerUpdate;
    private final Runnable updateAction;
    private final DoubleConsumer renderAction;

    private volatile boolean running;
    private Thread loopThread;

    public GameLoop(int targetUps, Runnable updateAction, DoubleConsumer renderAction) {
        if (targetUps <= 0) {
            throw new IllegalArgumentException("targetUps must be greater than 0.");
        }

        this.nanosecondsPerUpdate = NANOSECONDS_PER_SECOND / targetUps;
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
        double accumulator = 0.0;

        while (running) {
            long now = System.nanoTime();
            accumulator += now - lastTime;
            lastTime = now;

            accumulator = runPendingUpdates(accumulator);

            if (!running) {
                return;
            }

            renderAction.accept(accumulator / nanosecondsPerUpdate);
            LockSupport.parkNanos(IDLE_PARK_NANOS);
        }
    }

    private double runPendingUpdates(double accumulator) {
        int updateCount = 0;

        while (shouldUpdate(accumulator, updateCount)) {
            updateAction.run();
            accumulator -= nanosecondsPerUpdate;
            updateCount++;
        }

        return discardExcessLag(accumulator, updateCount);
    }

    private boolean shouldUpdate(double accumulator, int updateCount) {
        return running
                && accumulator >= nanosecondsPerUpdate
                && updateCount < MAX_UPDATES_PER_FRAME;
    }

    private double discardExcessLag(double accumulator, int updateCount) {
        boolean updateLimitReached = updateCount == MAX_UPDATES_PER_FRAME;
        boolean atLeastOneUpdateStillPending = accumulator >= nanosecondsPerUpdate;

        if (updateLimitReached && atLeastOneUpdateStillPending) {
            return accumulator % nanosecondsPerUpdate;
        }

        return accumulator;
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
