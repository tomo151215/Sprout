package engine.core;

import java.util.Objects;

import engine.audio.AudioManager;
import engine.graphics.GameRenderer;
import engine.graphics.RendererConfig;
import engine.graphics.camera.Camera2D;
import engine.input.InputContext;
import engine.input.Keyboard;
import engine.input.Mouse;
import engine.loop.GameLoop;
import engine.object.GameObject;
import engine.scene.SceneManager;
import engine.system.GameSystem;
import engine.window.GameWindow;

public final class GameEngine {
    private final GameSettings settings;
    private final Keyboard keyboard;
    private final Mouse mouse;
    private final GameWindow window;
    private final GameRenderer renderer;
    private final GameLoop loop;

    private final ComponentRegistry<GameObject> objects = new ComponentRegistry<>();
    private final ComponentRegistry<GameSystem> systems = new ComponentRegistry<>();

    private final SceneManager sceneManager;
    private final AudioManager audioManager;

    private Runnable stopRequestHandler;
    private boolean started;
    private boolean closed;

    public GameEngine(GameSettings settings, int targetUps, RendererConfig rendererConfig) {
        this.settings = Objects.requireNonNull(settings, "settings must not be null.");
        Objects.requireNonNull(rendererConfig, "rendererConfig must not be null.");

        this.keyboard = new Keyboard();
        this.mouse = new Mouse();
        this.stopRequestHandler = this::stop;

        this.window = new GameWindow(settings, keyboard, mouse, this::requestStop);
        this.renderer = new GameRenderer(window.getCanvas(), rendererConfig);
        this.loop = new GameLoop(targetUps, this::update, this::render);

        this.sceneManager = new SceneManager(this);
        this.audioManager = new AudioManager();

        systems.add(sceneManager);
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        if (closed) {
            throw new IllegalStateException("A stopped GameEngine cannot be restarted.");
        }

        try {
            window.open();
            loop.start();
            started = true;
        } catch (RuntimeException | Error e) {
            try {
                stop();
            } catch (RuntimeException | Error closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
    }

    public synchronized void stop() {
        if (closed) {
            return;
        }

        closed = true;
        started = false;
        loop.stop();

        try {
            audioManager.close();
        } finally {
            window.close();
        }
    }

    public void addObject(GameObject object) {
        objects.add(object);
    }

    public void removeObject(GameObject object) {
        objects.remove(object);
    }

    public void addSystem(GameSystem system) {
        systems.add(system);
    }

    public void removeSystem(GameSystem system) {
        systems.remove(system);
    }

    public <T extends Enum<T>> InputContext<T> createInputContext(Class<T> actionClass) {
        return new InputContext<>(keyboard, mouse, actionClass);
    }

    public GameSettings getSettings() {
        return settings;
    }

    public Camera2D getCamera() {
        return renderer.getCamera();
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    void setStopRequestHandler(Runnable stopRequestHandler) {
        this.stopRequestHandler = Objects.requireNonNull(
                stopRequestHandler,
                "stopRequestHandler must not be null.");
    }

    private void requestStop() {
        stopRequestHandler.run();
    }

    private void update() {
        updateInput();
        updateObjects();
        updateSystems();
    }

    private void updateInput() {
        keyboard.updateSnapshot();
        mouse.updateSnapshot();
    }

    private void updateObjects() {
        objects.forEachComponent(GameObject::update);
    }

    private void updateSystems() {
        systems.forEachComponent(GameSystem::update);
    }

    private void render(double alpha) {
        renderer.render(objects, alpha);
    }
}
