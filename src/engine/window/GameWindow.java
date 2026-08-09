package engine.window;

import javax.swing.JFrame;

import engine.core.GameSettings;
import engine.input.Keyboard;
import engine.input.Mouse;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class GameWindow {
    private final JFrame frame = new JFrame();
    private final Canvas canvas = new Canvas();
    private final GameSettings set;
    private final Keyboard keyboard;

    public GameWindow(GameSettings set, Keyboard keyboard, Mouse mouse) {
        this.set = set;
        this.keyboard = keyboard;

        canvas.setPreferredSize(new Dimension(set.getWidth(), set.getHeight()));
        canvas.addKeyListener(keyboard);
        canvas.addMouseListener(mouse);
        canvas.addMouseMotionListener(mouse);
        canvas.addMouseWheelListener(mouse);
        canvas.setFocusable(true);

        canvas.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                GameWindow.this.keyboard.clear();
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestCanvasFocus();
            }
        });

        frame.setTitle(set.getTitle());
        frame.setResizable(set.isResizable());

        if (set.isExitOnClose()) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }

        frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                GameWindow.this.keyboard.clear();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                requestCanvasFocus();
            }
        });

        frame.add(canvas);
        frame.pack();

        if (set.isCenterOnScreen()) {
            frame.setLocationRelativeTo(null);
        }
    }

    public void show() {
        frame.setVisible(set.isVisible());
        if (set.isVisible()) {
            requestCanvasFocus();
        }
    }

    public void requestCanvasFocus() {
        canvas.requestFocusInWindow();
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
