package engine.scene;

import engine.core.GameEngine;
import engine.system.GameSystem;

public final class SceneManager implements GameSystem {
    private final GameEngine engine;
    private Scene currentScene;

    public SceneManager(GameEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("engine must not be null.");
        }
        this.engine = engine;
    }

    @Override
    public void update() {
        if (currentScene != null) {
            currentScene.update();
        }
    }

    public void changeScene(Scene nextScene) {
        if (nextScene == null) {
            throw new IllegalArgumentException("nextScene must not be null.");
        }

        endCurrentScene();
        startScene(nextScene);
    }

    public void endCurrentScene() {
        Scene sceneToEnd = currentScene;
        currentScene = null;

        if (sceneToEnd != null) {
            sceneToEnd.end();
        }
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public boolean hasScene() {
        return currentScene != null;
    }

    private void startScene(Scene scene) {
        scene.attach(engine);
        currentScene = scene;

        try {
            scene.start();
        } catch (RuntimeException | Error e) {
            currentScene = null;
            throw e;
        }
    }
}
