package engine.graphics;

import engine.core.GameSystem;
import engine.object.GameObject;

public final class CameraController implements GameSystem {
    private Camera2D camera;
    private GameObject target;

    private double viewportWidth;
    private double viewportHeight;

    private CameraDeadZone deadZone;
    private CameraBounds bounds;
    private CameraLookAhead lookAhead;

    public CameraController(Camera2D camera, GameObject target, double viewportWidth, double viewportHeight) {
        if (camera == null) {
            throw new IllegalArgumentException("camera must not be null.");
        }
        validateViewportSize(viewportWidth, viewportHeight);
        this.camera = camera;
        this.target = target;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    @Override
    public void update() {
        if (target == null) {
            return;
        }
        if (deadZone != null) {
            updateDeadZone();
        } else {
            updateFollow();
        }
        applyBounds();
    }

    public void updateFollow() {
        double cameraX = target.getX() - viewportWidth / 2.0;
        double cameraY = target.getY() - viewportHeight / 2.0;
        if (lookAhead != null) {
            lookAhead.update(target);
            cameraX += lookAhead.getLookAheadX();
            cameraY += lookAhead.getLookAheadY();
        }
        camera.setPosition(cameraX, cameraY);
    }

    public void updateDeadZone() {
        deadZone.apply(camera, target);
    }

    public void applyBounds() {
        if (bounds != null) {
            bounds.constrain(camera, viewportWidth, viewportHeight);
        }
    }

    public void setTarget(GameObject target) {
        this.target = target;
        if (lookAhead != null) {
            lookAhead.resetLookAhead();
        }
    }

    public GameObject getTarget() {
        return target;
    }

    public void setViewportSize(double viewportWidth, double viewportHeight) {
        validateViewportSize(viewportWidth, viewportHeight);
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public double getViewportWidth() {
        return viewportWidth;
    }

    public double getViewportHeight() {
        return viewportHeight;
    }

    public void setBounds(CameraBounds bounds) {
        this.bounds = bounds;
    }

    public CameraBounds getBounds() {
        return bounds;
    }

    public void setDeadZone(CameraDeadZone deadZone) {
        this.deadZone = deadZone;
    }

    public CameraDeadZone getDeadZone() {
        return deadZone;
    }

    public void setLookAhead(CameraLookAhead lookAhead) {
        this.lookAhead = lookAhead;
    }

    public CameraLookAhead getLookAhead() {
        return lookAhead;
    }

    public void clearDeadZone() {
        this.deadZone = null;
    }

    public void clearLookAhead() {
        this.lookAhead = null;
    }

    public void clearBounds() {
        this.bounds = null;
    }

    private void validateViewportSize(double viewportWidth, double viewportHeight) {
        if (viewportWidth <= 0.0 || viewportHeight <= 0.0) {
            throw new IllegalArgumentException("Viewport dimensions must be greater than 0.");
        }
    }
}
