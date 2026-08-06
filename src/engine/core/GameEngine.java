package engine.core;

import java.util.ArrayList;
import java.util.List;

import engine.graphics.Camera2D;
import engine.graphics.GameRenderer;
import engine.graphics.RendererConfig;
import engine.input.Keyboard;
import engine.input.Mouse;
import engine.object.GameObject;
import engine.window.GameWindow;

public class GameEngine {
    private final GameSettings setttings;
    private final Keyboard keyboard;
    private final Mouse mouse;
    private final GameWindow window;
    private final GameRenderer renderer;
    private final GameLoop loop;
    private final List<GameObject> renderObjects = new ArrayList<>();
    private final List<GameObject> updateObjects = new ArrayList<>();

    public GameEngine(GameSettings settings, int targetUps, RendererConfig rendererConfig) {
        if (settings == null) {
            throw new IllegalArgumentException("settings must not be null.");
        }
        if (rendererConfig == null) {
            throw new IllegalArgumentException("rendererConfig must not be null.");
        }
        this.setttings = settings;
        this.keyboard = new Keyboard();
        this.mouse = new Mouse();
        this.window = new GameWindow(settings, keyboard, mouse);
        this.renderer = new GameRenderer(window.getCanvas(), rendererConfig);
        this.loop = new GameLoop(targetUps, renderer, renderObjects, updateObjects, keyboard, mouse);
    }

    public void start() {
        window.show();
        loop.start();
    }

    public void stop() {
        loop.stop();
    }

    public void addObject(GameObject object) {
        updateObjects.add(object);
        renderObjects.add(object);
    }

    public void removeObject(GameObject object) {
        updateObjects.remove(object);
        renderObjects.remove(object);
    }

    public GameSettings getSetttings() {
        return setttings;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public GameWindow getWindow() {
        return window;
    }

    public GameRenderer getRenderer() {
        return renderer;
    }

    public GameLoop getLoop() {
        return loop;
    }

    public List<GameObject> getRenderObjects() {
        return renderObjects;
    }

    public List<GameObject> getUpdateObjects() {
        return updateObjects;
    }

    public Camera2D getCamera() {
        return renderer.getCamera();
    }

}
