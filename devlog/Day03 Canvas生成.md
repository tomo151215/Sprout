# Day03: GameWindowクラスの実装（JFrameとCanvasの統合）

これまでの `GameFrame` を発展させ、OS上のウィンドウ枠を管理する `JFrame` と、実際のゲーム画面を描画する `Canvas` をひとつのクラス（`GameWindow`）に統合します。
この設計により、ウィンドウサイズと実際のゲーム描画領域のサイズを正確に分離して管理できるようになります。

## 実装コード

`JFrame` と `Canvas` をフィールドとして持ち、設定クラス（`GameSettings`）の値をもとにウィンドウを組み立てる実装です。

```java
import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;

public final class GameWindow {
    private final JFrame frame = new JFrame();
    private final Canvas canvas = new Canvas();
    private final GameSettings set;

    public GameWindow(GameSettings set) {
        this.set = set;

        // Canvasの準備
        canvas.setPreferredSize(new Dimension(
                set.getWidth(), 
                set.getHeight()
        ));
        canvas.setFocusable(true);

        // ウィンドウの基本設定
        frame.setTitle(set.getTitle());
        frame.setResizable(set.isResizable());

        if (set.isExitOnClose()) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }

        // コンポーネントの配置
        frame.add(canvas);
        frame.pack();

        // サイズ確定後に中央寄せ
        if (set.isCenterOnScreen()) {
            frame.setLocationRelativeTo(null);
        }
    }

    public void show() {
        frame.setVisible(set.isVisible());
        if (set.isVisible()) {
            canvas.requestFocusInWindow();
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }
}

```

#### `setPreferredSize` (Canvasのサイズ指定)
```java
canvas.setPreferredSize(new Dimension(
        set.getWidth(), 
        set.getHeight()
));
```

`setPreferredSize` は「この部品はこのサイズが望ましい」とコンポーネントの推奨サイズを指定するメソッドです。この場合、Canvasに対して設定しているので、ゲーム描画画面の推奨サイズを指定しています。後述する `pack()` を実行した際、この推奨サイズを基準にフレーム含めたウィンドウ全体の大きさが計算されます。

#### `setFocusable` (キーボード入力の受付)
```java
canvas.setFocusable(true);
```
ゲームの操作（キーボード入力）は、**フォーカスを持っている部品**にのみ届きます。Canvas上でキー入力を検知できるようにするため、必ず `true` に設定します。

#### `setResizable` (ウィンドウ枠の可変設定)
```java
frame.setResizable(set.getIsResizeable());
```
画面表示後、プレイヤーがマウスでウィンドウの端を引っ張ってサイズを変更できるかどうかを設定します。
ゲームの場合、画面サイズが変わると描画ロジックが崩れることが多いため、基本的には `false`（固定）にすることが推奨されます。一応GameSettingsから変更できるようにします。

#### `pack` (サイズの最終調整と確定)
```java
frame.add(canvas);
frame.pack();
```
内部部品（Canvas）の推奨サイズに合わせて、外枠（JFrame）のサイズを自動調整します。
**必ずCanvasを `add()` した後に `pack()` を呼び出す**という順番が重要です。

#### `getCanvas()`メソッド
```java
public Canvas getCanvas() {
    return canvas;
}
```
ゲーム内容の描画を行う外部クラスから描画する画用紙（Canvasオブジェクト）を取得するためのメソッドです。後に使うので現時点で作成しておきます。

## コンストラクタで`setVisible`を呼び出さない

コンストラクタで `setVisible(true)` を呼ばず、`show()` メソッドを独立させるのには、以下の明確な理由があります。

#### **1. 「初期化」と「実行」の分離**
コンストラクタはオブジェクトの準備（初期化）に専念し、画面表示という「アクション（実行）」と分けるのが設計の基本です。
#### **2. 描画バグ（白飛び・チラつき）の防止**
画像データの読み込みや配置が終わる前に画面が可視化され、一瞬真っ白に表示されてしまうのを防ぎます。
#### **3. 初期設定を行う「猶予」の確保**
インスタンスの生成から画面表示までの間に、ステージ情報などの初期データを安全にセットする余裕を作ります。
#### **4. ゲームループとの同期**
「画面の表示」と「ゲームタイマーの開始」のタイミングを `show()` で合わせることで、描画とロジックのズレをなくします。

### フォーカスの要求タイミングについて

```java
canvas.requestFocusInWindow();
```

このメソッドはCanvasにフォーカスを要求するものですが、**画面が完全に表示される前（コンストラクタ内など）に呼んでも失敗します。**
そのため、`show()` メソッド内で `setVisible(true)` を実行し、ウィンドウがOS上に実体化した直後に呼び出す必要があります。

