package engine.graphics.camera;

import engine.object.GameObject;

public final class CameraDeadZone {
    private double x;
    private double y;
    private double width;
    private double height;

    public CameraDeadZone(double x, double y, double width, double height) {
        setBounds(x, y, width, height);
    }

    public void apply(Camera2D camera, GameObject target) {
        if (camera == null) {
            throw new IllegalArgumentException("camera must not be null.");
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null.");
        }

        moveCameraHorizontally(camera, target);
        moveCameraVertically(camera, target);
    }

    public void setBounds(double x, double y, double width, double height) {
        validateSize(width, height);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    private void moveCameraHorizontally(Camera2D camera, GameObject target) {
        double targetScreenX = camera.worldToScreenX(target.getX());
        double left = x;
        double right = x + width;

        if (targetScreenX < left) {
            camera.move(targetScreenX - left, 0.0);
            return;
        }

        if (targetScreenX > right) {
            camera.move(targetScreenX - right, 0.0);
        }
    }

    private void moveCameraVertically(Camera2D camera, GameObject target) {
        double targetScreenY = camera.worldToScreenY(target.getY());
        double top = y;
        double bottom = y + height;

        if (targetScreenY < top) {
            camera.move(0.0, targetScreenY - top);
            return;
        }

        if (targetScreenY > bottom) {
            camera.move(0.0, targetScreenY - bottom);
        }
    }

    private void validateSize(double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Dead-zone width and height must be greater than 0.");
        }
    }
}
