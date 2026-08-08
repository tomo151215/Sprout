package sample;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import engine.graphics.Camera2D;
import engine.object.GameObject;

public class StatusText extends GameObject {
    private final Camera2D camera;
    private final Player player;
    private final int totalCoins;

    public StatusText(Camera2D camera, Player player, int totalCoins) {
        super(0, 0);

        if (camera == null) {
            throw new IllegalArgumentException("camera must not be null.");
        }
        if (player == null) {
            throw new IllegalArgumentException("player must not be null.");
        }

        this.camera = camera;
        this.player = player;
        this.totalCoins = totalCoins;
    }

    @Override
    public void update() {
        setX(camera.getX() + 20);
        setY(camera.getY() + 30);
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        int drawX = (int) lerp(getPreviousX(), getX(), alpha);
        int drawY = (int) lerp(getPreviousY(), getY(), alpha);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(drawX - 10, drawY - 22, 520, 60, 12, 12);

        g.setColor(Color.WHITE);
        g.drawString(
                "Coins: " + player.getCollectedCount() + " / " + totalCoins,
                drawX,
                drawY
        );

        g.drawString(
                player.getMessage(),
                drawX,
                drawY + 25
        );
    }
}
