# Day22: RendererConfigの作成
Day22では、描画設定を管理する`RendererConfig`クラスを作成します。
現在のGameRendererは背景色が直接コード内に書かれています。
```java
g.setColor(Color.WHITE);
g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
```
しかし、今後ゲームごとに
```bash
背景色を変えたい 
デバッグ描画をON/OFFしたい 
アンチエイリアスを有効にしたい 
補間描画を有効/無効にしたい
```
という場面が出てきます。そのたびにGameRendererの中身を直接修正するのはよくありません。そこで、描画に関する設定を`RendererConfig`に分離します。

## RendererConfigの作成
`RendererConfig`で管理する設定は現時点では４つです。
```text
backgroundColor : 画面クリア時に使う背景色
antiAliasing    : 線や文字をなめらかに描画するか
debugRender     : デバッグ描画を有効にするか
interpolation   : 補間描画を有効にするか
```

設定はBuilderパターンで記述します。

```java
public final class RendererConfig {
    private final Color backgroundColor;
    private final boolean antiAliasing;
    private final boolean debugRender;
    private final boolean interpolation;

    private RendererConfig(Builder builder) {
        this.backgroundColor = builder.backgroundColor;
        this.antiAliasing = builder.antiAliasing;
        this.debugRender = builder.debugRender;
        this.interpolation = builder.interpolation;
    }

    // ネストクラス
    public static final class Builder {
        private Color backgroundColor;
        private boolean antiAliasing;
        private boolean debugRender = false;
        private boolean interpolation = true;

        private Builder() {
        }

        public Builder backgroundColor(Color color) {
            if (color == null) {
                throw new IllegalArgumentException("backgroundColor must not be null.");
            }
            this.backgroundColor = color;
            return this;
        }

        public Builder isAntiAliasing(boolean antiAliasing) {
            this.antiAliasing = antiAliasing;
            return this;
        }

        public Builder isDebugRender(boolean debugRender) {
            this.debugRender = debugRender;
            return this;
        }

        public Builder isInterpolation(boolean interpolation) {
            this.interpolation = interpolation;
            return this;
        }

        public RendererConfig build() {
            return new RendererConfig(this);
        }
    }

    public static Builder builder(){
        return new Builder();
    }
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isAntiAliasing() {
        return antiAliasing;
    }

    public boolean isDebugRender() {
        return debugRender;
    }

    public boolean isInterpolation() {
        return interpolation;
    }

}
```

各フィールドにはデフォルト値を持たせるものがあります。
```java
private boolean debugRender = false;
private boolean interpolation = true;
```
通常画面にデバック画面はデフォルトで出さないようにし、補完機能はデフォルトでONにして滑らかにしておきます
。

## GameRendererの変更
RendererConfigをGameRendererへ導入します。
```java
public class GameRenderer {
    private final Canvas canvas;
    private final BufferStrategy bs;
    private final RendererConfig config;

    public GameRenderer(Canvas canvas, RendererConfig config) {
        if (canvas == null) {
            throw new IllegalArgumentException("canvas must not be null.");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        this.canvas = canvas;
        this.config = config;
        this.canvas.createBufferStrategy(3);
        this.bs = this.canvas.getBufferStrategy();
    }

    public void render(List<GameObject> objects, double alpha) {
        Graphics g = bs.getDrawGraphics();

        try {
            Graphics2D g2 = (Graphics2D) g;
            clearScreen(g2);
            applyRenderingHints(g2);
            
            double renderAlpha = config.isInterpolation() ? alpha : 1.0;

            for (GameObject o : objects) {
                o.onDraw(g2, renderAlpha);
            }
        } finally {
            g.dispose();
        }
        bs.show();
    }

    private void clearScreen(Graphics2D g) {
        g.setColor(config.getBackgroundColor());
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void applyRenderingHints(Graphics2D g) {
        Object antiAliasingValue = config.isAntiAliasing() ? RenderingHints.VALUE_ANTIALIAS_ON
                : RenderingHints.VALUE_ANTIALIAS_OFF;
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, antiAliasingValue);
    }
}
```

### コンストラクタ
コンストラクタで`RendererConfig`を渡せるようにします。
```java
private final RendererConfig config;

public GameRenderer(Canvas canvas, RendererConfig config) {
    if (canvas == null) {
        throw new IllegalArgumentException("canvas must not be null.");
    }
    if (config == null) {
        throw new IllegalArgumentException("config must not be null.");
    }
    this.canvas = canvas;
    this.config = config;
    this.canvas.createBufferStrategy(3);
    this.bs = this.canvas.getBufferStrategy();
}
```
こうすることで、`GameRenderer`は描画設定を`config`から取得できます。

