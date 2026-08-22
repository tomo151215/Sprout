package engine.graphics.camera;

import engine.object.GameObject;
import engine.system.GameSystem;

public final class CameraController implements GameSystem {
    private final Camera2D camera;

    private GameObject target;
    private double viewportWidth;
    private double viewportHeight;

    private CameraFollow follow;
    private CameraDeadZone deadZone;
    private CameraBounds bounds;
    private CameraLookAhead lookAhead;

    public CameraController(Camera2D camera, GameObject target, double viewportWidth, double viewportHeight) {
        if (camera == null) {
            throw new IllegalArgumentException("camera must not be null.");
        }

        validateViewportSize(viewportWidth, viewportHeight);

        this.camera = camera;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        setTarget(target);
    }

    @Override
    public void update() {
        if (target == null) {
            return;
        }

        updateCameraPosition();
        constrainToBounds();
    }

    public void setTarget(GameObject target) {
        this.target = target;
        rebuildFollow();

        if (lookAhead != null) {
            lookAhead.reset();
        }
    }

    public GameObject getTarget() {
        return target;
    }

    public void setViewportSize(double viewportWidth, double viewportHeight) {
        validateViewportSize(viewportWidth, viewportHeight);
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

        if (follow != null) {
            follow.setViewportSize(viewportWidth, viewportHeight);
        }
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

        if (lookAhead != null) {
            lookAhead.reset();
        }
    }

    public CameraLookAhead getLookAhead() {
        return lookAhead;
    }

    public void clearDeadZone() {
        deadZone = null;
    }

    public void clearLookAhead() {
        lookAhead = null;
    }

    public void clearBounds() {
        bounds = null;
    }

    private void updateCameraPosition() {
        if (deadZone != null) {
            deadZone.apply(camera, target);
            return;
        }

        followTarget();
    }

    private void followTarget() {
        if (follow == null) {
            return;
        }

        double lookAheadX = 0.0;
        double lookAheadY = 0.0;

        if (lookAhead != null) {
            lookAhead.update(target);
            lookAheadX = lookAhead.getLookAheadX();
            lookAheadY = lookAhead.getLookAheadY();
        }

        follow.update(lookAheadX, lookAheadY);
    }

    private void constrainToBounds() {
        if (bounds != null) {
            bounds.constrain(camera, viewportWidth, viewportHeight);
        }
    }

    private void rebuildFollow() {
        if (target == null) {
            follow = null;
            return;
        }

        follow = new CameraFollow(camera, target, viewportWidth, viewportHeight);
    }

    private void validateViewportSize(double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Viewport dimensions must be greater than 0.");
        }
    }
}
