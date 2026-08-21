package engine.asset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public final class ImageLoader {
    private ImageLoader() {
    }

    public static BufferedImage load(String path) {
        validatePath(path);

        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image == null) {
                throw new IllegalArgumentException("Unsupported image format: " + path);
            }
            return image;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load image: " + path, e);
        }
    }

    public static Sprite loadSprite(String path) {
        return new Sprite(load(path));
    }

    private static void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be null or blank.");
        }
    }
}
