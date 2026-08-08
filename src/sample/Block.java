package sample;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.object.GameObject;

public class Block extends GameObject {
    private final int width;
    private final int height;

    public Block(double x, double y, int width, int height) {
        super(x, y);

        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0.");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than 0.");
        }

        this.width = width;
        this.height = height;
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        int drawX = (int) lerp(getPreviousX(), getX(), alpha);
        int drawY = (int) lerp(getPreviousY(), getY(), alpha);

        g.setColor(new Color(80, 80, 80));
        g.fillRect(drawX, drawY, width, height);

        g.setColor(new Color(40, 40, 40));
        g.drawRect(drawX, drawY, width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}