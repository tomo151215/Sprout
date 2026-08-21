package engine.asset;

import java.awt.image.BufferedImage;

public final class Sprite {
    private final BufferedImage image;

    public Sprite(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image must not be null.");
        }
        this.image = image;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public BufferedImage getImage() {
        return image;
    }
}
