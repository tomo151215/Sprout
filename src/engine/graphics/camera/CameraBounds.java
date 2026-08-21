package engine.graphics.camera;

public final class CameraBounds {
    private double worldX;
    private double worldY;
    private double worldWidth;
    private double worldHeight;

    public CameraBounds(double worldX, double worldY, double worldWidth, double worldHeight) {
        setBounds(worldX, worldY, worldWidth, worldHeight);
    }

    public void constrain(Camera2D camera, double viewportWidth, double viewportHeight) {
        if (camera == null) {
            throw new IllegalArgumentException("camera must not be null.");
        }

        validateViewport(viewportWidth, viewportHeight);

        double constrainedX = constrainX(camera.getX(), viewportWidth);
        double constrainedY = constrainY(camera.getY(), viewportHeight);
        camera.setPosition(constrainedX, constrainedY);
    }

    public void setBounds(double worldX, double worldY, double worldWidth, double worldHeight) {
        validateWorldSize(worldWidth, worldHeight);
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

    private double constrainX(double cameraX, double viewportWidth) {
        if (worldWidth <= viewportWidth) {
            return worldX;
        }

        double maxCameraX = worldX + worldWidth - viewportWidth;
        return clamp(cameraX, worldX, maxCameraX);
    }

    private double constrainY(double cameraY, double viewportHeight) {
        if (worldHeight <= viewportHeight) {
            return worldY;
        }

        double maxCameraY = worldY + worldHeight - viewportHeight;
        return clamp(cameraY, worldY, maxCameraY);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private void validateWorldSize(double width, double height) {
        requirePositive(width, "worldWidth");
        requirePositive(height, "worldHeight");
    }

    private void validateViewport(double width, double height) {
        requirePositive(width, "viewportWidth");
        requirePositive(height, "viewportHeight");
    }

    private void requirePositive(double value, String name) {
        if (value <= 0.0) {
            throw new IllegalArgumentException(name + " must be greater than 0.");
        }
    }
}
