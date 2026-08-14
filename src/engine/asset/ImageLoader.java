package engine.asset;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public final class ImageLoader {
    private ImageLoader() {
    }

    public static BufferedImage load(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be null or blank.");
        }

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
}
