package engine.scene;

import engine.core.GameEngine;

public abstract class Scene {
    private GameEngine engine;
    private boolean started;

    public final void start() {
        if (started) {
            return;
        }

        started = true;
        try {
            onStart();
        } catch (RuntimeException | Error e) {
            started = false;
            throw e;
        }
    }

    public final void update() {
        if (started) {
            onUpdate();
        }
    }

    public final void end() {
        if (!started) {
            return;
        }

        try {
            onEnd();
        } finally {
            started = false;
        }
    }

    public final boolean isStarted() {
        return started;
    }

    protected abstract void onStart();

    protected void onUpdate() {
    }

    protected void onEnd() {
    }

    protected final GameEngine engine() {
        if (engine == null) {
            throw new IllegalStateException("Scene is not attached to an engine.");
        }
        return engine;
    }

    final void attach(GameEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("engine must not be null.");
        }
        if (this.engine != null && this.engine != engine) {
            throw new IllegalStateException("Scene is already attached to another engine.");
        }

        this.engine = engine;
    }
}
