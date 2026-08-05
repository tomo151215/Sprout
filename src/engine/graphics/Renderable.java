package engine.graphics;

import java.awt.Graphics2D;

public interface Renderable {
    void draw(Graphics2D g, double alpha);
}
