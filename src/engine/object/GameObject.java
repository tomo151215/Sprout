package engine.object;

import java.awt.Graphics2D;

import engine.graphics.Renderable;
import engine.update.Updatable;

public abstract class GameObject implements Renderable, Updatable {
    private double x;
    private double y;
    private double previousX;
    private double previousY;

    protected GameObject(double x, double y) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
    }

    @Override
    public final void update() {
        savePreviousPosition();
        onUpdate();
    }

    @Override
    public final void draw(Graphics2D graphics, double alpha) {
        onDraw(graphics, alpha);
    }

    protected abstract void onUpdate();

    protected abstract void onDraw(Graphics2D graphics, double alpha);

    public final double getX() {
        return x;
    }

    public final double getY() {
        return y;
    }

    public final double getPreviousX() {
        return previousX;
    }

    public final double getPreviousY() {
        return previousY;
    }

    public final void setX(double x) {
        this.x = x;
    }

    public final void setY(double y) {
        this.y = y;
    }

    public final void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public final void move(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
    }

    protected final double interpolatedX(double alpha) {
        return lerp(previousX, x, alpha);
    }

    protected final double interpolatedY(double alpha) {
        return lerp(previousY, y, alpha);
    }

    protected final double lerp(double start, double end, double alpha) {
        return start + (end - start) * alpha;
    }

    private void savePreviousPosition() {
        previousX = x;
        previousY = y;
    }
}
