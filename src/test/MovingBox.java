package test;

import java.awt.Color;
import java.awt.Graphics;

import engine.graphics.Renderable;
import engine.update.Updatable;

public class MovingBox implements Renderable, Updatable {

    private double currentX;
    private double currentY;
    private double previousX;
    private int width;
    private int height;

    public MovingBox(double currentX, double currentY, int width, int height) {
        this.currentX = currentX;
        this.currentY = currentY;
        this.previousX = currentX;
        this.width = width;
        this.height = height;
    }

    @Override
    public void update() {
        this.previousX = currentX;
        this.currentX += 1.0;
    }

    @Override
    public void draw(Graphics g, double alpha) {
        //Lerp実装
        double lerpX = (1 - alpha) * previousX + alpha * currentX;

        g.setColor(Color.BLACK);
        g.fillRect((int)lerpX, (int)this.currentY, this.width, this.height);
    }
}
