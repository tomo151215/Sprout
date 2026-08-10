package engine.graphics;

import engine.object.GameObject;

public class CameraLookAhead {
    private double lookAheadX = 0.0;
    private double lookAheadY = 0.0;
    private double maxLookAheadX;
    private double maxLookAheadY;
    private double speed;
    private double threshold = 0.01;

    public CameraLookAhead(double maxLookAheadX, double maxLookAheadY, double speed) {
        validateSize(speed, maxLookAheadX, maxLookAheadY);
        this.maxLookAheadX = maxLookAheadX;
        this.maxLookAheadY = maxLookAheadY;
        this.speed = speed;
    }

    public void update(GameObject target) {
        if (target == null) {
            return;
        }
        // 移動量計算
        double dx = distinctDirectionX(target);
        double dy = distinctDirectionY(target);
        // 先読み量決定
        double targetOffsetX = calculateTargetOffset(dx, maxLookAheadX);
        double targetOffsetY = calculateTargetOffset(dy, maxLookAheadY);
        // １回のupdateのカメラずらし幅
        lookAheadX += (targetOffsetX - lookAheadX) * speed;
        lookAheadY += (targetOffsetY - lookAheadY) * speed;
    }

    private double calculateTargetOffset(double diff, double maxLookAhead) {
        if (diff > threshold) {
            return maxLookAhead;
        }
        if (diff < threshold) {
            return -maxLookAhead;
        }
        return 0.0;
    }

    private void validateSize(double speed, double maxLookAheadX, double maxLookAheadY) {
        if (speed <= 0.0 || speed > 1.0) {
            throw new IllegalArgumentException("speed must be 0.0 < speed <= 1.0");
        }
        if (maxLookAheadX < 0) {
            throw new IllegalArgumentException("maxLookAheadX must be greater than 0.");
        }
        if (maxLookAheadY < 0) {
            throw new IllegalArgumentException("maxLookAheadY must be greater than 0.");
        }
    }

    public double distinctDirectionX(GameObject target) {
        return target.getX() - target.getPreviousX();
    }

    public double distinctDirectionY(GameObject target) {
        return target.getY() - target.getPreviousY();
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
        if (maxLookAheadX < 0.0 || maxLookAheadY < 0.0) {
            throw new IllegalArgumentException("maxLookAheadX and maxLookAheadY must be greater than 0.0.");
        }
        this.maxLookAheadX = maxLookAheadX;
        this.maxLookAheadY = maxLookAheadY;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (speed <= 0.0 || speed > 1.0) {
            throw new IllegalArgumentException("speed must be 0.0 < speed <= 1.0.");
        }
        this.speed = speed;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        if (threshold < 0.0) {
            throw new IllegalArgumentException("threshold must be threshold >= 0.0.");
        }
        this.threshold = threshold;
    }

    public void resetLookAhead() {
        this.lookAheadX = 0.0;
        this.lookAheadY = 0.0;
    }
}
