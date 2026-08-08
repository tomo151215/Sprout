package sample;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import engine.input.InputContext;
import engine.object.GameObject;

public class Player extends GameObject {
    private static final int WIDTH = 40;
    private static final int HEIGHT = 40;

    private final InputContext<Action> input;
    private final List<Block> blocks;
    private final List<Coin> coins;
    private final Goal goal;

    private final double startX;
    private final double startY;

    private double speed = 4.0;
    private int collectedCount;
    private boolean cleared;
    private String message = "Collect all coins and reach the green goal.";

    public Player(
            double x,
            double y,
            InputContext<Action> input,
            List<Block> blocks,
            List<Coin> coins,
            Goal goal
    ) {
        super(x, y);

        if (input == null) {
            throw new IllegalArgumentException("input must not be null.");
        }
        if (blocks == null) {
            throw new IllegalArgumentException("blocks must not be null.");
        }
        if (coins == null) {
            throw new IllegalArgumentException("coins must not be null.");
        }
        if (goal == null) {
            throw new IllegalArgumentException("goal must not be null.");
        }

        this.input = input;
        this.blocks = blocks;
        this.coins = coins;
        this.goal = goal;
        this.startX = x;
        this.startY = y;
    }

    @Override
    public void update() {
        if (cleared) {
            message = "CLEAR! You collected every coin.";
            return;
        }

        double dx = 0.0;
        double dy = 0.0;

        if (input.isPressed(Action.LEFT)) {
            dx -= speed;
        }
        if (input.isPressed(Action.RIGHT)) {
            dx += speed;
        }
        if (input.isPressed(Action.UP)) {
            dy -= speed;
        }
        if (input.isPressed(Action.DOWN)) {
            dy += speed;
        }

        moveWithCollision(dx, dy);
        collectCoins();
        checkGoal();
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
                    WIDTH,
                    HEIGHT,
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

    private void collectCoins() {
        for (Coin coin : coins) {
            if (!coin.isCollected()
                    && intersects(
                            getX(),
                            getY(),
                            WIDTH,
                            HEIGHT,
                            coin.getX(),
                            coin.getY(),
                            coin.getWidth(),
                            coin.getHeight()
                    )) {
                coin.collect();
                collectedCount++;
                message = "Coin collected: " + collectedCount + " / " + coins.size();
            }
        }
    }

    private void checkGoal() {
        if (!intersects(
                getX(),
                getY(),
                WIDTH,
                HEIGHT,
                goal.getX(),
                goal.getY(),
                goal.getWidth(),
                goal.getHeight()
        )) {
            return;
        }

        if (collectedCount == coins.size()) {
            cleared = true;
            message = "CLEAR! You reached the goal.";
        } else {
            message = "You need all coins before entering the goal.";
        }
    }

    public void resetToStart() {
        setX(startX);
        setY(startY);
        setPreviousX(startX);
        setPreviousY(startY);
        message = "Hit by enemy! Returned to start.";
    }

    public int getCollectedCount() {
        return collectedCount;
    }

    public boolean isCleared() {
        return cleared;
    }

    public String getMessage() {
        return message;
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        int drawX = (int) lerp(getPreviousX(), getX(), alpha);
        int drawY = (int) lerp(getPreviousY(), getY(), alpha);

        g.setColor(new Color(40, 100, 220));
        g.fillRect(drawX, drawY, WIDTH, HEIGHT);

        g.setColor(Color.WHITE);
        g.drawRect(drawX, drawY, WIDTH, HEIGHT);
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