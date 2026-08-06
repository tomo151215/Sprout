package sample;

import java.awt.Color;
import java.awt.Graphics2D;

import engine.input.Mouse;
import engine.input.MouseButton;
import engine.object.GameObject;

public class MousePointerBox extends GameObject {
    private final Mouse mouse;

    public MousePointerBox(Mouse mouse) {
        super(0, 0);
        this.mouse = mouse;
    }

    @Override
    public void update() {
        setX(mouse.getX());
        setY(mouse.getY());
        if (mouse.isJustPressed(MouseButton.LEFT)) {
            System.out.println("左クリック");
        }
        if (mouse.isJustPressed(MouseButton.RIGHT)) {
            System.out.println("右クリック");
        }
        if (mouse.getWheelRotation() != 0) {
            System.out.println("wheel = " + mouse.getWheelRotation());
        }
    }

    @Override
    public void draw(Graphics2D g, double alpha) {
        int drawX = (int) lerp(getPreviousX(), getX(), alpha);
        int drawY = (int) lerp(getPreviousY(), getY(), alpha);
        g.setColor(Color.RED);
        g.fillRect(drawX - 5, drawY - 5, 10, 10);
    }
}