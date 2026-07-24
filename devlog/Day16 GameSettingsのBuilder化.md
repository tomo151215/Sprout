# Day16: GameSettingsのBuilder化

## GameSettingsの問題点
現在のGameSettingsは、インスタンス化するときに以下のように行います。
```java
new GameSettings(800, 600, "SampleGame", true, true, true, false);
```
これだとコンストラクタの各引数が何を表しているかよくわからず、ミスが発生する可能性があります。そこでBuilderパターンを用います。

## Builder化
```java
package engine.core;

public final class GameSettings {

    private final int width;
    private final int height;
    private final String title;
    private final boolean isVisible;
    private final boolean centerOnScreen;
    private final boolean exitOnClose;
    private final boolean isResizeable;

    private GameSettings(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.title = builder.title;
        this.isVisible = builder.isVisible;
        this.centerOnScreen = builder.centerOnScreen;
        this.exitOnClose = builder.exitOnClose;
        this.isResizeable = builder.isResizeable;
    }

    public static final class Builder {
        private int width;
        private int height;
        private String title;
        private boolean isVisible = true;
        private boolean centerOnScreen = true;
        private boolean exitOnClose = true;
        private boolean isResizeable = false;

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

        public Builder isResizeable(boolean isResizeable) {
            this.isResizeable = isResizeable;
            return this;
        }

        public GameSettings build() {
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

    public boolean isResizeable() {
        return isResizeable;
    }

}
```
またMyGameにおいてcreateSetings()メソッド内でBuilderを使用してGameSettingsオブジェクトを作成しています。
```java
    protected GameSettings createSettings() {
        return GameSettings.builder()
                .size(800,600)
                .title("SampleApp")
                .build();
    }
```