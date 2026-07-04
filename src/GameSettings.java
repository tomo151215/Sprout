public final class GameSettings {
    private final int height;
    private final int width;
    private final String title;
    private final boolean isVisible;

    public GameSettings(int height, int width, String title, boolean isVisible) {
        this.height = height;
        this.width = width;
        this.title = title;
        this.isVisible = isVisible;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getTitle() {
        return title;
    }

    public boolean isVisible() {
        return isVisible;
    }

}
