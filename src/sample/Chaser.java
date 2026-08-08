package sample;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import engine.object.GameObject;

public class Chaser extends GameObject {
    private static final int SIZE = 36;

    private final Player player;
    private final List<Block> blocks;

    private double speed = 1.6;

    public Chaser(double x, double y, Player player, List<Block> blocks) {
        super(x, y);

        if (player == null) {
            throw new IllegalArgumentException("player must not be null.");
        }
        if (blocks == null) {
            throw new IllegalArgumentException("blocks must not be null.");
        }

        this.player = player;
        this.blocks = blocks;
    }

    @Override
    public void update() {
        if (player.isCleared()) {
            return;
        }

        double dx = player.getX() - getX();
        double dy = player.getY() - getY();

        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0.0) {
            dx = dx / length * speed;
            dy = dy / length * speed;
        }

        moveWithCollision(dx, dy);

        if (intersects(
                getX(),
                getY(),
                SIZE,
                SIZE,
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight()
        )) {
            player.resetToStart();
        }
    }

    private void moveWithCollision(double dx, double dy) {
        double oldX = getX();
        double oldY = getY();

        setX(getX() + dx);

        if (collidesWithAnyBlock()) {
            setX(oldX);
        }

        setY(getY() + dy);

        if (collidesWithAnyBlock()) {
            setY(oldY);
        }
    }

    private boolean collidesWithAnyBlock() {
        for (Block block : blocks) {
            if (intersects(
                    getX(),
                    getY(),
                    SIZE,
                    SIZE,
                    block.getX(),
                    block.getY(),
                    block.getWidth(),
                    block.getHeight()
            )) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        int drawX = (int) lerp(getPreviousX(), getX(), alpha);
        int drawY = (int) lerp(getPreviousY(), getY(), alpha);

        g.setColor(new Color(220, 60, 60));
        g.fillOval(drawX, drawY, SIZE, SIZE);

        g.setColor(Color.WHITE);
        g.drawOval(drawX, drawY, SIZE, SIZE);
    }

    private boolean intersects(
            double ax,
            double ay,
            double aw,
            double ah,
            double bx,
            double by,
            double bw,
            double bh
    ) {
        return ax < bx + bw
                && ax + aw > bx
                && ay < by + bh
                && ay + ah > by;
    }
}
