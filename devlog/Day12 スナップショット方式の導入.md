# Day12: スナップショット方式の導入
現在、キーの押下状態を管理する`pressed`配列に対して、「EDT（イベントディスパッチスレッド）」と「ゲームループ」の2つのスレッドから同時にアクセスする設計になっています。EDT は OS からのキーボード入力を受け取って`pressed`の更新（書き込み）を行い、ゲームループはキャラクターの操作などのために`pressed`の読み取りを行っています。

## スレッド間の競合（データレース）による問題点
このように、複数のスレッドが同期制御なしに同じデータへアクセスし合うことを**競合**（データレース）と呼びます。ゲーム開発においてこれが起こると、主に以下のような深刻なバグを引き起こす可能性があります。

* **1フレーム内での状態の矛盾:** ゲームループの処理途中で EDT が配列を書き換えてしまうと、「フレームの計算開始時は右キーが押されていたのに、計算終了時には離されていることになっている」という矛盾が起き、キャラクターの挙動が不安定になります。
* **メモリ可視性の問題:** Java の仕様上、適切に同期処理を行わないと、キャッシュの影響により「EDT が書き換えた最新の入力状態が、ゲームループ側に即座に反映されない（見えない）」という現象が起こり得ます。

## 競合の解消：スナップショット方式
この問題を解消し、安全性を確保するための手法が**入力スナップショット**です。

EDT が随時更新している `pressed` 配列を、ゲームループに直接読ませることはしません。代わりに、毎フレームのupdateの直前に `pressed` の状態を別の配列へ丸ごとコピー（スナップショットを取得）し、そのフレームの更新処理ではコピーされた安全なデータのみを参照するようにします。これにより、「そのフレームを処理している間は、絶対に入力状態が変化しない」ことが保証され、意図しない挙動を完全に防ぐことができます。

また、previousPressed配列も同様にフレームの境界で更新することで、isJustPressed()なども効くようにしておきます。

## Keyboardクラスの改良
```java
public class Keyboard implements KeyListener {

    private static final int KEY_COUNT = 512;
    
    // EDT専用配列　[書き込み]
    private final boolean[] currentPressed = new boolean[KEY_COUNT];
    
    // ゲームループスレッド専用配列 [読み取り]
    private final boolean[] pressed = new boolean[KEY_COUNT];
    private final boolean[] previousPressed = new boolean[KEY_COUNT];

    // 排他制御のためのロック用オブジェクト
    private final Object lock = new Object();

    public void updateSnapshot() {
        // 1. 今フレームのコピーを、前フレーム用(previous)へ退避
        System.arraycopy(
                pressed, 0, 
                previousPressed, 0, 
                KEY_COUNT);

        // 2. EDTが書き込んでいる最新状態(current)を、今フレーム用(pressed)へコピー
        // ※この瞬間にEDTが割り込まないよう synchronized で鍵をかける
        synchronized (lock) {
            System.arraycopy(
                    currentPressed, 0, 
                    pressed, 0, 
                    KEY_COUNT);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (isWithinBounds(code)) {
            // 鍵をかけて最新状態を更新
            synchronized (lock) {
                currentPressed[code] = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (isWithinBounds(code)) {
            // 鍵をかけて最新状態を更新
            synchronized (lock) {
                currentPressed[code] = false;
            }
        }
    }

    ...
}
```

この `Keyboard` クラスの改良において、最も重要なポイントは「書き込み側（EDT）」と「読み取り側（GameLoop）」のアクセスを分離し、整合性を保つことです。

### 1. 3つの配列による役割分担

コード内には3つの配列が存在します。これらを明確に使い分けることで、データの不整合を防いでいます。

* **`currentPressed`（EDT専用）**
    * **役割:** キーボードイベントが発生した瞬間に書き込まれる「最新の入力状態」です。
    * **ポイント:** ここはEDTからいつでも更新されます。いわば「生の入力」が流れ込むバケツのようなものです。
* **`pressed`（GameLoop専用）**
    * **役割:** ゲームループの処理内で使われる「現在のフレームの確定した入力状態」です。
    * **ポイント:** ゲームループはこの配列だけを見に行きます。フレームの途中でEDTがキーを押しても、この配列は書き換わらないため、**1フレーム内での入力の揺らぎがゼロ**になります。
