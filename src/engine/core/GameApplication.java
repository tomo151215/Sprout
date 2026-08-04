package engine.core;

import engine.graphics.RendererConfig;

public abstract class GameApplication {
    private boolean running;
    private GameEngine engine;
    private final int DEFAULT_UPS = 60;

    protected abstract GameSettings createSettings();

    protected abstract RendererConfig createRendererConfig();

    protected int targetUps() {
        return DEFAULT_UPS;
    }

    protected void onInit() {
    }

    protected void onShutdown() {
    }

    protected final GameEngine engine() {
        if (engine == null) {
            throw new IllegalStateException("Engine is not initialized yet.");
        }
        return engine;
    }

    public final void run() {
        if (running) {
            return;
        }
        GameSettings settings = createSettings();
        RendererConfig config = createRendererConfig();
        this.engine = new GameEngine(settings, targetUps(), config);
        onInit();
        engine.start();
        running = true;
    }

    public final void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (engine != null) {
            engine.stop();
        }
        onShutdown();
    }
}
