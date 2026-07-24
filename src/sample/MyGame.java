package sample;

import java.awt.event.KeyEvent;

import engine.core.GameApplication;
import engine.core.GameSettings;
import engine.input.InputManager;

public class MyGame extends GameApplication {
    private int UPS = 120;

    @Override
    protected GameSettings createSettings() {
        return GameSettings.builder()
                .size(800,600)
                .title("SampleApp")
                .build();
    }

    @Override
    protected void onInit() {
        InputManager<Action> input = new InputManager<>(engine().getKeyboard(), Action.class);

        input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_LEFT);
        input.addMapping(Action.MOVE_UP, KeyEvent.VK_UP);
        input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_RIGHT);
        input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_DOWN);

        engine().addObject(new Block(input, 200, 100, 3));
    }

    @Override
    protected int targetUps() {
        return this.UPS;
    }

}
