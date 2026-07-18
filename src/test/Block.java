package test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import engine.input.Keyboard;
import engine.object.GameObject;

public class Block extends GameObject {
    private int width;
    private int height;
    private int speed;
    private final Keyboard k;

    public Block(Keyboard k, int width, int height,int speed) {
        super(400, 300);
        this.k = k;
        this.width = width;
        this.height = height;
        this.speed=speed;
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
        if (k.isPressed(KeyEvent.VK_LEFT)) {
            setX(getX() - speed);
        }

        if (k.isPressed(KeyEvent.VK_UP)) {
            setY(getY() - speed);
        }

        if (k.isPressed(KeyEvent.VK_RIGHT)) {
            setX(getX() + speed);
        }

        if (k.isPressed(KeyEvent.VK_DOWN)) {
            setY(getY() + speed);
        }
    }

}
