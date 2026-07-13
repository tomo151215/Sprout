package engine.object;

import engine.graphics.Renderable;
import engine.update.Updatable;
import java.awt.Graphics;

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

    public final void onDraw(Graphics g, double alpha) {
        draw(g, alpha);
    }

    // Lerpメソッド
    protected final double lerp(double start, double end, double alpha) {
        return (1 - alpha) * start + alpha * end;
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

    

}
