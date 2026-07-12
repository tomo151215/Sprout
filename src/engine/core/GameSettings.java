package engine.core;
public final class GameSettings {

    private final int width;
    private final int height;
    private final String title;
    private final boolean isVisible;
    private final boolean centerOnScreen;
    private final boolean exitOnClose;
    private final boolean isResizeable;

    public GameSettings(int width, int height, String title, boolean isVisible, boolean centerOnScreen,
            boolean exitOnClose, boolean isResizeable) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.isVisible = isVisible;
        this.centerOnScreen = centerOnScreen;
        this.exitOnClose = exitOnClose;
        this.isResizeable = isResizeable;
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

    public boolean isCenterOnScreen() {
        return centerOnScreen;
    }

    public boolean isExitOnClose() {
        return exitOnClose;
    }

    public boolean isResizable() {
        return isResizeable;
    }

}
