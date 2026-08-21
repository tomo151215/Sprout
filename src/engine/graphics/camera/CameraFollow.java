package engine.graphics.camera;

import engine.object.GameObject;

public final class CameraFollow {
    private static final double DEFAULT_FOLLOW_SPEED = 0.1;

    private final Camera2D camera;
    private GameObject target;
    private double viewportWidth;
    private double viewportHeight;
    private boolean smooth;
    private double followSpeed = DEFAULT_FOLLOW_SPEED;

    public CameraFollow(
            Camera2D camera,
            GameObject target,
            double viewportWidth,
            double viewportHeight) {

        if (camera == null) {
            throw new IllegalArgumentException("camera must not be null.");
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null.");
        }

        validateViewportSize(viewportWidth, viewportHeight);

        this.camera = camera;
        this.target = target;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public void update() {
        update(0.0, 0.0);
    }

    public void update(double offsetX, double offsetY) {
        double targetCameraX = target.getX() - viewportWidth / 2.0 + offsetX;
        double targetCameraY = target.getY() - viewportHeight / 2.0 + offsetY;

        if (smooth) {
            moveSmoothly(targetCameraX, targetCameraY);
            return;
        }

        camera.setPosition(targetCameraX, targetCameraY);
    }

    public void setTarget(GameObject target) {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null.");
        }
        this.target = target;
    }

    public GameObject getTarget() {
        return target;
    }

    public void setViewportSize(double viewportWidth, double viewportHeight) {
        validateViewportSize(viewportWidth, viewportHeight);
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    public boolean isSmooth() {
        return smooth;
    }

    public void setFollowSpeed(double followSpeed) {
        if (followSpeed <= 0.0 || followSpeed > 1.0) {
            throw new IllegalArgumentException(
                    "followSpeed must satisfy 0.0 < followSpeed <= 1.0.");
        }
        this.followSpeed = followSpeed;
    }

    public double getFollowSpeed() {
        return followSpeed;
    }

    private void moveSmoothly(double targetCameraX, double targetCameraY) {
        double nextX = camera.getX() + (targetCameraX - camera.getX()) * followSpeed;
        double nextY = camera.getY() + (targetCameraY - camera.getY()) * followSpeed;
        camera.setPosition(nextX, nextY);
    }

    private void validateViewportSize(double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Viewport dimensions must be greater than 0.");
        }
    }
}
