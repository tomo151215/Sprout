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
                .size(800, 600)
                .title("SampleApp")
                .build();
    }

    @Override
    protected void onInit() {
        engine().addObject(new MousePointerBox(engine().getMouse()));
    }

    @Override
    protected int targetUps() {
        return this.UPS;
    }

}
