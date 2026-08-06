package sample;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import engine.input.InputContext;
import engine.object.GameObject;

public class PacmanWorld extends GameObject {
    public static final int TILE = 24;
    public static final int UI_HEIGHT = 48;

    private static final String[] LEVEL = {
            "#####################",
            "#P........#........G#",
            "#.###.###.#.###.###.#",
            "#...................#",
            "#.###.#.#####.#.###.#",
            "#.....#...#...#.....#",
            "#####.###.#.###.#####",
            "#.........G.........#",
            "#####.###.#.###.#####",
            "#.....#...#...#.....#",
            "#.###.#.#####.#.###.#",
            "#G........#.........#",
            "#####################"
    };

    public static final int COLS = LEVEL[0].length();
    public static final int ROWS = LEVEL.length;
    public static final int WIDTH = COLS * TILE;
    public static final int HEIGHT = ROWS * TILE + UI_HEIGHT;

    private final InputContext<Action> input;
    private final Random random = new Random();

    private char[][] map;
    private Player player;
    private final List<Ghost> ghosts = new ArrayList<>();

    private int score;
    private int remainingDots;
    private boolean gameOver;
    private boolean gameClear;

    public PacmanWorld(InputContext<Action> input) {
        super(0, 0);
        this.input = input;
        reset();
    }

