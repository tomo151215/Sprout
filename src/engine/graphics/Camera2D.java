package engine.graphics;

import java.awt.Graphics2D;

public class Camera2D {
    private double x;
    private double y;

    public Camera2D() {
        this(0.0, 0.0);
    }

    public Camera2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void move(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    public void apply(Graphics2D g) {
        if (g == null) {
            throw new IllegalArgumentException("graphics must not be null.");
        }
        g.translate(-x, -y);
    }

    public double screenToWorldX(double screenX) {
        return x + screenX;
    }

    public double screenToWorldY(double screenY) {
        return y + screenY;
    }

    public double worldToScreenX(double worldX) {
        return worldX - x;
    }

    public double worldToScreenY(double worldY) {
        return worldY - y;
    }
}
