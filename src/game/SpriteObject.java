package game;

import java.awt.Graphics2D;

import engine.asset.Sprite;
import engine.input.InputContext;
import engine.object.GameObject;

public final class SpriteObject extends GameObject {
    private final Sprite sprite;

    private static final double SPEED = 3.0;
    private final InputContext<Action> input;

    public SpriteObject(double x, double y, InputContext<Action> input, Sprite sprite) {
        super(x, y);
        this.sprite = sprite;
        this.input = input;
    }

    @Override
    protected void onUpdate() {
        if (input.isPressed(Action.MOVE_RIGHT)) {
            move(SPEED, 0.0);
        }

        if (input.isPressed(Action.MOVE_LEFT)) {
            move(-SPEED, 0.0);
        }

        if (input.isPressed(Action.MOVE_DOWN)) {
            move(0.0, SPEED);
        }

        if (input.isPressed(Action.MOVE_UP)) {
            move(0.0, -SPEED);
        }
    }

    @Override
    protected void onDraw(Graphics2D graphics, double alpha) {
        int renderX = (int) interpolatedX(alpha);
        int renderY = (int) interpolatedY(alpha);
        graphics.drawImage(sprite.getImage(), renderX, renderY, null);
    }
}