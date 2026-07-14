package test;

import java.awt.Color;
import java.awt.Graphics;

import engine.object.GameObject;

public class MovingBox extends GameObject {

    private int width;
    private int height;

    public MovingBox(double x, double y, int width, int height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }

    @Override
    public void update() {
        setX(getX() + 1.0);
    }

    @Override
    public void draw(Graphics g, double alpha) {
        // Lerp実装
        double lerpX = lerp(getPreviousX(), getX(), alpha);

        g.setColor(Color.BLACK);
        g.fillRect((int) lerpX, (int) getY(), this.width, this.height);
    }
}
