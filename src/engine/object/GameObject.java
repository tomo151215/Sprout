package engine.object;

import engine.graphics.Renderable;
import engine.update.Updatable;
import java.awt.Graphics;
import java.awt.Graphics2D;

public abstract class GameObject implements Renderable, Updatable {
    // 現在位置
    private double x;
    private double y;

    // 前フレームの位置
    private double previousX;
    private double previousY;

    public GameObject(double x, double y) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
    }

    public final void onUpdate() {
        this.previousX = x;
        this.previousY = y;
        update();
    }

    public final void onDraw(Graphics2D g, double alpha) {
        draw(g, alpha);
    }

    protected final double lerp(double start, double end, double alpha) {
        return start + (end - start) * alpha;
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

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setPreviousX(double previousX) {
        this.previousX = previousX;
    }

    public void setPreviousY(double previousY) {
        this.previousY = previousY;
    }

}
