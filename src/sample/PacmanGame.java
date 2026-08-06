package sample;

import java.awt.Color;
import java.awt.event.KeyEvent;

import engine.core.GameApplication;
import engine.core.GameSettings;
import engine.graphics.RendererConfig;
import engine.input.InputContext;

public class PacmanGame extends GameApplication {
    private InputContext<Action> input;

    @Override
    protected GameSettings createSettings() {
        return GameSettings.builder()
                .size(PacmanWorld.WIDTH, PacmanWorld.HEIGHT)
                .title("Sprout Pacman Sample")
                .isResizeable(false)
                .build();
    }

    @Override
    protected RendererConfig createRendererConfig() {
        return RendererConfig.builder()
                .backgroundColor(Color.BLACK)
                .isAntiAliasing(true)
                .isDebugRender(false)
                .isInterpolation(true)
                .build();
    }

    @Override
    protected int targetUps() {
        return 60;
    }

    @Override
    protected void onInit() {
        input = new InputContext<>(
                engine().getKeyboard(),
                engine().getMouse(),
                Action.class
        );

        input.addMapping(Action.UP, KeyEvent.VK_UP);
        input.addMapping(Action.UP, KeyEvent.VK_W);

        input.addMapping(Action.DOWN, KeyEvent.VK_DOWN);
        input.addMapping(Action.DOWN, KeyEvent.VK_S);

        input.addMapping(Action.LEFT, KeyEvent.VK_LEFT);
        input.addMapping(Action.LEFT, KeyEvent.VK_A);

        input.addMapping(Action.RIGHT, KeyEvent.VK_RIGHT);
        input.addMapping(Action.RIGHT, KeyEvent.VK_D);

        input.addMapping(Action.RESTART, KeyEvent.VK_R);

        engine().getCamera().setPosition(0, 0);
        engine().addObject(new PacmanWorld(input));
    }
}