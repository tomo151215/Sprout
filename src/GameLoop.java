import java.util.List;

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

            int maxUpdateCount = 5;
            int updateCount = 0;

            while (accumulator >= nsPerUpdate && updateCount < maxUpdateCount) {
                update();
                accumulator -= nsPerUpdate;
                updateCount++;
            }

            if (updateCount == maxUpdateCount) {
                accumulator = 0.0;
            }

            // alphaを計算
            double alpha = accumulator / nsPerUpdate;
            render(alpha);

            // 待っているプロセスがいるならCPU解放、いなければそのまま続行
            Thread.yield();
        }
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
