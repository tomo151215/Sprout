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
        onStart();
    }

    public final void update() {
        if (!started) {
            return;
        }
        onStart();
    }

    public final void end() {
        if (!started) {
            return;
        }
        onEnd();
        started = false;
    }

    public abstract void onStart();

    public void onUpdate() {
    };

    public void onEnd() {
    };

    protected final GameEngine engine() {
        if (engine == null) {
            throw new IllegalStateException(
                    "Scene is not attached to an engine.");
        }
        return engine;
    }

    final void attach(GameEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("engine must not be null.");
        }
        this.engine = engine;
    }

    public final boolean isStarted() {
        return started;
    }
}
