package engine.asset;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class Sprite {
    private final BufferedImage image;

    public Sprite(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getWidth() {
        return image != null ? image.getWidth() : 0;
    }

    public int getHeight() {
        return image != null ? image.getHeight() : 0;
    }

    // 指定した幅と高さに直接リサイズ（アスペクト比が変化する可能性がある）
    public Sprite resized(int targetWidth, int targetHeight) {
        if (image == null || targetWidth <= 0 || targetHeight <= 0) {
            return this;
        }
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return new Sprite(resizedImage);
    }

    // アスペクト比を維持して、指定した幅にリサイズ
    public Sprite resizedByWidth(int targetWidth) {
        if (getWidth() == 0)
            return this;
        double rate = (double) targetWidth / getWidth();
        int targetHeight = (int) Math.round(getHeight() * rate);
        return resized(targetWidth, targetHeight);
    }

    // アスペクト比を維持して、指定した高さにリサイズ
    public Sprite resizedByHeight(int targetHeight) {
        if (getHeight() == 0)
            return this;
        double rate = (double) targetHeight / getHeight();
        int targetWidth = (int) Math.round(getWidth() * rate);
        return resized(targetWidth, targetHeight);
    }

    // 指定した倍率（scale）でリサイズ
    public Sprite scaled(double scale) {
        if (scale <= 0)
            return this;
        int targetWidth = (int) Math.round(getWidth() * scale);
        int targetHeight = (int) Math.round(getHeight() * scale);
        return resized(targetWidth, targetHeight);
    }

    // 指定した最大枠 (maxWidth × maxHeight) 内に、アスペクト比を保持して収まるようにリサイズ
    public Sprite resizedToFit(int maxWidth, int maxHeight) {
        if (getWidth() == 0 || getHeight() == 0)
            return this;
        double widthRatio = (double) maxWidth / getWidth();
        double heightRatio = (double) maxHeight / getHeight();
        double ratio = Math.min(widthRatio, heightRatio);
        int newWidth = (int) Math.round(getWidth() * ratio);
        int newHeight = (int) Math.round(getHeight() * ratio);
        return resized(newWidth, newHeight);
    }

    // 指定した最小枠 (minWidth × minHeight) 全体を隙間なく覆うように、アスペクト比を保持して拡大/縮小
    public Sprite resizedToFill(int minWidth, int minHeight) {
        if (getWidth() == 0 || getHeight() == 0)
            return this;
        double widthRatio = (double) minWidth / getWidth();
        double heightRatio = (double) minHeight / getHeight();
        double ratio = Math.max(widthRatio, heightRatio);
        int newWidth = (int) Math.round(getWidth() * ratio);
        int newHeight = (int) Math.round(getHeight() * ratio);
        return resized(newWidth, newHeight);
    }
}