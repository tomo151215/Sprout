# Day06: Lerp実装と可変FPS化

## GameLoopの進化
現状（Day05）のGameLoopは、固定時間ステップでUpdateの間隔を固定し、Updateが実行されたタイミングでのみレンダリングを行う仕様であるため、実質的にFPSの上限がUPSに制限されています。

画面をより滑らかに描画するためには、Updateの間隔を固定したまま、レンダリングのみをハードウェアの性能に合わせて実行する「可変FPS」への対応が理想的です。ただし、Updateが固定間隔である以上、単にレンダリングの頻度を増やすだけではオブジェクトの座標が更新されず、描画は変化しません。

そこで、レンダリングのたびに次フレームへの進捗率から「予測位置」を算出し、前フレームとの間を線形補間（Lerp）して描画することで、高リフレッシュレート環境でも極めて滑らかな映像表現が可能になります。
## Lerp（線形補間）の理論
Lerp（線形補間）では、1フレームの基準時間（`nsPerUpdate`）に対する、アキュムレータの余り時間（`accumulator`）の割合を $\alpha$ （アルファ値）として定義します。描画時には、この $\alpha$ を用いて「前回の座標（$A$）」と「現在の座標（$B$）」の間を結ぶ予測位置を算出し、画面に出力します。Lerpの計算式には、以下の表現があります。

$$Lerp(A, B, \alpha) = A + (B - A)\alpha \quad (0.0 \le \alpha \le 1.0)$$

前回の座標 $A$ から、現在への移動ベクトル $(B - A)$ に対する進捗度を足し合わせる計算式です。オブジェクトが停止している（$A = B$）際に $(B - A)$ が完全に $0$ になるため、浮動小数点の丸め誤差が発生しにくく、静止時の微細なブレ（カクつき）を防ぐことができます。ゲームエンジン等で広く採用されており、今回はこちらの式を利用します。

また、数学的に等価な表現として以下のようなものもあります。

$$Lerp(A, B, \alpha) = (1 - \alpha)A + \alpha B \quad (0.0 \le \alpha \le 1.0)$$

過去の状態と現在の状態の「時間的な比重」を掛け合わせる計算式です。直感的な表現ですが、プログラム上で計算した場合、停止時（$A$ と $B$ が同じ値）であっても小数の乗算によって微小な浮動小数点誤差（例：`400.0` が `399.999...` になる現象）が生じる可能性があります。

## Lerp実装と可変FPSの実装

### 可変FPS化
可変FPSはPCの性能に応じてできるだけレンダリングするので、これまでの`update == true`の時だけ、`render()`せずに、無条件でレンダリングすればよいです。よって、`update`フラグを削除して、無条件でレンダリングします。
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

            while (accumulator >= nsPerUpdate) {
                update();
                accumulator -= nsPerUpdate;
            }
            
            render();
            sleep();
        }
    }
```
可変FPSにはなりましたが、肝心のレンダリングごとにフレームが変化していないのでalpha値を計算して`render()`に渡して、値に基づいて描画してもらえるように修正します。alpha値はwhile文の後で計算します。

### Lerp実装
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

            while (accumulator >= nsPerUpdate) {
                update();
                accumulator -= nsPerUpdate;
            }

            double alpha = accumulator / nsPerUpdate;

            render(alpha);
        }
    }

    private void render(double alpha) {
        renderer.render(renderables, alpha);
    }
```
`GameRenderer.render()`にもalphaを渡すので、Gamerendererクラスも修正します。renderメソッドの引数にalphaを追加し、Renderable.drawの引数にalphaを渡し、それに基づいて描画するようにします。
```java
public void render(List<Renderable> renderables, double alpha) {
        Graphics g = bs.getDrawGraphics();
        try {
            // 背景クリア
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            // 描画内容
            for (Renderable r : renderables) {
                r.draw(g, alpha);
            }
        } finally {
            // リソース解放
            g.dispose();
        }
        // フリッピング(画面反映)
        bs.show();
    }
```
当然Renderableインタフェースのdrawメソッドを修正します。
```java
public interface Renderable {
    void draw(Graphics g, double alpha);
}
```
実際にLerpの式を用いて描画するためのゲームオブジェクトを作ります。ここでは例として正方形のボックスが右に流れていく画面を描画していきます。
```java
public class MovingBox implements Renderable, Updatable {

    private double currentX;
    private double currentY;
    private double previousX;
    private int width;
    private int height;

    public MovingBox(double currentX, double currentY, int width, int height) {
        this.currentX = currentX;
        this.currentY = currentY;
        this.previousX = currentX;
        this.width = width;
        this.height = height;
    }

    @Override
    public void update() {
        this.previousX = currentX;
        this.currentX += 1.0;
    }

    @Override
    public void draw(Graphics g, double alpha) {
        //Lerp実装
        double lerpX = (1 - alpha) * previousX + alpha * currentX;

        g.setColor(Color.BLACK);
        g.fillRect((int)lerpX, (int)this.currentY, this.width, this.height);
    }
}
```
ここで作成したMovingBoxをmainメソッドで描画していきます。
```java
public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(set);
            window.show();
            GameRenderer renderer = new GameRenderer(window.getCanvas());
            List<Renderable> r = new ArrayList<>();
            List<Updatable> u = new ArrayList<>();
            MovingBox box = new MovingBox(0, 0, 100, 100);
            MovingBox box2 = new MovingBox(100, 200, 100, 100);
            r.add(box);
            r.add(box2);
            u.add(box);
            u.add(box2);

            GameLoop gameLoop = new GameLoop(200, renderer, r, u);

            gameLoop.start();
        });
    }
```



