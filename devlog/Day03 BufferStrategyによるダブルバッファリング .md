# Day03: BufferStrategyによるダブルバッファリング

## 概要
Swingの標準的なコンポーネント（JPanelなど）では自動でダブルバッファリングが行われる仕様でしたが、Canvasに描画する場合はダブルバッファリングが効いていないので自前で実装する必要があります。そこで、簡単に実装するためのクラスが`BufferStrategy`クラスです。このクラスを使ってダブルバッファリングを実装し、画面に図形を表示させます。

## ダブルバッファリングとは
画面を描画するとき、毎回直接目に見える部分に描画していたら、描画途中の状態が見えてしまい、画面のちらつきが発生してしまいます。そこで、裏側でいったん1フレーム全体を描画しきってから画面に転送を行うことで、描画途中の可視化を防ぎ、ちらつきを防止することができます。この仕組みを**ダブルバッファリング**といいます。この仕組みを簡単に実装するには`BufferStrategy`クラスを利用します。

## `BufferStrategy`クラスによるダブルバッファリング
`BufferStrategy`クラスを利用してダブルバッファリングを行う手順は以下である。
1. Canvasオブジェクトのバッファを用意
    ```java
    canvas.createBufferStrategy(2);  //2枚のバッファを用意する。
    ```
    このCanvas専用の描画領域が２つ（表と裏）できることを意味します。
2. 描画領域を操作するためのBufferStrategyオブジェクトを取得
    ```java
    BufferStrategy bs = canvas.getBufferStrategy();
    ```
3. 描画用のペン（Graphicsオブジェクト）を取得
    ```java
    Graphics g = bs.getDrawGraphics();
    ```
    「裏側のバッファ（バックバッファ）」に対して描画するためのペンのようなもの（Graphicsオブジェクト）を取得します。この時点での描画処理はすべてメモリ上で行われ、まだ画面には反映されません。
4. 前フレームの内容を消去して、描画をする
    ```java
    g.setColor(Color.BLACK);    //黒のペンを持つ
    g.fillRect(0, 0, set.getWidth(), set.getHeight()); //画面全体を塗りつぶす
    //ここから書きたい描画を行う
    ```
    取得した `Graphics` オブジェクトを使い、前フレームの内容を塗りつぶして消去し、裏側のバッファとして、ゲームの画面描画を実行します。実際のゲームではwhile文でパラパラ漫画の様に何度もレンダリングしてフレームを描画するので、描画のたびに前フレームの内容を消去しないといけない。
5. グラフィックスオブジェクトの破棄（リソース解放）
    ```java
    g.dispose();
    ```
    描画が完了したら、必ず `dispose()` を呼び出してシステムリソース（グラフィックスコンテキスト）を解放します。これを怠ると、メモリリークやパフォーマンス低下の原因になります。
6. バッファの切り替え（画面への反映）
    ```java
    bs.show();
    ```
    裏側で完全に描き終わったバッファと、現在画面に表示されているバッファを一瞬で入れ替えます（ページフリッピング / コピー）。これにより、プレイヤーには描きかけの行程が見えず、完成した1枚の絵が滑らかに表示されます。

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

## `GameRenderer`クラスの作成
Canvasにダブルバッファリングで描画する専用のクラスを作成します。
```java
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.List;

public class GameRenderer {
    private final Canvas canvas;
    private final BufferStrategy bs;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.canvas.createBufferStrategy(2);
        this.bs = this.canvas.getBufferStrategy();
    }

    public void render(List<Renderable> renderables) {
        Graphics g = bs.getDrawGraphics();
        try {
            // 描画内容
            for (Renderable r : renderables) {
                r.draw(g);
            }
        } finally {
            //リソース解放
            g.dispose();
        }

        // フリッピング(画面交換)
        bs.show();
    }
}
```
`try-finally`で囲んでいる理由はtryブロックで何か例外が発生したとしても、finallyで必ずdisposeしてリソース解放できるからです。

## `Main`クラスの内容
実際にcanvasの内容を表示していきます。ただし、このコードでは renderer.render(r); を1回しか呼び出していません。そのため、これはあくまで「1フレーム分の描画」です。ゲームのようにキャラクターを動かしたり、画面を継続的に更新したりするには、後でゲームループを作成し、その中で update() と render() を繰り返し実行する必要があります。
```java
import java.util.List;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(set);
            window.show();
            GameRenderer renderer = new GameRenderer(window.getCanvas());
            List<Renderable> r = new ArrayList<>();
            r.add(new Object1());
            r.add(new Object2());
            renderer.render(r);
        });
    }
}
```
`Object1`と`Object2`はRenderableを実装したクラスで、ゲーム画面に表示するオブジェクトの描画をそれぞれ担当します。