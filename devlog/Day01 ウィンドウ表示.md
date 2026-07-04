# Day01: ウィンドウ表示を行う

## 概要

Swingの `JFrame` を使って空のウィンドウを表示します。ゲーム処理はまだ作りません。「実行し、GUIウィンドウを持ち、閉じると終了する」という最小単位を確実に実装します。

## Swingとは

SwingはJava標準ライブラリに含まれるGUIツールキットです。`javax.swing` パッケージにあります。ゲームエンジン専用ではありませんが、標準ライブラリだけでウィンドウ、入力、描画を試すには十分です。

Swingの代表的な部品:

```text
JFrame: ウィンドウ
JPanel: Swingの描画や部品配置によく使うパネル
JButton: ボタン
JLabel: ラベル
```

また、`JFrame` はゲーム画面そのものではありません。ウィンドウの外枠です。ゲーム画面は後で `Canvas` として中に置きます。

## EDTとは

Swingには Event Dispatch Thread、略してEDTという専用スレッドがあります。EDTはGUIイベントを処理します。

```text
マウスクリック
キー入力
ウィンドウ再描画
ボタン操作
GUI部品の更新
```

Swingの部品を作ったり変更したりする処理は、基本的にEDT上で実行します。そのため `SwingUtilities.invokeLater` を使います。Swingでは、ウィンドウを表示するとEDTなどのスレッドが動き続けるため、ウィンドウを閉じるまでアプリケーションが残ります。

```java
SwingUtilities.invokeLater(() -> {
    // Swingの部品作成・表示
});
```

## 最小コード

```java
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("My Java Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
```

## それぞれの行の意味

```java
JFrame frame = new JFrame("My Java Game");
```

ウィンドウを作ります。文字列はタイトルバーに表示されます。

```java
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
```

ウィンドウを閉じたらJavaプロセスも終了する設定です。これを指定しないと、ウィンドウを閉じてもプロセスが残ることがあります。

```java
frame.setSize(800, 600);
```

ウィンドウ全体のサイズを指定します。タイトルバーや枠も含む点に注意してください。後日、ゲーム描画領域そのものは `Canvas` に指定します。

```java
frame.setLocationRelativeTo(null);
```

画面中央付近に表示します。

```java
frame.setVisible(true);
```

ウィンドウを実際に表示します。作っただけでは画面に出ません。


## 拡張

外枠の設定をクラス化します。

```java
public final class GameSettings {

    private final int width;
    private final int height;
    private final String title;
    private final boolean isVisible;
    private final boolean centerOnScreen;
    private final boolean exitOnClose;

    public GameSettings(int width, int height, String title, boolean isVisible, boolean centerOnScreen,
            boolean exitOnClose) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.isVisible = isVisible;
        this.centerOnScreen = centerOnScreen;
        this.exitOnClose = exitOnClose;
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

}

```

将来ここにFPS、スケール、音声ON/OFF、デバッグ表示などを追加します。この設定を以下のGameWindowクラスに渡し、その設定の基づいてFrameを生成するように記述します。
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
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true);
        SwingUtilities.invokeLater(() -> {
            new GameFrame(set);
        });
    }
```


