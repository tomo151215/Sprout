package sample;

import java.awt.Color;
import java.awt.event.KeyEvent;

import engine.core.GameApplication;
import engine.core.GameSettings;
import engine.graphics.CameraBounds;
import engine.graphics.CameraController;
import engine.graphics.CameraLookAhead;
import engine.graphics.RendererConfig;
import engine.input.InputContext;
import sample.Action;
import sample.GameWorld;

public final class MyGame extends GameApplication {
    @Override
    protected GameSettings createSettings() {
        return GameSettings.builder()
                .size(800, 600)
                .title("Sprout Dash")
                .isVisible(true)
                .centerOnScreen(true)
                .exitOnClose(true)
                .isResizable(false)
                .build();
    }

    @Override
    protected RendererConfig createRendererConfig() {
        return RendererConfig.builder()
                .backgroundColor(new Color(25, 30, 38))
                .isAntiAliasing(true)
                .isInterpolation(true)
                .isDebugRender(false)
                .build();
    }

    @Override
    protected void onInit() {
        InputContext<Action> input = new InputContext<>(
                engine().getKeyboard(),
                engine().getMouse(),
                Action.class
        );

        input.addMapping(Action.LEFT, KeyEvent.VK_LEFT);
        input.addMapping(Action.LEFT, KeyEvent.VK_A);

        input.addMapping(Action.RIGHT, KeyEvent.VK_RIGHT);
        input.addMapping(Action.RIGHT, KeyEvent.VK_D);

        input.addMapping(Action.UP, KeyEvent.VK_UP);
        input.addMapping(Action.UP, KeyEvent.VK_W);

        input.addMapping(Action.DOWN, KeyEvent.VK_DOWN);
        input.addMapping(Action.DOWN, KeyEvent.VK_S);

        input.addMapping(Action.DASH, KeyEvent.VK_SPACE);
        input.addMapping(Action.RESTART, KeyEvent.VK_R);

        GameWorld world = new GameWorld(engine(), input);
        world.register();

        CameraController cameraController = new CameraController(
                engine().getCamera(),
                world.getPlayer(),
                engine().getSettings().getWidth(),
                engine().getSettings().getHeight()
        );

        cameraController.setBounds(
                new CameraBounds(
                        0,
                        0,
                        world.getWorldWidth(),
                        world.getWorldHeight()
                )
        );

        cameraController.setLookAhead(
                new CameraLookAhead(
                        80,
                        40,
                        0.12
                )
        );

        engine().addSystem(world);
        engine().addSystem(cameraController);
    }
}
