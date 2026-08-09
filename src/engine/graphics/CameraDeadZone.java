package engine.graphics;

import engine.object.GameObject;

public class CameraDeadZone {
    private double deadZoneX;
    private double deadZoneY;
    private double deadZoneWidth;
    private double deadZoneHeight;

    public CameraDeadZone(double deadZoneX, double deadZoneY, double deadZoneWidth, double deadZoneHeight) {
        validateSize(deadZoneWidth, deadZoneHeight);
        this.deadZoneX = deadZoneX;
        this.deadZoneY = deadZoneY;
        this.deadZoneWidth = deadZoneWidth;
        this.deadZoneHeight = deadZoneHeight;
    }

    private void validateSize(double width, double height) {
        if (width <= 0.0) {
            throw new IllegalArgumentException("width must be greater than 0.");
        }
        if (height <= 0.0) {
            throw new IllegalArgumentException("height must be greater than 0.");
        }
    }

    public void apply(Camera2D camera, GameObject target) {
        if (camera == null) {
            throw new IllegalArgumentException("camera must not be null.");
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null.");
        }
        double deadZoneLeftX = deadZoneX;
        double deadZoneRightX = deadZoneX + deadZoneWidth;
        double deadZoneTopY = deadZoneY;
        double deadZoneBottomY = deadZoneY + deadZoneHeight;

        double targetScreenX = camera.worldToScreenX(target.getX());
        double targetScreenY = camera.worldToScreenY(target.getY());

        // 対象の画面X座標がデッドゾーン左端より左にある → カメラを左へ動かす
        if (targetScreenX < deadZoneLeftX) {
            camera.move(targetScreenX - deadZoneLeftX, 0);
        }
        // 対象の画面X座標がデッドゾーン右端より右にある → カメラを右へ動かす
        if (targetScreenX > deadZoneRightX) {
            camera.move(targetScreenX - deadZoneRightX, 0);
        }
        // 対象の画面Y座標がデッドゾーン上端より上にある → カメラを上へ動かす
        if (targetScreenY < deadZoneTopY) {
            camera.move(0, targetScreenY - deadZoneTopY);
        }
        // 対象の画面Y座標がデッドゾーン下端より下にある → カメラを下へ動かす
        if (targetScreenY > deadZoneTopY) {
            camera.move(0, targetScreenY - deadZoneTopY);
        }
    }

    public double getDeadZoneX() {
        return deadZoneX;
    }

    public double getDeadZoneY() {
        return deadZoneY;
    }

    public double getDeadZoneWidth() {
        return deadZoneWidth;
    }

    public double getDeadZoneHeight() {
        return deadZoneHeight;
    }

    public void setBounds(double deadZoneX, double deadZoneY, double deadZoneWidth, double deadZoneHeight) {
        validateSize(deadZoneWidth, deadZoneHeight);
        this.deadZoneX = deadZoneX;
        this.deadZoneY = deadZoneY;
        this.deadZoneWidth = deadZoneWidth;
        this.deadZoneHeight = deadZoneHeight;
    }

}