### アンチエイリアスの設定
アンチエイリアスを利用すると見え方がより滑らかになります。`Graphics2D`クラスの`setRenderlingHint()`メソッドを使用します。まず、内部で`Graphics`クラスを`Graphics2D`クラスにキャストします。
```java
Graphics g = bs.getDrawGraphics();
Graphics2D g2 = (Graphics2D) g;
```
`bs.getDrawGraphics()`で返ってくるオブジェクトの実体は`Graphics2D`なのでダウンキャスト可能です。（`Graphics2D`は`Graphics`の子クラス）そして、アンチエイリアス設定を適用します。
```java
if (config.isAntiAliasing()) { 
    g2.setRenderingHint( 
        RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON 
    ); 
}
```
ここで、RenderableやGameObjectのdraw()はまだGraphicsを受け取る設計のままです。そのためここでは`o.onDraw(g2, alpha)`とし、`Graphics2D`を`Graphics`として渡します。ポリモフィズムでこの渡し方は可能です。

### clearScreenメソッドを作る
背景クリア処理を`render()`の中に直接書くのではなく、メソッドに分けます。
```java
private void clearScreen(Graphics2D g) {
    g.setColor(config.getBackgroundColor());
    g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
}
```
これにより、`render()`の流れが読みやすくなります。
```java
try {
    clearScreen(g2);
    for (GameObject o : objects) {
        o.onDraw(g2, alpha);
    }
} finally {
    g.dispose();
}
```

### applyRenderingHintsメソッドを作る
アンチエイリアスの設定も、別メソッドにします。
```java
private void applyRenderingHints(Graphics2D g) {
    Object antiAliasingValue = config.isAntiAliasing() ? RenderingHints.VALUE_ANTIALIAS_ON
                : RenderingHints.VALUE_ANTIALIAS_OFF;
    g.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, 
        antiAliasingValue
    );
}
```
`RenderingHints`クラスにおける各種ヒントの値（`VALUE_*`）は、すべて`Object`型として定義されています。

### renderDebugメソッドを作る
現時点では何もしないメソッドである。
```java
private void renderDebug(Graphics2D g) {
}
```
DebugRenderがtrueの時に実行するので、以下の様に書きます。
```java
try {
    Graphics2D g2 = (Graphics2D) g;
    clearScreen(g2);
    applyRenderingHints(g2);

    for (GameObject o : objects) {
        o.onDraw(g2, alpha);
    }

    if (config.isDebugRender()) {
        renderDebug(g2);
    }
} finally {
    g.dispose();
}
```
### renderalphaの導入
線形補完を実行するかどうかをinterpolationに基づいて決定します。もし、interpolationがtrueの場合は普通にalphaを使用して線形補完を行います。falseだった場合は、`alpha = 1.0`として、実質的に線形補完を無効化します。
```java
double renderAlpha = config.isInterpolation() ? alpha : 1.0;

for (GameObject o : objects) {
    o.onDraw(g, renderAlpha);
}
```


## GameEngineの変更
GameEngineでGameRendererを使用しているので、コンストラクタにRendererConfigを渡します。
```java
public GameEngine(GameSettings settings, int targetUps, RendererConfig rendererConfig) {
    if (settings == null) {
        throw new IllegalArgumentException("settings must not be null.");
    }
    if (rendererConfig == null) {
        throw new IllegalArgumentException("rendererConfig must not be null.");
    }
    this.setttings = settings;
    this.keyboard = new Keyboard();
    this.mouse = new Mouse();
    this.window = new GameWindow(settings, keyboard, mouse);
    this.renderer = new GameRenderer(window.getCanvas(), rendererConfig);
    this.loop = new GameLoop(targetUps, renderer, renderObjects, updateObjects, keyboard, mouse);
}
```

## GameApplicationの変更
GameEngineを変更すると、GameApplicationに影響が出るのでこれも修正します。
GameEngineのコンストラクタにRendererConfigオブジェクトを渡せるようにします。
```java
public final void run() {
    if (running) {
        return;
    }
    GameSettings settings = createSettings();
    RendererConfig config = createRendererConfig();
    this.engine = new GameEngine(settings, targetUps(), config);
    onInit();
    engine.start();
    running = true;
}
```
ここでGameRendererConfigはゲームごとに異なるので、サブクラスで実装してもらうために、抽象メソッド`createRendererConfig()`を作成します。
```java
protected abstract RendererConfig createRendererConfig();
```
