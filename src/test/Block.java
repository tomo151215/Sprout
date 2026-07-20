package test;

import java.awt.Color;
import java.awt.Graphics;

import engine.input.InputManager;
import engine.object.GameObject;

public class Block extends GameObject {
    private int width;
    private int height;
    private int speed;
    private final InputManager<Action> input;

    public Block(InputManager<Action> input, int width, int height, int speed) {
        super(400, 300);
        this.input = input;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    @Override
    public void draw(Graphics g, double alpha) {
        int drawX = (int) lerp(getPreviousX(), getX(), alpha);
        int drawY = (int) lerp(getPreviousY(), getY(), alpha);
        g.setColor(Color.BLUE);
        g.fillRect(drawX, drawY, width, height);
    }

    @Override
    public void update() {
        if (input.isPressed(Action.MOVE_LEFT)) {
            setX(getX() - speed);
        }

        if (input.isPressed(Action.MOVE_UP)) {
            setY(getY() - speed);
        }

        if (input.isPressed(Action.MOVE_RIGHT)) {
            setX(getX() + speed);
        }

        if (input.isPressed(Action.MOVE_DOWN)) {
            setY(getY() + speed);
        }
    }

}
