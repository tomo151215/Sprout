package engine.core;

import java.util.ArrayList;
import java.util.List;

import engine.graphics.GameRenderer;
import engine.input.Keyboard;
import engine.object.GameObject;
import engine.window.GameWindow;

public class GameEngine {
    private final GameSettings setttings;
    private final Keyboard keyboard;
    private final GameWindow window;
    private final GameRenderer renderer;
    private final GameLoop loop;
    private final List<GameObject> renderObjects = new ArrayList<>();
    private final List<GameObject> updateObjects = new ArrayList<>();

    public GameEngine(GameSettings settings, int targetUps) {
        this.setttings = settings;
        this.keyboard = new Keyboard();
        this.window = new GameWindow(settings, keyboard);
        this.renderer = new GameRenderer(window.getCanvas());
        this.loop = new GameLoop(targetUps, renderer, renderObjects, updateObjects, keyboard);
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

    
}
