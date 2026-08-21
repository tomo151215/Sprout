package engine.graphics.camera;

import java.awt.Graphics2D;

public final class Camera2D {
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
        return x;
    }

    public double getY() {
        return y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void move(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
    }

    public void apply(Graphics2D graphics) {
        if (graphics == null) {
            throw new IllegalArgumentException("graphics must not be null.");
        }
        graphics.translate(-x, -y);
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
