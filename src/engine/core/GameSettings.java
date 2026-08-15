package engine.core;

public final class GameSettings {

    private final int width;
    private final int height;
    private final String title;
    private final boolean isVisible;
    private final boolean centerOnScreen;
    private final boolean exitOnClose;
    private final boolean isResizable;

    private GameSettings(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.title = builder.title;
        this.isVisible = builder.isVisible;
        this.centerOnScreen = builder.centerOnScreen;
        this.exitOnClose = builder.exitOnClose;
        this.isResizable = builder.isResizable;
    }

    public static final class Builder {
        private int width;
        private int height;
        private String title;
        private boolean isVisible = true;
        private boolean centerOnScreen = true;
        private boolean exitOnClose = true;
        private boolean isResizable = false;

        public Builder size(int width, int height) {
            if (width <= 0) {
                throw new IllegalArgumentException("width must be greater than 0.");
            }

            if (height <= 0) {
                throw new IllegalArgumentException("height must be greater than 0.");
            }

            this.width = width;
            this.height = height;
            return this;
        }

        public Builder title(String title) {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("title must not be blank.");
            }
            this.title = title;
            return this;
        }

        public Builder isVisible(boolean isVisible) {
            this.isVisible = isVisible;
            return this;
        }

        public Builder centerOnScreen(boolean centerOnScreen) {
            this.centerOnScreen = centerOnScreen;
            return this;
        }

        public Builder exitOnClose(boolean exitOnClose) {
            this.exitOnClose = exitOnClose;
            return this;
        }

        public Builder isResizable(boolean isResizeable) {
            this.isResizable = isResizeable;
            return this;
        }

        public GameSettings build() {
            if (width <= 0 || height <= 0) {
                throw new IllegalStateException(
                        "size must be configured before build().");
            }

            if (title == null || title.isBlank()) {
                throw new IllegalStateException(
                        "title must be configured before build().");
            }
            return new GameSettings(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
        return isResizable;
    }

}
