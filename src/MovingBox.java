import java.awt.Color;
import java.awt.Graphics;

public class MovingBox implements Updatable, Renderable {
    private int x = 100;
    private int y = 250;
    private int size = 50;
    private int speed = 4;

    @Override
    public void update() {
        x += speed;

        if (x < 0 || x + size > 800) {
            speed *= -1;
        }
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, size, size);
    }

}
