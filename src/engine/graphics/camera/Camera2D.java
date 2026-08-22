package engine.graphics.camera;

import java.awt.Graphics2D;

public final class Camera2D {

    private double x;
    private double y;
    private double previousX;
    private double previousY;

    public Camera2D() {
        this(0.0, 0.0);
    }

    public Camera2D(double x, double y) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getPreviousX() {
        return previousX;
    }

    public double getPreviousY() {
        return previousY;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void move(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
    }

    public void savePreviousPosition() {
        previousX = x;
        previousY = y;
    }

    public void apply(Graphics2D graphics, double alpha) {
        if (graphics == null) {
            throw new IllegalArgumentException("graphics must not be null.");
        }
        double renderX = lerp(previousX, x, alpha);
        double renderY = lerp(previousY, y, alpha);
        graphics.translate(-renderX, -renderY);
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

    private double lerp(double start, double end, double alpha) {
        return start + (end - start) * alpha;
    }
}
