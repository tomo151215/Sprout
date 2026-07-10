# renderとupdateの分離とLerp実装
Day04の段階で基本的なGameLoopの型は出来上がっていますが、高性能なPCではより滑らかに描画されるようにrenderとupdateを分離して、update間隔は一定で、より高性能なPCほど滑らかに描画されるように改変します。

## なぜ分離が必要なのか？
現在のゲームループではた、ゲームのロジックが更新され（`updated == true`）ときだけ描画を行っています。これでは、どれだけグラフィックボードやモニターが高性能であっても、最大で targetUps（例：60回/秒）のペースでしか画面が書き換わりません。理想的なゲームループは、「ゲームの進行スピード（UPS）は完全に固定しつつ、画面の描き換え（FPS）はPCの限界（またはモニターの同期速度）まで解放する」という状態です。これを実現するために、update を実行したかどうかに関わらず、毎フレーム render を呼び出す構造へと変更します。

## Lerp実装によるマイクロスタッターの除去
updateは制限されているので、それに合わせてrenderを無制限にする場合、同じ内容のフレームを複数回描画する場合が出てきます。この時、画面は一瞬のカクつきが起こったように見えます。これを**マイクロスタッター**といいます。これを防ぐには、「描画補間（Lerp: 線形補間）」を行う必要があります。LerpはnsPerupdateに対するaccumulatorの割合を$\alpha$とし、以下の数式に従ってオブジェクトの座標を計算して、それを予測値として描画します。$A$は前回の座標、$B$は現在の座標を表しています。
$$
Lerp(A, B, \alpha) = A + (B-A)\alpha　(0.0 \leqq \alpha \leqq 1.0)
$$
もしくは
$$
Lerp(A, B, \alpha) = (1-\alpha)A + \alpha B　(0.0 \leqq \alpha \leqq 1.0)
$$

ここからは、実際にゲームループを分離し、Lerpを組み込むための実装ステップを解説します。

### `GameLoop.java` の改変

まずは、毎フレーム `render` を呼び出すように変更し、アキュムレータの余りから `alpha`（0.0以上 1.0未満の割合）を算出してレンダラーに渡すようにします。また、極端な処理落ち時のフリーズ（Spiral of Death）を回避する安全装置は残しておきます。

```java
@Override
public void run() {
    final double nsPerUpdate = 1_000_000_000.0 / targetUps;
    long lastTime = System.nanoTime();
    double accumulator = 0.0;

    while (running) {
        long now = System.nanoTime();
        long elapsed = now - lastTime;
        lastTime = now;
        accumulator += elapsed;
        
        int maxUpdateCount = 5;
        int updateCount = 0;

        while (accumulator >= nsPerUpdate && updateCount < maxUpdateCount) {
            update();
            accumulator -= nsPerUpdate;
            updateCount++;
        }

        if (updateCount == maxUpdateCount) {
            accumulator = 0.0;
        }

        // alphaを計算
        double alpha = accumulator / nsPerUpdate;
        render(alpha);

        // 待っているプロセスがいるならCPU解放、いなければそのまま続行
        Thread.yield();
    }
}

private void render(double alpha) {
    renderer.render(renderables, alpha);
}
```

### `Renderable` と `GameRenderer` の改変
次に、ゲームループから渡された `alpha` を受け取り、描画対象の各オブジェクトへとバケツリレーのように渡していきます。

```java
// Renderable.java
public interface Renderable {
    void draw(Graphics g, double alpha);
}
```

```java
// GameRenderer.java の renderメソッドを修正
public void render(List<Renderable> renderables, double alpha) {
    Graphics g = bs.getDrawGraphics();
    try {
        //背景クリア
        g.setColor(Color.BLACK); // 背景クリア
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        //描画
        for (Renderable r : renderables) {
            r.draw(g, alpha); // 各オブジェクトにalphaを伝播
        }
    } finally {
        g.dispose();
    }

    bs.show();
    Toolkit.getDefaultToolkit().sync(); // OSの描画キューと同期させてカクつき防止
}
```

### `GameObject` クラスの導入（Lerpの自動化）

これまでは`Renderable`,`Updatable`を実装して動かすオブジェクトを定義していましたが、Lerpを実装するにあたって、上述の数式による処理を各オブジェクトの `draw` メソッド内で書かないといけないので非常に面倒です。そこで、抽象クラス`gameObject`を挟んで、動かすオブジェクトに自動でLerp機能を含有させることができます。

```java
import java.awt.Graphics;

public abstract class GameObject implements Updatable, Renderable {
    // 座標の管理（外部から勝手に書き換えられないよう private にする）
    private double currentX, currentY;
    private double prevX, prevY;

    // 子クラスが実装する純粋なゲームロジック
    protected abstract void onUpdate();
    protected abstract void onDraw(Graphics g);

    @Override
    public final void update() {
        // ① まず、今の位置を「過去の位置」として保存する
        prevX = currentX;
        prevY = currentY;

        // ② 子クラス独自のロジック（移動など）を実行させる
        onUpdate();
    }

    @Override
    public final void draw(Graphics g, double alpha) {
        //親クラスが自動でLerp（線形補間）座標を計算する
        double drawX = prevX + (currentX - prevX) * alpha;
        double drawY = prevY + (currentY - prevY) * alpha;

        //キャンバスの原点(0, 0)を、今計算したLerp座標にワープさせる（超重要！）
        g.translate((int) drawX, (int) drawY);

        try {
            //子クラスの描画処理を呼び出す
            onDraw(g);
        } finally {
            //他のオブジェクトに影響が出ないよう、原点を元の位置に戻す
            g.translate(-(int) drawX, -(int) drawY);
        }
    }

    // --- 子クラスが座標を操作するための便利なメソッド ---
    public void move(double dx, double dy) {
        this.currentX += dx;
        this.currentY += dy;
    }

    public void setPosition(double x, double y) {
        this.currentX = x;
        this.currentY = y;
        this.prevX = x;
        this.prevY = y; // 生まれた瞬間にLerpが暴れないよう同期する
    }
}
```

### 4. 子クラス（`Player` など）の実装

`GameObject` を継承することで、子クラスは過去の座標や `alpha`、Lerpの数式を一切気にする必要がなくなります。純粋な移動ロジックと、`(0, 0)` を基準とした描画処理のみを記述できます。

```java
import java.awt.Color;
import java.awt.Graphics;

public class Player extends GameObject {

    public Player(double startX, double startY) {
        setPosition(startX, startY);
    }

    @Override
    protected void onUpdate() {
        // 移動ロジックのみに集中できる
        move(2.0, 0.0);
    }

    @Override
    protected void onDraw(Graphics g) {
        // 親クラスで原点がLerp座標に移動しているため、(0, 0)に描画するだけでよい
        g.setColor(Color.RED);
        g.fillRect(0, 0, 32, 32); 
    }
}

```

## まとめ

この設計により、ゲームの論理的な進行速度（UPS）は完全に固定しつつ、描画はモニターの限界まで滑らかに行う（FPSの解放）ことが可能になります。さらに、抽象クラスを用いたカプセル化によって、新しいキャラクターやオブジェクトを追加する際の実装コストを大幅に削減しつつ、バグの入り込みにくい強固なアーキテクチャを実現できます。




