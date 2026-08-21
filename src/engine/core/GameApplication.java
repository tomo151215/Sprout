package engine.core;

import engine.graphics.RendererConfig;

public abstract class GameApplication {
    private static final int DEFAULT_UPDATES_PER_SECOND = 60;

    private GameEngine engine;
    private boolean running;

    protected abstract GameSettings createSettings();

    protected RendererConfig createRendererConfig() {
        return RendererConfig.builder().build();
    }

    protected int targetUps() {
        return DEFAULT_UPDATES_PER_SECOND;
    }

    protected void onInit() {
    }

    protected void onShutdown() {
    }

    protected final GameEngine engine() {
        if (engine == null) {
            throw new IllegalStateException("Engine is not initialized.");
        }
        return engine;
    }

    public final synchronized void run() {
        if (running) {
            return;
        }

        GameEngine newEngine = createEngine();
        engine = newEngine;
        newEngine.setStopRequestHandler(this::stop);

        try {
            onInit();
            newEngine.start();
            running = true;
        } catch (RuntimeException | Error e) {
            closeAfterFailedStart(newEngine, e);
            engine = null;
            throw e;
        }
    }

    public final synchronized void stop() {
        if (!running) {
            return;
        }

        running = false;
        GameEngine currentEngine = engine;

        try {
            if (currentEngine != null) {
                currentEngine.stop();
            }
        } finally {
            try {
                onShutdown();
            } finally {
                engine = null;
            }
        }
    }

    private GameEngine createEngine() {
        GameSettings settings = createSettings();
        if (settings == null) {
            throw new IllegalStateException("createSettings() must not return null.");
        }

        RendererConfig rendererConfig = createRendererConfig();
        if (rendererConfig == null) {
            throw new IllegalStateException("createRendererConfig() must not return null.");
        }

        return new GameEngine(settings, targetUps(), rendererConfig);
    }

    private void closeAfterFailedStart(GameEngine engine, Throwable originalError) {
        try {
            engine.stop();
        } catch (RuntimeException | Error closeError) {
            originalError.addSuppressed(closeError);
        }
    }
}
