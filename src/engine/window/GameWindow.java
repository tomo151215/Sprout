package engine.window;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

import javax.swing.JFrame;

import engine.core.GameSettings;
import engine.input.Keyboard;
import engine.input.Mouse;

public final class GameWindow {
    private final GameSettings settings;
    private final Keyboard keyboard;
    private final Mouse mouse;
    private final Runnable closeRequestHandler;

    private final JFrame frame;
    private final Canvas canvas;

    public GameWindow(
            GameSettings settings,
            Keyboard keyboard,
            Mouse mouse,
            Runnable closeRequestHandler) {

        this.settings = Objects.requireNonNull(settings, "settings must not be null.");
        this.keyboard = Objects.requireNonNull(keyboard, "keyboard must not be null.");
        this.mouse = Objects.requireNonNull(mouse, "mouse must not be null.");
        this.closeRequestHandler = Objects.requireNonNull(
                closeRequestHandler,
                "closeRequestHandler must not be null.");

        WindowComponents components = SwingExecutor.callAndWait(this::createComponents);
        this.frame = components.frame();
        this.canvas = components.canvas();
    }

    public void open() {
        SwingExecutor.runAndWait(() -> {
            frame.setVisible(settings.isVisible());

            if (settings.isVisible()) {
                requestCanvasFocus();
            }
        });
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void close() {
        SwingExecutor.runAndWait(frame::dispose);
    }

    private WindowComponents createComponents() {
        JFrame newFrame = new JFrame();
        Canvas newCanvas = new Canvas();

        configureCanvas(newCanvas);
        configureFrame(newFrame, newCanvas);

        return new WindowComponents(newFrame, newCanvas);
    }

    private void configureCanvas(Canvas targetCanvas) {
        targetCanvas.setPreferredSize(new Dimension(settings.getWidth(), settings.getHeight()));
        targetCanvas.setFocusable(true);

        targetCanvas.addKeyListener(keyboard);
        targetCanvas.addMouseListener(mouse);
        targetCanvas.addMouseMotionListener(mouse);
        targetCanvas.addMouseWheelListener(mouse);

        targetCanvas.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
                clearInputState();
            }
        });

        targetCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                targetCanvas.requestFocusInWindow();
            }
        });
    }

    private void configureFrame(JFrame targetFrame, Canvas targetCanvas) {
        targetFrame.setTitle(settings.getTitle());
        targetFrame.setResizable(settings.isResizable());
        targetFrame.setDefaultCloseOperation(closeOperation());
        targetFrame.addWindowFocusListener(createWindowFocusListener(targetCanvas));
        targetFrame.addWindowListener(createWindowCloseListener());
        targetFrame.add(targetCanvas);
        targetFrame.pack();

        if (settings.isCenterOnScreen()) {
            targetFrame.setLocationRelativeTo(null);
        }
    }

    private WindowAdapter createWindowFocusListener(Canvas targetCanvas) {
        return new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent event) {
                clearInputState();
            }

            @Override
            public void windowGainedFocus(WindowEvent event) {
                targetCanvas.requestFocusInWindow();
            }
        };
    }

    private WindowAdapter createWindowCloseListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (settings.isExitOnClose()) {
                    closeRequestHandler.run();
                }
            }
        };
    }

    private int closeOperation() {
        return settings.isExitOnClose()
                ? JFrame.EXIT_ON_CLOSE
                : JFrame.DO_NOTHING_ON_CLOSE;
    }

    private void requestCanvasFocus() {
        canvas.requestFocusInWindow();
    }

    private void clearInputState() {
        keyboard.clear();
        mouse.clear();
    }

    private record WindowComponents(JFrame frame, Canvas canvas) {
    }
}
