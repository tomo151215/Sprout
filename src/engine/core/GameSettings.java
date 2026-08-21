package engine.core;

public final class GameSettings {
    private final int width;
    private final int height;
    private final String title;
    private final boolean visible;
    private final boolean centerOnScreen;
    private final boolean exitOnClose;
    private final boolean resizable;

    private GameSettings(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.title = builder.title;
        this.visible = builder.visible;
        this.centerOnScreen = builder.centerOnScreen;
        this.exitOnClose = builder.exitOnClose;
        this.resizable = builder.resizable;
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
        return visible;
    }

    public boolean isCenterOnScreen() {
        return centerOnScreen;
    }

    public boolean isExitOnClose() {
        return exitOnClose;
    }

    public boolean isResizable() {
        return resizable;
    }

    public static final class Builder {
        private int width;
        private int height;
        private String title;
        private boolean visible = true;
        private boolean centerOnScreen = true;
        private boolean exitOnClose = true;
        private boolean resizable;

        private Builder() {
        }

        public Builder size(int width, int height) {
            requirePositive(width, "width");
            requirePositive(height, "height");
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder title(String title) {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("title must not be null or blank.");
            }
            this.title = title;
            return this;
        }

        public Builder visible(boolean visible) {
            this.visible = visible;
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

        public Builder resizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public GameSettings build() {
            validateRequiredSettings();
            return new GameSettings(this);
        }

        private void validateRequiredSettings() {
            requirePositive(width, "width");
            requirePositive(height, "height");

            if (title == null || title.isBlank()) {
                throw new IllegalStateException("title must be configured before build().");
            }
        }

        private static void requirePositive(int value, String name) {
            if (value <= 0) {
                throw new IllegalArgumentException(name + " must be greater than 0.");
            }
        }
    }
}
