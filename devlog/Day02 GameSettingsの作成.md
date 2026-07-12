# GameSettingsの作成

## フレームの設定をクラス化する

フレームの設定をクラス化します。設定クラスなので`final`修飾子を付けます。

```java
public final class GameSettings {

    private final int width;
    private final int height;
    private final String title;
    private final boolean isVisible;
    private final boolean centerOnScreen;
    private final boolean exitOnClose;
    private final boolean isResizeable;

    public GameSettings(int width, int height, String title, boolean isVisible, boolean centerOnScreen,
            boolean exitOnClose, boolean isResizeable) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.isVisible = isVisible;
        this.centerOnScreen = centerOnScreen;
        this.exitOnClose = exitOnClose;
        this.isResizeable = isResizeable;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getTitle() {
        return title;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean isCenterOnScreen() {
        return centerOnScreen;
    }

    public boolean isExitOnClose() {
        return exitOnClose;
    }

    public boolean isResizable() {
        return isResizeable;
    }

}


```

将来ここに様々な設定を追加します。この設定を以下のGameWindowクラスに渡し、その設定の基づいてFrameを生成するように記述します。
```java
import javax.swing.JFrame;

public class GameFrame {
    private final JFrame frame = new JFrame();

    public GameFrame(GameSettings set) {
        frame.setTitle(set.getTitle());
        frame.setSize(set.getWidth(), set.getHeight());

        if (set.isExitOnClose()) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
        
        if (set.isCenterOnScreen()) {
            frame.setLocationRelativeTo(null);
        }

        frame.setVisible(set.isVisible());
    }
}
```
mainメソッドでは以下の様に記述してフレームを表示する。非常にすっきりしていることがわかります。
```java
    public static void main(String[] args) {
        GameSettings set = new GameSettings(
                800, 
                600, 
                "Sample Frame", 
                true, 
                true, 
                true
        );
        SwingUtilities.invokeLater(() -> {
            new GameFrame(set);
        });
    }
```



