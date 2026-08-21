package engine.graphics;

import java.awt.Color;

public final class RendererConfig {
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    private final Color backgroundColor;
    private final boolean antiAliasing;
    private final boolean interpolation;

    private RendererConfig(Builder builder) {
        this.backgroundColor = builder.backgroundColor;
        this.antiAliasing = builder.antiAliasing;
        this.interpolation = builder.interpolation;
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

    public boolean isInterpolation() {
        return interpolation;
    }

    public static final class Builder {
        private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
        private boolean antiAliasing;
        private boolean interpolation = true;

        private Builder() {
        }

        public Builder backgroundColor(Color backgroundColor) {
            if (backgroundColor == null) {
                throw new IllegalArgumentException("backgroundColor must not be null.");
            }
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder antiAliasing(boolean antiAliasing) {
            this.antiAliasing = antiAliasing;
            return this;
        }

        public Builder interpolation(boolean interpolation) {
            this.interpolation = interpolation;
            return this;
        }

        public RendererConfig build() {
            return new RendererConfig(this);
        }
    }
}
