package game;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.object.GameObject;

public final class WorldObject extends GameObject {
    private static final int SIZE = 80;
    private final Color color;

    public WorldObject(double x, double y, Color color) {
        super(x, y);
        this.color = color;
    }

    @Override
    protected void onUpdate() {
    }

    @Override
    protected void onDraw(Graphics2D graphics, double alpha) {
        int renderX = (int) interpolatedX(alpha);
        int renderY = (int) interpolatedY(alpha);
        graphics.setColor(color);
        graphics.fillRect(renderX, renderY, SIZE, SIZE);
    }
}