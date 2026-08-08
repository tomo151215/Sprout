package sample;

import java.awt.Graphics2D;

import engine.graphics.Camera2D;
import engine.graphics.CameraBounds;
import engine.graphics.CameraFollow;
import engine.object.GameObject;

public final class CameraFollowUpdater extends GameObject {
    private final Camera2D camera;
    private final CameraFollow cameraFollow;
    private final CameraBounds cameraBounds;
    private final double viewportWidth;
    private final double viewportHeight;

    public CameraFollowUpdater(
            Camera2D camera,
            CameraFollow cameraFollow,
            CameraBounds cameraBounds,
            double viewportWidth,
            double viewportHeight
    ) {
        super(0, 0);

        if (camera == null) {
            throw new IllegalArgumentException("camera must not be null.");
        }
        if (cameraFollow == null) {
            throw new IllegalArgumentException("cameraFollow must not be null.");
        }
        if (cameraBounds == null) {
            throw new IllegalArgumentException("cameraBounds must not be null.");
        }
        if (viewportWidth <= 0.0) {
            throw new IllegalArgumentException("viewportWidth must be greater than 0.");
        }
        if (viewportHeight <= 0.0) {
            throw new IllegalArgumentException("viewportHeight must be greater than 0.");
        }

        this.camera = camera;
        this.cameraFollow = cameraFollow;
        this.cameraBounds = cameraBounds;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    @Override
    public void update() {
        cameraFollow.update();

        cameraBounds.constrain(
                camera,
                viewportWidth,
                viewportHeight
        );
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
    }
}