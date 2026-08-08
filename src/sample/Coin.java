package sample;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.object.GameObject;

public class Coin extends GameObject {
    private static final int SIZE = 24;

    private boolean collected;

    public Coin(double x, double y) {
        super(x, y);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        if (collected) {
            return;
        }

        int drawX = (int) lerp(getPreviousX(), getX(), alpha);
        int drawY = (int) lerp(getPreviousY(), getY(), alpha);

        g.setColor(new Color(255, 210, 40));
        g.fillOval(drawX, drawY, SIZE, SIZE);

        g.setColor(new Color(160, 120, 20));
        g.drawOval(drawX, drawY, SIZE, SIZE);
    }

    public void collect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public int getWidth() {
        return SIZE;
    }

    public int getHeight() {
        return SIZE;
    }
}