# Day02: GameWindowクラスの実装（JFrameとCanvasの統合）

## 概要

これまでの `GameFrame` を発展させ、OS上のウィンドウ枠を管理する `JFrame` と、実際のゲーム画面を描画する `Canvas` をひとつのクラス（`GameWindow`）に統合します。
この設計により、ウィンドウサイズと実際のゲーム描画領域のサイズを正確に分離して管理できるようになります。

## 実装コード

`JFrame` と `Canvas` をフィールドとして持ち、設定クラス（`GameSettings`）の値をもとにウィンドウを組み立てる実装です。

```java
import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;

public class GameWindow {
    private final JFrame frame = new JFrame();
    private final Canvas canvas = new Canvas();

    public GameWindow(GameSettings set) {
        // 1. Canvasの準備
        canvas.setPreferredSize(new Dimension(set.getWidth(), set.getHeight()));
        canvas.setFocusable(true);

        // 2. ウィンドウの基本設定
        frame.setTitle(set.getTitle());
        frame.setResizable(set.getIsResizeable());
        
        if (set.isExitOnClose()) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }

        // 3. コンポーネントの配置（追加順序が重要）
        frame.add(canvas); 
        frame.pack(); 

        // 4. サイズ確定後に中央寄せ
        if (set.isCenterOnScreen()) {
            frame.setLocationRelativeTo(null);
        }
    }

    public void show(GameSettings set){
        frame.setVisible(set.isVisible());

        // ウィンドウが表示されたら、Canvasにキーボード入力を集中させる
        if (set.isVisible()) {
            canvas.requestFocusInWindow();
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }
}

```

## 各設定の役割と重要な手順

### `setPreferredSize` (Canvasのサイズ指定)

`setPreferredSize` は「この部品はこのサイズが望ましい」とコンポーネントの推奨サイズを指定するメソッドです。後述する `pack()` を実行した際、この推奨サイズを基準にウィンドウ全体の大きさが計算されます。

```java
canvas.setPreferredSize(new Dimension(set.getWidth(), set.getHeight()));

```

### `setFocusable` (キーボード入力の受付)

ゲームの操作（キーボード入力）は、**フォーカスを持っている部品**にのみ届きます。Canvas上でキー入力を検知できるようにするため、必ず `true` に設定します。

```java
canvas.setFocusable(true);

```

### `setResizable` (ウィンドウ枠の可変設定)

画面表示後、プレイヤーがマウスでウィンドウの端を引っ張ってサイズを変更できるかどうかを設定します。
ゲームの場合、画面サイズが変わると描画ロジックが崩れることが多いため、基本的には `false`（固定）にすることが推奨されます。一応GameSettingsから変更できるようにします。

```java
frame.setResizable(set.getIsResizeable());

```

### `pack` (サイズの最終調整と確定)

内部部品（Canvas）の推奨サイズに合わせて、外枠（JFrame）のサイズを自動調整します。
**必ずCanvasを `add()` した後に `pack()` を呼び出す**という順番が重要です。

```java
frame.add(canvas);
frame.pack();

```

---

## なぜ表示処理を `show()` メソッドに分離するのか？

コンストラクタ内で `setVisible(true)` を呼ばず、あえて `show()` メソッドを独立させているのには、ゲーム開発における明確な設計原則（理由）があります。

* **オブジェクトの「初期化（静的）」と「起動（動的）」の分離**
コンストラクタは、メモリ上にオブジェクト（設計図）を組み立てる「初期化」に専念すべきです。一方、画面を表示する処理はOS側へリソースを要請したり、描画スレッドを起動させる「実行（アクション）」にあたるため、メソッドを分けるのがオブジェクト指向の基本原則に適しています。
* **不完全な状態（白飛び・チラつき）の防止**
コンストラクタ内で画面を可視化してしまうと、画像やステージデータなどの読み込み・配置が完了していない不完全な状態でOS側への描画命令が走ってしまい、一瞬画面が真っ白に化ける（白飛びする）などの描画バグの原因になります。
* **外部からの初期設定（パラメータ注入）の猶予確保**
インスタンスが生成されてから画面が表示されるまでの間に「隙間（猶予）」を作ることで、外部からデータ（初期ステージ番号や初期配置など）を安全にセットし、準備が完全に整ってから一発で画面を表示できるようになります。
* **ゲームループとの完全な同時起動**
「画面の実体化」と「ゲームの時間を動かし始めること（タイマースタート）」は、同時に行うべき一連のアクションです。これらを `show()` の呼び出しタイミングに合わせることで、描画とゲームロジックの開始タイミングにミリ秒単位のズレが生じなくなります。

### フォーカスの要求タイミングについて

```java
canvas.requestFocusInWindow();

```

このメソッドはCanvasにフォーカスを要求するものですが、**画面が完全に表示される前（コンストラクタ内など）に呼んでも失敗します。**
そのため、`show()` メソッド内で `setVisible(true)` を実行し、ウィンドウがOS上に実体化した直後に呼び出す必要があります。（※OSの描画タイミングによってはこれでも失敗することがあるため、後続の実装でマウスクリック時にフォーカスを取り直すなどの安全策を入れることもあります）