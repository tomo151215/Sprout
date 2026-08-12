package sample;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import engine.core.GameEngine;
import engine.core.GameSystem;
import engine.graphics.Camera2D;
import engine.input.InputContext;
import engine.object.GameObject;
import sample.Action;

public final class GameWorld implements GameSystem {
    private final GameEngine engine;
    private final InputContext<Action> input;

    private final int worldWidth = 1800;
    private final int worldHeight = 1000;

    private final List<Block> blocks = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();

    private final Player player;
    private final Goal goal;

    private boolean cleared;
    private boolean failed;

    public GameWorld(GameEngine engine, InputContext<Action> input) {
        if (engine == null) {
            throw new IllegalArgumentException("engine must not be null.");
        }

        if (input == null) {
            throw new IllegalArgumentException("input must not be null.");
        }

        this.engine = engine;
        this.input = input;

        this.player = new Player(80, 80, input, this);
        this.goal = new Goal(1640, 850);

        createStage();
    }

    private void createStage() {
        blocks.add(new Block(260, 160, 260, 50));
        blocks.add(new Block(650, 120, 240, 50));
        blocks.add(new Block(1050, 200, 260, 50));
        blocks.add(new Block(180, 430, 300, 50));
        blocks.add(new Block(620, 520, 320, 50));
        blocks.add(new Block(1080, 610, 280, 50));
        blocks.add(new Block(1420, 760, 190, 50));

        coins.add(new Coin(330, 110));
        coins.add(new Coin(720, 70));
        coins.add(new Coin(1140, 150));
        coins.add(new Coin(290, 380));
        coins.add(new Coin(730, 470));
        coins.add(new Coin(1170, 560));
        coins.add(new Coin(1490, 710));

        enemies.add(new Enemy(350, 340, 240, 500));
        enemies.add(new Enemy(780, 720, 620, 960));
        enemies.add(new Enemy(1250, 360, 1080, 1420));
    }

    public void register() {
        engine.addObject(new WorldBackground(worldWidth, worldHeight));

        for (Block block : blocks) {
            engine.addObject(block);
        }

        for (Coin coin : coins) {
            engine.addObject(coin);
        }

        for (Enemy enemy : enemies) {
            engine.addObject(enemy);
        }

        engine.addObject(goal);
        engine.addObject(player);
        engine.addObject(new ScreenHud(engine.getCamera(), this, input));
    }

    @Override
    public void update() {
        if (input.isJustPressed(Action.RESTART)) {
            reset();
        }
    }

    public void reset() {
        cleared = false;
        failed = false;

        player.reset(80, 80);

        for (Coin coin : coins) {
            coin.reset();
        }

        enemies.get(0).reset(350, 340, 1);
        enemies.get(1).reset(780, 720, -1);
        enemies.get(2).reset(1250, 360, 1);
    }

    public boolean isPlaying() {
        return !cleared && !failed;
    }

    public void clear() {
        cleared = true;
    }

    public void fail() {
        failed = true;
    }

    public boolean isCleared() {
        return cleared;
    }

    public boolean isFailed() {
        return failed;
    }