    private void reset() {
        map = new char[ROWS][COLS];
        ghosts.clear();

        score = 0;
        remainingDots = 0;
        gameOver = false;
        gameClear = false;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                char ch = LEVEL[row].charAt(col);

                if (ch == 'P') {
                    player = new Player(col * TILE, row * TILE);
                    map[row][col] = '.';
                    remainingDots++;
                } else if (ch == 'G') {
                    ghosts.add(new Ghost(col * TILE, row * TILE));
                    map[row][col] = '.';
                    remainingDots++;
                } else {
                    map[row][col] = ch;

                    if (ch == '.') {
                        remainingDots++;
                    }
                }
            }
        }

        input.clear();
    }

    @Override
    public void update() {
        if (input.isJustPressed(Action.RESTART)) {
            reset();
            return;
        }

        if (gameOver || gameClear) {
            return;
        }

        player.update();

        for (Ghost ghost : ghosts) {
            ghost.update();
        }

        eatDot();
        checkGhostCollision();

        if (remainingDots <= 0) {
            gameClear = true;
        }
    }

    private void eatDot() {
        int centerCol = pixelToCol(player.centerX());
        int centerRow = pixelToRow(player.centerY());

        if (!isInside(centerRow, centerCol)) {
            return;
        }

        if (map[centerRow][centerCol] == '.') {
            map[centerRow][centerCol] = ' ';
            score += 10;
            remainingDots--;
        }
    }

    private void checkGhostCollision() {
        for (Ghost ghost : ghosts) {
            double dx = player.centerX() - ghost.centerX();
            double dy = player.centerY() - ghost.centerY();
            double distanceSq = dx * dx + dy * dy;

            int radius = TILE / 2;

            if (distanceSq <= radius * radius) {
                gameOver = true;
                return;
            }
        }
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        drawMap(g);
        drawPlayer(g);
        drawGhosts(g);
        drawUi(g);
    }

    private void drawMap(Graphics2D g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = col * TILE;
                int y = row * TILE;

                if (map[row][col] == '#') {
                    g.setColor(new Color(0, 40, 180));
                    g.fillRoundRect(x + 2, y + 2, TILE - 4, TILE - 4, 8, 8);

                    g.setColor(new Color(0, 100, 255));
                    g.drawRoundRect(x + 2, y + 2, TILE - 4, TILE - 4, 8, 8);
                } else if (map[row][col] == '.') {
                    g.setColor(new Color(255, 220, 160));
                    int dotSize = 5;
                    g.fillOval(
                            x + TILE / 2 - dotSize / 2,
                            y + TILE / 2 - dotSize / 2,
                            dotSize,
                            dotSize);
                }
            }
        }
    }

    private void drawPlayer(Graphics2D g) {
        int x = (int) player.x;
        int y = (int) player.y;

        int mouth = 35 + (int) (Math.abs(Math.sin(player.animation)) * 20);
        int startAngle = switch (player.direction) {
            case RIGHT, NONE -> mouth;
            case LEFT -> 180 + mouth;
            case UP -> 90 + mouth;
            case DOWN -> 270 + mouth;
        };

        int extent = 360 - mouth * 2;

        g.setColor(Color.YELLOW);
        g.fillArc(
                x + 2,
                y + 2,
                TILE - 4,
                TILE - 4,
                startAngle,
                extent);
    }

    private void drawGhosts(Graphics2D g) {
        for (Ghost ghost : ghosts) {
            int x = (int) ghost.x;
            int y = (int) ghost.y;

            g.setColor(ghost.color);
            g.fillRoundRect(x + 3, y + 3, TILE - 6, TILE - 6, 12, 12);

            g.setColor(Color.WHITE);
            g.fillOval(x + 7, y + 8, 5, 6);
            g.fillOval(x + 14, y + 8, 5, 6);

            g.setColor(Color.BLACK);
            g.fillOval(x + 9, y + 10, 2, 2);
            g.fillOval(x + 16, y + 10, 2, 2);
        }
    }

    private void drawUi(Graphics2D g) {
        int y = ROWS * TILE;

        g.setColor(Color.BLACK);
        g.fillRect(0, y, WIDTH, UI_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString("SCORE: " + score, 16, y + 30);
        g.drawString("R: RESTART", WIDTH - 120, y + 30);

        if (gameOver) {
            drawCenterMessage(g, "GAME OVER");
        }

        if (gameClear) {
            drawCenterMessage(g, "GAME CLEAR");
        }
    }

    private void drawCenterMessage(Graphics2D g, String message) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));

        int textWidth = g.getFontMetrics().stringWidth(message);
        int x = (WIDTH - textWidth) / 2;
        int y = ROWS * TILE / 2;

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, y - 50, WIDTH, 80);

        g.setColor(Color.WHITE);
        g.drawString(message, x, y);
    }

    private boolean canMove(double x, double y) {
        int left = pixelToCol(x + 2);
        int right = pixelToCol(x + TILE - 3);
        int top = pixelToRow(y + 2);
        int bottom = pixelToRow(y + TILE - 3);

        return !isWall(top, left)
                && !isWall(top, right)
                && !isWall(bottom, left)
                && !isWall(bottom, right);
    }

    private boolean canMoveInDirection(double x, double y, Direction direction, double speed) {
        double nextX = x + direction.dx() * speed;
        double nextY = y + direction.dy() * speed;
        return canMove(nextX, nextY);
    }

    private boolean isWall(int row, int col) {
        if (!isInside(row, col)) {
            return true;
        }
        return map[row][col] == '#';
    }

    private boolean isInside(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    private int pixelToCol(double x) {
        return (int) (x / TILE);
    }

    private int pixelToRow(double y) {
        return (int) (y / TILE);
    }

    private boolean isTileAligned(double value) {
        double tile = value / TILE;
        return Math.abs(tile - Math.round(tile)) < 0.01;
    }

    private boolean isGridAligned(double x, double y) {
        return isTileAligned(x) && isTileAligned(y);
    }

    private final class Player {
        private double x;
        private double y;

        private Direction direction = Direction.NONE;
        private Direction requestedDirection = Direction.NONE;

        private final double speed = 2.0;
        private double animation;

        // 交差点の何px手前から曲がり予約を受け付けるか
        private static final double TURN_BUFFER_DISTANCE = 14.0;

        private Player(double x, double y) {
            this.x = x;
            this.y = y;
        }

        private void update() {
            Direction inputDirection = readInputDirection();

            // キーを離したら止まる
            if (inputDirection == Direction.NONE) {
                direction = Direction.NONE;
                requestedDirection = Direction.NONE;
                return;
            }

            requestedDirection = inputDirection;

            // 停止中なら、押された方向へ直接進む
            if (direction == Direction.NONE) {
                tryMoveFromStop();
                return;
            }

            // 同じ方向を押しているなら、そのまま進む
            if (requestedDirection == direction) {
                moveForwardOrStop();
                return;
            }

            // 逆方向なら、すぐ反転する
            if (requestedDirection == direction.opposite()) {
                if (canMoveInDirection(x, y, requestedDirection, speed)) {
                    direction = requestedDirection;
                    moveForwardOrStop();
                } else {
                    direction = Direction.NONE;
                }
                return;
            }

            // 直角に曲がりたい場合
            if (isPerpendicular(direction, requestedDirection)) {
                handleBufferedTurn();
                return;
            }

            moveForwardOrStop();
        }

        private Direction readInputDirection() {
            if (input.isPressed(Action.UP)) {
                return Direction.UP;
            }

            if (input.isPressed(Action.RIGHT)) {
                return Direction.RIGHT;
            }

            if (input.isPressed(Action.DOWN)) {
                return Direction.DOWN;
            }

            if (input.isPressed(Action.LEFT)) {
                return Direction.LEFT;
            }

            return Direction.NONE;
        }

        private void tryMoveFromStop() {
            snapToLaneFor(requestedDirection);

            if (canMoveInDirection(x, y, requestedDirection, speed)) {
                direction = requestedDirection;
                moveForwardOrStop();
            } else {
                direction = Direction.NONE;
            }
        }

        private void handleBufferedTurn() {
            TurnPoint turnPoint = nextTurnPoint();

            // 次の交差点が遠すぎるなら、今の方向へ進む
            if (turnPoint.distance > TURN_BUFFER_DISTANCE) {
                moveForwardOrStop();
                return;
            }

            // 次の交差点で、曲がりたい方向へ進めるか調べる
            if (!canMoveInDirection(turnPoint.x, turnPoint.y, requestedDirection, speed)) {
                moveForwardOrStop();
                return;
            }

            // 交差点に到達していないなら、交差点まで進む
            if (turnPoint.distance > speed) {
                moveTowardTurnPoint(turnPoint);
                return;
            }

            // 交差点に到達したので、座標を補正して曲がる
            x = turnPoint.x;
            y = turnPoint.y;
            direction = requestedDirection;

            moveForwardOrStop();
        }

        private TurnPoint nextTurnPoint() {
            double targetX = x;
            double targetY = y;

            if (direction == Direction.RIGHT) {
                targetX = Math.ceil(x / TILE) * TILE;
            } else if (direction == Direction.LEFT) {
                targetX = Math.floor(x / TILE) * TILE;
            } else if (direction == Direction.DOWN) {
                targetY = Math.ceil(y / TILE) * TILE;
            } else if (direction == Direction.UP) {
                targetY = Math.floor(y / TILE) * TILE;
            }

            double distance;

            if (direction == Direction.RIGHT || direction == Direction.LEFT) {
                distance = Math.abs(targetX - x);
            } else {
                distance = Math.abs(targetY - y);
            }

            return new TurnPoint(targetX, targetY, distance);
        }

        private void moveTowardTurnPoint(TurnPoint turnPoint) {
            double move = Math.min(speed, turnPoint.distance);

            x += direction.dx() * move;
            y += direction.dy() * move;
            animation += 0.25;
        }

        private void moveForwardOrStop() {
            if (canMoveInDirection(x, y, direction, speed)) {
                x += direction.dx() * speed;
                y += direction.dy() * speed;
                animation += 0.25;
            } else {
                direction = Direction.NONE;
            }
        }

        private boolean isPerpendicular(Direction a, Direction b) {
            boolean aHorizontal = a == Direction.LEFT || a == Direction.RIGHT;
            boolean aVertical = a == Direction.UP || a == Direction.DOWN;
            boolean bHorizontal = b == Direction.LEFT || b == Direction.RIGHT;
            boolean bVertical = b == Direction.UP || b == Direction.DOWN;

            return (aHorizontal && bVertical) || (aVertical && bHorizontal);
        }

        private void snapToLaneFor(Direction direction) {
            if (direction == Direction.UP || direction == Direction.DOWN) {
                x = Math.round(x / TILE) * TILE;
            }

            if (direction == Direction.LEFT || direction == Direction.RIGHT) {
                y = Math.round(y / TILE) * TILE;
            }
        }

        private double centerX() {
            return x + TILE / 2.0;
        }

        private double centerY() {
            return y + TILE / 2.0;
        }

        private final class TurnPoint {
            private final double x;
            private final double y;
            private final double distance;

            private TurnPoint(double x, double y, double distance) {
                this.x = x;
                this.y = y;
                this.distance = distance;
            }
        }
    }

    private final class Ghost {
        private double x;
        private double y;
        private Direction direction = Direction.LEFT;
        private final double speed = 1.5;
        private final Color color;

        private Ghost(double x, double y) {
            this.x = x;
            this.y = y;
            this.color = randomGhostColor();
        }

        private void update() {
            if (isGridAligned(x, y)) {
                chooseDirection();
            }

            if (canMoveInDirection(x, y, direction, speed)) {
                x += direction.dx() * speed;
                y += direction.dy() * speed;
            } else {
                chooseDirection();
            }
        }

        private void chooseDirection() {
            List<Direction> candidates = new ArrayList<>();

            for (Direction d : List.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)) {
                if (d == direction.opposite()) {
                    continue;
                }

                if (canMoveInDirection(x, y, d, speed)) {
                    candidates.add(d);
                }
            }

            if (candidates.isEmpty()) {
                Direction reverse = direction.opposite();
                if (canMoveInDirection(x, y, reverse, speed)) {
                    direction = reverse;
                }
                return;
            }

            direction = candidates.get(random.nextInt(candidates.size()));
        }

        private double centerX() {
            return x + TILE / 2.0;
        }

        private double centerY() {
            return y + TILE / 2.0;
        }

        private Color randomGhostColor() {
            Color[] colors = {
                    Color.PINK,
                    Color.CYAN,
                    Color.ORANGE,
                    Color.RED
            };
            return colors[random.nextInt(colors.length)];
        }
    }
}