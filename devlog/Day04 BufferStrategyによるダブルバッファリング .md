# Day03: BufferStrategyによるトリプルバッファリング
Swingの標準コンポーネント（`JPanel`など）は、自動的にダブルバッファリングが有効になっています。一方で、`Canvas`に描画する場合は自動では有効にならないため、自分でバッファリングを実装する必要があります。
Javaでは、そのための仕組みとして **`BufferStrategy`クラス** が用意されています。本章では、`BufferStrategy`を利用してゲーム画面を描画する方法を学びます。

## バッファリングとは
ゲームでは1秒間に何十回も画面を書き換えます。もし画面に直接描画すると、

- 背景を消す
- キャラクターを描く
- 敵を描く
- UIを描く

という途中経過までユーザーに見えてしまいます。これによって画面がちらつく（フリッカー）現象が発生します。そこで、画面には直接描画せず、

1. 裏側のバックバッファへ最後まで描画する
2. 完成した画面だけを一瞬で画面へ表示する

という仕組みを利用します。これが**バッファリング**です。

## ダブルバッファリング
ダブルバッファリングでは2枚のバッファを使用します。
- フロントバッファ（現在表示中）
- バックバッファ（描画中）

```
Front（表示中）
Back（描画中）
```
描画が終わると
```
Front ←→ Back
```
と入れ替わります。これにより描画途中が見えなくなり、ちらつきを防ぐことができます。

## トリプルバッファリング

実際のゲームでは、**ダブルバッファリングよりトリプルバッファリングを使用することが一般的**です。トリプルバッファリングでは3枚のバッファを用います。

- Front（現在表示中）
- Back1（描画済み）
- Back2（次に描画する）

### なぜトリプルバッファリングのほうが良いのか
ダブルバッファリングでは、表示中のバッファと描画用のバッファしかないため、描画が終わっても画面表示が完了するまで次の描画を開始できない場合があります。

一方、トリプルバッファリングでは描画用バッファがもう1枚あるため、画面表示中でも次のフレームを描画できます。その結果、CPUやGPUが待機しにくくなり、フレームレートが安定してカクつきも起こりにくくなります。ゆえに、ゲームでは一般的にトリプルバッファリングが推奨されています。

## `BufferStrategy`によるトリプルバッファリング
`BufferStrategy`クラスを利用してトリプルバッファリングを行う手順は以下の通りです。これらの処理を`Gamerenderr`クラスとして作成します。

### 1. Canvasオブジェクトのバッファを作成する

```java
canvas.createBufferStrategy(3);
```
`createBufferStrategy(3)`で、3枚のバッファを持つ`BufferStrategy`を作成します。戻り値はvoidです。
```java
public void createBufferStrategy(int numBuffers)
```

### 2. 描画領域を操作するための`BufferStrategy`オブジェクトを取得

```java
BufferStrategy bs = canvas.getBufferStrategy();
```

### 3. 描画用の`Graphics`を取得する

```java
Graphics g = bs.getDrawGraphics();
```
「裏側のバッファ（バックバッファ）」に対して描画するためのペンのようなもの（Graphicsオブジェクト）を取得します。この時点での描画処理はすべてメモリ上で行われ、まだ画面には反映されません。

### 4. 前フレームを消去して描画する

```java
g.setColor(Color.BLACK);  //黒である必要はない
g.fillRect(0, 0, width, height);
```
ゲームでは毎フレーム描き直すため、前フレームを消去してから新しい画面を描画します。通常ここで背景をクリアするのはあまり推奨されるやり方ではないが、現時点ではここでクリアしておきます。なお、色は何色でもいいが、のちに別の方法で背景クリアを行うのでここでは適当に黒色にしておきます。その後、実際の描画内容を記述します。

### 5. `Graphics`を破棄する

```java
g.dispose();
```

描画が終了したら必ず`dispose()`を呼び出します。これにより使用していたグラフィックスリソースを解放できます。呼び忘れるとメモリ消費やパフォーマンス低下の原因になります。

### 6. バッファを表示する（フリッピング）
```java
bs.show();
```
描き終えたバックバッファを画面へ表示（フリッピング）します。ユーザーから見ると、一瞬で画面が切り替わるため、描画途中は見えず滑らかな画面になります。

## 描画オブジェクトの分離

ゲームでは

- プレイヤー
- 敵
- 背景
- UI

など、多くのオブジェクトを描画します。描画処理を1つのクラスへ書いてしまうと管理が難しくなるため、それぞれを独立したクラスに分離します。そこで`Renderable`インタフェースを作成します。

```java
import java.awt.Graphics;

public interface Renderable {
    void draw(Graphics g);
}
```
各ゲームオブジェクトはこのインタフェースを実装し、自分自身の描画方法を持ちます。例えば、Player、Enemy、Background、Scoreなどがそれぞれ`Renderable`を実装します。これにより`GameRenderer`は「何を描くか」を意識せず、「描画する仕組み」に専念できます。

## 描画オブジェクト
描画する内容はオブジェクトごとに`Renderable`インタフェースを実装した各オブジェクトとして分離すると、GameRenderingは描画内容にこだわらずにダブルバッファリングのロジックに集中できます。よってまず`Renderable`インタフェースを作成します。

### `Renderable`インタフェース
以下のようなdraw()メソッドを持つ`Renderable`インタフェースを作成します。
```java
import java.awt.Graphics;

public interface Renderable {
    void draw(Graphics g);
}
```
このインタフェースを実装したクラスでdraw()メソッドをオーバーライドして、描画内容を決めます。描画内容を複数クラスに分割すると管理しやすいのでインタフェースにしています。

## `GameRenderer`クラス

```java
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.List;

public class GameRenderer {

    private final Canvas canvas;
    private final BufferStrategy bs;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.canvas.createBufferStrategy(3);
        this.bs = this.canvas.getBufferStrategy();
    }

    public void render(List<Renderable> renderables) {

        Graphics g = bs.getDrawGraphics();

        try {
            // 前フレームを消去
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            // 描画
            for (Renderable r : renderables) {
                r.draw(g);
            }

        } finally {
            //リソース解放
            g.dispose();
        }
        // フリッピング
        bs.show();
    }
}
```

`try-finally`を使用している理由は、描画中に例外が発生しても`finally`が必ず実行され、`dispose()`によるリソース解放が保証されるためです。

## `Main`クラス

```java
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        GameSettings set = new GameSettings(
                800,
                600,
                "Sample Frame",
                true,
                true,
                true,
                false
        );

        SwingUtilities.invokeLater(() -> {
            //window表示
            GameWindow window = new GameWindow(set);
            window.show();
            //Renderer準備
            GameRenderer renderer = new GameRenderer(window.getCanvas());
            List<Renderable> renderables = new ArrayList<>();
            renderables.add(new Object1());
            renderables.add(new Object2());
            // レンダリング実行
            renderer.render(renderables);
        });
    }
}
```
このコードでは
```java
renderer.render(renderables);
```
を1回だけ実行しています。つまり、このプログラムでは**1フレームだけ描画して終了**します。実際のゲームでは、後で作成するゲームループの中で
```text
update()
    ↓
render()
    ↓
update()
    ↓
render()
```
を繰り返し実行することで、キャラクターの移動やアニメーションを実現します。

