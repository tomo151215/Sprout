
package engine.scene;

import engine.core.GameEngine;
import engine.core.GameSystem;

public class SceneManager implements GameSystem {
    private Scene currentScene;
    private final GameEngine engine;

    public SceneManager(GameEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("engine must nut be null.");
        }
        this.engine = engine;
    }

    @Override
    public void update() {
        if (currentScene == null) {
            return;
        }
        currentScene.update();
    }

    public void setScene(Scene scene) {
        if (scene == null) {
            throw new IllegalArgumentException("scene must not be null.");
        }
        if (currentScene != null) {
            currentScene.end();
        }
        this.currentScene = scene;
        currentScene.attach(engine);
        currentScene.start();
    }

    public void endCurrentScene() {
        if (currentScene == null) {
            return;
        }
        currentScene.end();
        currentScene = null;
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public boolean hasScene() {
        return currentScene != null;
    }
}
