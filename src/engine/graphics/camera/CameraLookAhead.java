package engine.graphics.camera;

import engine.object.GameObject;

public final class CameraLookAhead {
    private static final double DEFAULT_THRESHOLD = 0.01;

    private double lookAheadX;
    private double lookAheadY;
    private double maxLookAheadX;
    private double maxLookAheadY;
    private double speed;
    private double threshold = DEFAULT_THRESHOLD;

    public CameraLookAhead(double maxLookAheadX, double maxLookAheadY, double speed) {
        validateMaxLookAhead(maxLookAheadX, maxLookAheadY);
        validateSpeed(speed);

        this.maxLookAheadX = maxLookAheadX;
        this.maxLookAheadY = maxLookAheadY;
        this.speed = speed;
    }

    public void update(GameObject target) {
        if (target == null) {
            return;
        }

        double targetOffsetX = targetOffset(movementDeltaX(target), maxLookAheadX);
        double targetOffsetY = targetOffset(movementDeltaY(target), maxLookAheadY);

        lookAheadX += (targetOffsetX - lookAheadX) * speed;
        lookAheadY += (targetOffsetY - lookAheadY) * speed;
    }

    public double getLookAheadX() {
        return lookAheadX;
    }

    public double getLookAheadY() {
        return lookAheadY;
    }

    public double getMaxLookAheadX() {
        return maxLookAheadX;
    }

    public double getMaxLookAheadY() {
        return maxLookAheadY;
    }

    public void setMaxLookAhead(double maxLookAheadX, double maxLookAheadY) {
        validateMaxLookAhead(maxLookAheadX, maxLookAheadY);
        this.maxLookAheadX = maxLookAheadX;
        this.maxLookAheadY = maxLookAheadY;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        validateSpeed(speed);
        this.speed = speed;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        if (threshold < 0.0) {
            throw new IllegalArgumentException("threshold must be greater than or equal to 0.");
        }
        this.threshold = threshold;
    }

    public void reset() {
        lookAheadX = 0.0;
        lookAheadY = 0.0;
    }

    private double targetOffset(double movementDelta, double maxLookAhead) {
        if (movementDelta > threshold) {
            return maxLookAhead;
        }
        if (movementDelta < -threshold) {
            return -maxLookAhead;
        }
        return 0.0;
    }

    private double movementDeltaX(GameObject target) {
        return target.getX() - target.getPreviousX();
    }

    private double movementDeltaY(GameObject target) {
        return target.getY() - target.getPreviousY();
    }

    private void validateMaxLookAhead(double maxLookAheadX, double maxLookAheadY) {
        if (maxLookAheadX < 0.0 || maxLookAheadY < 0.0) {
            throw new IllegalArgumentException(
                    "maxLookAheadX and maxLookAheadY must be greater than or equal to 0.");
        }
    }

    private void validateSpeed(double speed) {
        if (speed <= 0.0 || speed > 1.0) {
            throw new IllegalArgumentException("speed must satisfy 0.0 < speed <= 1.0.");
        }
    }
}
