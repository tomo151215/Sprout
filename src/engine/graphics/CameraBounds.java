package engine.graphics;

public class CameraBounds {
    private double worldX;
    private double worldY;
    private double worldWidth;
    private double worldHeight;

    public CameraBounds(double worldX, double worldY, double worldWidth, double worldHeight) {
        if (worldWidth <= 0.0) {
            throw new IllegalArgumentException("worldWidth must be greater than 0.");
        }
        if (worldHeight <= 0.0) {
            throw new IllegalArgumentException("worldHeight must be greater than 0.");
        }
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void constrain(Camera2D camera, double viewportWidth, double viewportHeight) {
        if (camera == null) {
            throw new IllegalArgumentException("camera must not be null.");
        }
        if (viewportWidth <= 0.0) {
            throw new IllegalArgumentException("viewportWidth must be greater than 0.");
        }
        if (viewportHeight <= 0.0) {
            throw new IllegalArgumentException("viewportHeight must be greater than 0.");
        }

        double constrainedX;
        double constrainedY;

        if (worldWidth <= viewportWidth) {
            constrainedX = worldX;
        } else {
            double maxCameraX = worldX + worldWidth - viewportWidth;
            constrainedX = clamp(camera.getX(), worldX, maxCameraX);
        }
        if (worldHeight <= viewportHeight) {
            constrainedY = worldY;
        } else {
            double maxCameraY = worldY + worldHeight - viewportHeight;
            constrainedY = clamp(camera.getY(), worldY, maxCameraY);
        }
        camera.setPosition(constrainedX, constrainedY);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public void setBounds(double worldX, double worldY, double worldWidth, double worldHeight) {
        validateSize(worldWidth, worldHeight);
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public double getWorldX() {
        return worldX;
    }

    public double getWorldY() {
        return worldY;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }

    private void validateSize(double worldWidth, double worldHeight) {
        if (worldWidth <= 0.0) {
            throw new IllegalArgumentException("worldWidth must be greater than 0.");
        }
        if (worldHeight <= 0.0) {
            throw new IllegalArgumentException("worldHeight must be greater than 0.");
        }
    }
}
