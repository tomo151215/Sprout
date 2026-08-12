package engine.graphics;

import java.awt.Color;

public final class RendererConfig {
    private final Color backgroundColor;
    private final boolean antiAliasing;
    private final boolean debugRender;
    private final boolean interpolation;

    private RendererConfig(Builder builder) {
        this.backgroundColor = builder.backgroundColor;
        this.antiAliasing = builder.antiAliasing;
        this.debugRender = builder.debugRender;
        this.interpolation = builder.interpolation;
    }

    // ネストクラス
    public static final class Builder {
        private Color backgroundColor = Color.WHITE;
        private boolean antiAliasing;
        private boolean debugRender = false;
        private boolean interpolation = true;

        private Builder() {
        }

        public Builder backgroundColor(Color color) {
            if (color == null) {
                throw new IllegalArgumentException("backgroundColor must not be null.");
            }
            this.backgroundColor = color;
            return this;
        }

        public Builder isAntiAliasing(boolean antiAliasing) {
            this.antiAliasing = antiAliasing;
            return this;
        }

        public Builder isDebugRender(boolean debugRender) {
            this.debugRender = debugRender;
            return this;
        }

        public Builder isInterpolation(boolean interpolation) {
            this.interpolation = interpolation;
            return this;
        }

        public RendererConfig build() {
            return new RendererConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isAntiAliasing() {
        return antiAliasing;
    }

    public boolean isDebugRender() {
        return debugRender;
    }

    public boolean isInterpolation() {
        return interpolation;
    }

}
