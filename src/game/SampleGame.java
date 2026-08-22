package game;

import java.awt.Color;
import java.awt.event.KeyEvent;

import engine.asset.ImageLoader;
import engine.asset.Sprite;
import engine.core.GameApplication;
import engine.core.GameSettings;
import engine.graphics.RendererConfig;
import engine.graphics.camera.CameraBounds;
import engine.graphics.camera.CameraController;
import engine.graphics.camera.CameraLookAhead;
import engine.input.InputContext;

public final class SampleGame extends GameApplication {
    private final int TARGET_UPS = 100;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int WORLD_WIDTH = 2400;
    private static final int WORLD_HEIGHT = 1600;
    private InputContext<Action> input;

    @Override
    protected GameSettings createSettings() {
        return GameSettings.builder().size(WIDTH, HEIGHT).title("Sprout Mini World").resizable(false)
                .centerOnScreen(true).build();
    }

    @Override
    protected RendererConfig createRendererConfig() {
        return RendererConfig.builder().backgroundColor(Color.WHITE).antiAliasing(true).interpolation(true).build();
    }

    @Override
    protected void onInit() {
        configureInput();
        Sprite playerSprite = ImageLoader.loadSprite("src/game/assets/player.png").resizedByWidth(80);
        SpriteObject playerObject = new SpriteObject(200.0, 100.0, input, playerSprite);
        engine().addObject(playerObject);
        addWorldObjects();
        configureCamera(playerObject);
    }

    private void configureInput() {
        input = engine().createInputContext(Action.class);
        input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_A);
        input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_LEFT);
        input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_D);
        input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_RIGHT);
        input.addMapping(Action.MOVE_UP, KeyEvent.VK_W);
        input.addMapping(Action.MOVE_UP, KeyEvent.VK_UP);
        input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_S);
        input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_DOWN);
        input.addMapping(Action.JUMP, KeyEvent.VK_SPACE);
    }

    private void addWorldObjects() {
        engine().addObject(new WorldObject(100, 100, Color.RED));
        engine().addObject(new WorldObject(700, 300, Color.BLUE));
        engine().addObject(new WorldObject(1200, 700, Color.ORANGE));
        engine().addObject(new WorldObject(1800, 400, Color.MAGENTA));
        engine().addObject(new WorldObject(2100, 1200, Color.CYAN));
        engine().addObject(new WorldObject(500, 1300, Color.PINK));
    }

    private void configureCamera(SpriteObject sprite) {
        CameraController cameraController = new CameraController(engine().getCamera(), sprite, WIDTH, HEIGHT);
        cameraController.setLookAhead(new CameraLookAhead(120, 80, 0.1));
        cameraController.setBounds(new CameraBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT));
        engine().addSystem(cameraController);
    }

    @Override
    protected int targetUps() {
        return TARGET_UPS;
    }

}