    public GameObject getPlayer() {
        return player;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<Coin> getCoins() {
        return coins;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public Goal getGoal() {
        return goal;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public int getCollectedCoinCount() {
        int count = 0;

        for (Coin coin : coins) {
            if (coin.isCollected()) {
                count++;
            }
        }

        return count;
    }

    public int getTotalCoinCount() {
        return coins.size();
    }

    public boolean hasCollectedAllCoins() {
        return getCollectedCoinCount() == coins.size();
    }

    public static boolean intersects(
            double ax,
            double ay,
            double aw,
            double ah,
            double bx,
            double by,
            double bw,
            double bh) {
        return ax < bx + bw
                && ax + aw > bx
                && ay < by + bh
                && ay + ah > by;
    }
}

abstract class RectObject extends GameObject {
    private final int width;
    private final int height;

    protected RectObject(
            double x,
            double y,
            int width,
            int height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean intersects(RectObject other) {
        return GameWorld.intersects(
                getX(),
                getY(),
                width,
                height,
                other.getX(),
                other.getY(),
                other.getWidth(),
                other.getHeight());
    }
}

final class Player extends RectObject {
    private static final int SIZE = 34;

    private final InputContext<Action> input;
    private final GameWorld world;

    private int dashFrames;
    private int dashCooldown;

    Player(
            double x,
            double y,
            InputContext<Action> input,
            GameWorld world) {
        super(x, y, SIZE, SIZE);
        this.input = input;
        this.world = world;
    }

    public void reset(double x, double y) {
        setX(x);
        setY(y);
        setPreviousX(x);
        setPreviousY(y);
        dashFrames = 0;
        dashCooldown = 0;
    }

    @Override
    public void update() {
        if (!world.isPlaying()) {
            return;
        }

        double dx = 0;
        double dy = 0;

        if (input.isPressed(Action.LEFT)) {
            dx -= 1.0;
        }

        if (input.isPressed(Action.RIGHT)) {
            dx += 1.0;
        }

        if (input.isPressed(Action.UP)) {
            dy -= 1.0;
        }

        if (input.isPressed(Action.DOWN)) {
            dy += 1.0;
        }

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;
        }

        if (dashCooldown > 0) {
            dashCooldown--;
        }

        if (input.isJustPressed(Action.DASH)
                && dashCooldown == 0
                && (dx != 0 || dy != 0)) {
            dashFrames = 8;
            dashCooldown = 40;
        }

        double speed = dashFrames > 0 ? 11.0 : 4.5;

        if (dashFrames > 0) {
            dashFrames--;
        }

        moveX(dx * speed);
        moveY(dy * speed);
        clampToWorld();
        collectCoins();
        checkEnemyHit();
        checkGoal();
    }

    private void moveX(double amount) {
        setX(getX() + amount);

        for (Block block : world.getBlocks()) {
            if (intersects(block)) {
                if (amount > 0) {
                    setX(block.getX() - getWidth());
                } else if (amount < 0) {
                    setX(block.getX() + block.getWidth());
                }
            }
        }
    }

    private void moveY(double amount) {
        setY(getY() + amount);

        for (Block block : world.getBlocks()) {
            if (intersects(block)) {
                if (amount > 0) {
                    setY(block.getY() - getHeight());
                } else if (amount < 0) {
                    setY(block.getY() + block.getHeight());
                }
            }
        }
    }

    private void clampToWorld() {
        if (getX() < 0) {
            setX(0);
        }

        if (getY() < 0) {
            setY(0);
        }

        if (getX() + getWidth() > world.getWorldWidth()) {
            setX(world.getWorldWidth() - getWidth());
        }

        if (getY() + getHeight() > world.getWorldHeight()) {
            setY(world.getWorldHeight() - getHeight());
        }
    }

    private void collectCoins() {
        for (Coin coin : world.getCoins()) {
            if (!coin.isCollected() && intersects(coin)) {
                coin.collect();
            }
        }
    }

    private void checkEnemyHit() {
        for (Enemy enemy : world.getEnemies()) {
            if (intersects(enemy)) {
                world.fail();
                return;
            }
        }
    }

    private void checkGoal() {
        if (world.hasCollectedAllCoins() && intersects(world.getGoal())) {
            world.clear();
        }
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        int x = (int) lerp(getPreviousX(), getX(), alpha);
        int y = (int) lerp(getPreviousY(), getY(), alpha);

        if (dashFrames > 0) {
            g.setColor(new Color(90, 230, 255));
        } else {
            g.setColor(new Color(80, 220, 130));
        }

        g.fillRect(x, y, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.drawRect(x, y, getWidth(), getHeight());
    }
}

final class Block extends RectObject {
    Block(double x, double y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        g.setColor(new Color(105, 115, 132));
        g.fillRect(
                (int) getX(),
                (int) getY(),
                getWidth(),
                getHeight());

        g.setColor(new Color(205, 215, 230));
        g.drawRect(
                (int) getX(),
                (int) getY(),
                getWidth(),
                getHeight());
    }
}

final class Coin extends RectObject {
    private boolean collected;

    Coin(double x, double y) {
        super(x, y, 24, 24);
    }

    public void collect() {
        collected = true;
    }

    public void reset() {
        collected = false;
    }

    public boolean isCollected() {
        return collected;
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        if (collected) {
            return;
        }

        g.setColor(new Color(255, 220, 70));
        g.fillOval(
                (int) getX(),
                (int) getY(),
                getWidth(),
                getHeight());

        g.setColor(Color.WHITE);
        g.drawOval(
                (int) getX(),
                (int) getY(),
                getWidth(),
                getHeight());
    }
}

final class Enemy extends RectObject {
    private final double minX;
    private final double maxX;

    private double speed = 3.0;
    private int direction = 1;

    Enemy(double x, double y, double minX, double maxX) {
        super(x, y, 36, 36);
        this.minX = minX;
        this.maxX = maxX;
    }

    public void reset(double x, double y, int direction) {
        setX(x);
        setY(y);
        setPreviousX(x);
        setPreviousY(y);
        this.direction = direction;
    }

    @Override
    public void update() {
        setX(getX() + speed * direction);

        if (getX() < minX) {
            setX(minX);
            direction = 1;
        }

        if (getX() + getWidth() > maxX) {
            setX(maxX - getWidth());
            direction = -1;
        }
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        int x = (int) lerp(getPreviousX(), getX(), alpha);
        int y = (int) lerp(getPreviousY(), getY(), alpha);

        g.setColor(new Color(230, 75, 90));
        g.fillRect(x, y, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.drawRect(x, y, getWidth(), getHeight());
    }
}

final class Goal extends RectObject {
    Goal(double x, double y) {
        super(x, y, 70, 70);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        g.setColor(new Color(40, 180, 95));
        g.fillRect(
                (int) getX(),
                (int) getY(),
                getWidth(),
                getHeight());

        g.setColor(Color.WHITE);
        g.drawRect(
                (int) getX(),
                (int) getY(),
                getWidth(),
                getHeight());

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString(
                "GOAL",
                (int) getX() + 13,
                (int) getY() + 42);
    }
}

final class WorldBackground extends GameObject {
    private final int width;
    private final int height;

    WorldBackground(int width, int height) {
        super(0, 0);
        this.width = width;
        this.height = height;
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        g.setColor(new Color(30, 38, 48));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(45, 55, 70));

        for (int x = 0; x <= width; x += 80) {
            g.drawLine(x, 0, x, height);
        }

        for (int y = 0; y <= height; y += 80) {
            g.drawLine(0, y, width, y);
        }

        g.setColor(new Color(90, 100, 120));
        g.setStroke(new BasicStroke(4));
        g.drawRect(0, 0, width, height);
    }
}

final class ScreenHud extends GameObject {
    private final Camera2D camera;
    private final GameWorld world;
    private final InputContext<Action> input;

    ScreenHud(
            Camera2D camera,
            GameWorld world,
            InputContext<Action> input) {
        super(0, 0);
        this.camera = camera;
        this.world = world;
        this.input = input;
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        int screenX = (int) camera.getX();
        int screenY = (int) camera.getY();

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.setColor(Color.WHITE);

        g.drawString(
                "Coins: "
                        + world.getCollectedCoinCount()
                        + " / "
                        + world.getTotalCoinCount(),
                screenX + 20,
                screenY + 28);

        g.drawString(
                "Move: WASD/Arrow  Dash: SPACE  Restart: R",
                screenX + 20,
                screenY + 52);

        int mouseWorldX = (int) camera.screenToWorldX(input.getMouseX());

        int mouseWorldY = (int) camera.screenToWorldY(input.getMouseY());

        g.drawString(
                "Mouse World: " + mouseWorldX + ", " + mouseWorldY,
                screenX + 20,
                screenY + 76);

        if (world.isCleared()) {
            drawCenterMessage(
                    g,
                    screenX,
                    screenY,
                    "CLEAR!",
                    new Color(120, 255, 160));
        } else if (world.isFailed()) {
            drawCenterMessage(
                    g,
                    screenX,
                    screenY,
                    "GAME OVER",
                    new Color(255, 110, 120));
        }
    }

    private void drawCenterMessage(
            Graphics2D g,
            int screenX,
            int screenY,
            String text,
            Color color) {
        g.setFont(new Font("SansSerif", Font.BOLD, 46));
        g.setColor(color);
        g.drawString(text, screenX + 275, screenY + 280);

        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("Press R to restart", screenX + 315, screenY + 320);
    }

}