* **`previousPressed`**
    * **役割:** 1フレーム前の `pressed` を保持するための配列です。
    * **ポイント:** これがあるおかげで、「今のフレームで押されたか（`isJustPressed`）」や「離されたか（`isJustReleased`）」を正確に判定できます。

### 2. 排他制御（`synchronized`）の重要性

`synchronized (lock)` は、コードの中の「鍵をかける」処理です。EDTでアクセスして更新することになる`currentPresed`が関係する処理はすべて`synchronized (lock)`にします。

```java
synchronized (lock) {
    currentPressed[code] = true;
}
```

このブロックがあることで、**「EDTが書き込んでいる最中に、GameLoopが読み取ってしまう」というバグを完全に防いでいます。**

重要なのは、「両方のスレッドが同じ `lock` オブジェクトを通してアクセスしている」という点です。もし片方だけ `synchronized` を使い、もう片方が使わなかったら、データは保護されません。両方のスレッドがこの「ルール（鍵）」を守ることで、データの読み書きを「絶対に同時に行わせない」ように強制しています。

### 3. `updateSnapshot()` の処理フロー

このメソッドこそが、ゲームエンジンにおけるフレームの境界線を作っています。

```java
public void updateSnapshot() {
    // 1. 過去の状態を更新する
    System.arraycopy(pressed, 0, previousPressed, 0, KEY_COUNT);
    
    // 2. 最新状態をコピーする
    synchronized (lock) {
        System.arraycopy(currentPressed, 0, pressed, 0, KEY_COUNT);
    }
}

```

1. まず、現在 `pressed` にあるデータを `previousPressed` にコピーします。これで「1フレーム前のデータ」が確定します。
2. 次に、EDTが管理している `currentPressed` を `pressed` にコピーします。このコピー処理の間だけ `lock` を取得します。
    * コピー中にEDTが `keyPressed` を呼び出そうとしても、`lock` が解除されるまで待機させられます。
    * コピーが終わった瞬間に `lock` が解放され、EDTの更新処理が再開されます。



これにより、**GameLoopは、EDTの処理を一切気にせず、完全に静止した入力スナップショットを安全に受け取ることができる**のです。

## GameLoopの改良
```java
public final class GameLoop implements Runnable {
    private final int targetUps;
    private final GameRenderer renderer;
    private final List<GameObject> renderObjects;
    private final List<GameObject> updateObjects;
    private final Keyboard keyboard; // ★追加：Keyboardの参照を保持
    private Thread th;

    private volatile boolean running;

    public GameLoop(int targetUps, GameRenderer renderer, List<GameObject> renderObjects, List<GameObject> updateObjects, Keyboard keyboard) {
        this.targetUps = targetUps;
        this.renderer = renderer;
        this.renderObjects = renderObjects;
        this.updateObjects = updateObjects;
        this.keyboard = keyboard; // ★追加
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        th = new Thread(this);
        th.start();
    }

    public synchronized void stop() {
        running = false;

        if (th != null && Thread.currentThread() != th) {
            try {
                th.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        final double nsPerUpdate = 1_000_000_000.0 / targetUps;
        final int maxUpdatesPerFrame = 5;

        long lastTime = System.nanoTime();
        double accumulator = 0.0;

        while (running) {
            int updateCount = 0;
            long now = System.nanoTime();
            long elapsed = now - lastTime;
            lastTime = now;
            accumulator += elapsed;
            while (accumulator >= nsPerUpdate && updateCount < maxUpdatesPerFrame) {
                update();
                accumulator -= nsPerUpdate;
                updateCount++;
            }

            if (updateCount == maxUpdatesPerFrame) {
                accumulator = 0.0;
            }

            double alpha = accumulator / nsPerUpdate;
            render(alpha);
            sleep();
        }
    }

    private void sleep() {
        LockSupport.parkNanos(1_000_000);
    }

    private void update() {
        // ★追加：ゲームロジックの更新より先に、まず入力のスナップショットを取得する！
        keyboard.updateSnapshot();

        for (GameObject u : updateObjects) {
            u.onUpdate();
        }
    }

    private void render(double alpha) {
        renderer.render(renderObjects, alpha);
    }
}
```
GameLoopでは主に、updateのたびに最初に入力状態のスナップショットを取ってくるということをします。そのためにKeyboardオブジェクトをフィールドに保持しておきます。



