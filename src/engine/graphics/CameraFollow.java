package engine.graphics;

import engine.object.GameObject;

public class CameraFollow {
    private final Camera2D camera;
    private GameObject target;
    private int viewportWidth;
    private int viewportHeight;

    private boolean smooth;
    private double followSpeed;

    public CameraFollow(Camera2D camera, GameObject target, int viewportWidth, int viewportHeight) {
        if (camera == null) {
            throw new IllegalArgumentException("camera must not be null.");
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null.");
        }
        if (viewportWidth <= 0) {
            throw new IllegalArgumentException("viewportWidth must be greater than 0.");
        }
        if (viewportHeight <= 0) {
            throw new IllegalArgumentException("viewportHeight must be greater than 0.");
        }

        this.camera = camera;
        this.target = target;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public void update() {
        double targetCameraX = target.getX() - viewportWidth / 2.0;
        double targetCameraY = target.getY() - viewportHeight / 2.0;
        if (smooth) {
            double nextX = camera.getX() + (targetCameraX - camera.getX()) * followSpeed;
            double nextY = camera.getY() + (targetCameraY - camera.getY()) * followSpeed;
            camera.setPosition(nextX, nextY);
        } else {
            camera.setPosition(targetCameraX, targetCameraY);
        }
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

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    public boolean isSmooth() {
        return smooth;
    }

    public void setFollowSpeed(double followSpeed) {
        if (followSpeed <= 0.0 || followSpeed > 1.0) {
            throw new IllegalArgumentException("followSpeed must be greater than 0 and less than or equal to 1.");
        }
        this.followSpeed = followSpeed;
    }

    public double getFollowSpeed() {
        return followSpeed;
    }
}
