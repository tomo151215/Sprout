# 致命的なバグの修正
GPT-5.6 Solを使用した現時点でのコードレビューを行う。明確なバグを修正し、リファクタリング等は行っていない。

---
---
* 重大度：🔴 致命的
* 該当箇所：`Scene / update() / merged.txt 約1989–1994行`
* 指摘内容：`update()` 内で `onUpdate()` ではなく **`onStart()` を呼んでいます**。そのためシーン開始処理が毎ゲーム更新で実行され、本来の更新処理 `onUpdate()` は一度も実行されません。`onStart()` 内でオブジェクト登録・リソース確保などをしている場合、重複登録やリソース増加などの二次障害にもつながります。
* 修正案：

```java
public final void update() {
    if (!started) {
        return;
    }
    onUpdate();
}
```

---

---

* 重大度：🔴 致命的
* 該当箇所：`Keyboard / isJustReleased() / merged.txt 約1694–1696行`
* 指摘内容：範囲外の `keyCode` を渡した場合、`ArrayIndexOutOfBoundsException` が発生します。`isPressed()` は範囲チェックしていますが、`isJustReleased()` では `!isPressed(keyCode)` が `true` になると、その後に `previousPressed[keyCode]` を直接参照します。たとえば `-1` や `512` を渡すだけでクラッシュ可能です。さらに `InputManager.addMapping()` は任意の整数を登録できるため、通常のAPI経由でも到達できます。 
* 修正案：

```java
public boolean isJustReleased(int keyCode) {
    if (!isWithinBounds(keyCode)) {
        return false;
    }
    return !pressed[keyCode] && previousPressed[keyCode];
}
```

同様に3メソッドを同じ形に統一すると安全です。

```java
public boolean isPressed(int keyCode) {
    return isWithinBounds(keyCode) && pressed[keyCode];
}

public boolean isJustPressed(int keyCode) {
    if (!isWithinBounds(keyCode)) {
        return false;
    }
    return pressed[keyCode] && !previousPressed[keyCode];
}

public boolean isJustReleased(int keyCode) {
    if (!isWithinBounds(keyCode)) {
        return false;
    }
    return !pressed[keyCode] && previousPressed[keyCode];
}
```

---

---

* 重大度：🔴 致命的
* 該当箇所：`GameLoop / コンストラクタ・run() / merged.txt 約396–454行`
* 指摘内容：`targetUps` の妥当性確認がありません。`0` の場合は `nsPerUpdate` が正の無限大になり、`update()` が実行されなくなります。負数の場合は `nsPerUpdate` が負になり、更新条件が常に成立しやすくなって毎ループ最大5回更新する異常動作になります。 
* 修正案：

```java
public GameLoop(
        int targetUps,
        GameRenderer renderer,
        List<GameObject> renderObjects,
        List<GameObject> updateObjects,
        List<GameSystem> systems,
        Keyboard keyboard,
        Mouse mouse) {

    if (targetUps <= 0) {
        throw new IllegalArgumentException(
                "targetUps must be greater than 0.");
    }

    this.targetUps = targetUps;
    this.renderer = renderer;
    this.renderObjects = renderObjects;
    this.updateObjects = updateObjects;
    this.systems = systems;
    this.keyboard = keyboard;
    this.mouse = mouse;
}
```

---

---

* 重大度：🔴 致命的
* 該当箇所：`GameEngine / オブジェクト・システム管理`、`GameLoop / update()`
* 指摘内容：`ArrayList` をゲームループが拡張for文で走査している最中に `addObject()`、`removeObject()`、`addSystem()`、`removeSystem()` が呼ばれると、`ConcurrentModificationException` によりゲームループスレッドが終了する可能性があります。特に `GameSystem.update()` の中から `engine.addSystem()` などを呼ぶと、`systems` 自身を走査中なので直接この問題に到達できます。  
* 修正案：今回は安全性を優先し、既存構造をほぼ変更しないなら `CopyOnWriteArrayList` が単純です。

```java
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

private final List<GameObject> renderObjects =
        new CopyOnWriteArrayList<>();

private final List<GameObject> updateObjects =
        new CopyOnWriteArrayList<>();

private final List<GameSystem> systems =
        new CopyOnWriteArrayList<>();
```

なお、現在のgetterは内部リストそのものを返しており、`null` の追加や重複追加によって `addObject()` のチェックを完全に迂回できます。

こちらも最低限、読み取り専用にします。

```java
public List<GameObject> getRenderObjects() {
    return List.copyOf(renderObjects);
}

public List<GameObject> getUpdateObjects() {
    return List.copyOf(updateObjects);
}
```

---

---

* 重大度：🔴 致命的
* 該当箇所：`GameLoop / start(), stop(), run() / merged.txt 約407–456行`
* 指摘内容：ゲームループ内から `stop()` を呼んだ場合、自己 `join()` は適切に避けられていますが、古いゲームループスレッドが完全終了する前に別スレッドから `start()` される可能性があります。その場合 `running` が再び `true` となり、**古いスレッドと新しいスレッドの2本が同時にゲームループを実行する危険**があります。また `update()` や `render()` が例外終了した場合、現在は `running` が `true` のまま残るため、その後 `start()` しても再起動できません。 
* 修正案：

```java
public synchronized void start() {
    if (running || (th != null && th.isAlive())) {
        return;
    }

    running = true;
    th = new Thread(this);
    th.start();
}

@Override
public void run() {
    try {
        final double nsPerUpdate =
                1_000_000_000.0 / targetUps;
        final int maxUpdatesPerFrame = 5;

        long lastTime = System.nanoTime();
        double accumulator = 0.0;

        while (running) {
            int updateCount = 0;

            long now = System.nanoTime();
            long elapsed = now - lastTime;
            lastTime = now;
            accumulator += elapsed;

            while (running
                    && accumulator >= nsPerUpdate
                    && updateCount < maxUpdatesPerFrame) {

                update();
                accumulator -= nsPerUpdate;
                updateCount++;
            }

            if (!running) {
                break;
            }

            if (updateCount == maxUpdatesPerFrame
                    && accumulator >= nsPerUpdate) {
                accumulator %= nsPerUpdate;
            }

            double alpha = accumulator / nsPerUpdate;
            render(alpha);
            sleep();
        }
    } finally {
        running = false;
    }
}
```

## `finally` にすることで例外自体は揉み消さず、その例外は通常どおり上位へ伝わる一方、GameLoopの状態だけは必ず正しく戻せます。

---

* 重大度：🟡 警告
* 該当箇所：`GameLoop / run() / merged.txt 約448–450行`
* 指摘内容：更新回数がちょうど `maxUpdatesPerFrame` に達しただけで `accumulator = 0.0` としています。たとえば5.5更新分の時間が蓄積していた場合、5回更新後に残った本来有効な0.5更新分まで捨てられます。結果として補間値も0に戻り、時間の欠落が発生します。
* 修正案：上限到達後にも「まだ1更新以上の遅れが残っている場合」だけ古い更新分を捨てます。

```java
if (updateCount == maxUpdatesPerFrame
        && accumulator >= nsPerUpdate) {
    accumulator %= nsPerUpdate;
}
```

## これなら0～1更新分未満の端数は補間用に保持されます。

---

* 重大度：🟡 警告
* 該当箇所：`Scene / start(), end()`、`SceneManager / setScene(), endCurrentScene()`
* 指摘内容：ライフサイクルメソッドで例外が発生すると状態が不整合になります。`Scene.start()` は `started = true` にした後で `onStart()` を呼ぶため、`onStart()` が失敗しても「開始済み」のままです。逆に `end()` は `onEnd()` が例外を投げると `started = false` まで到達しません。また新しいシーンの `start()` が失敗しても `SceneManager.currentScene` にはその失敗したシーンが残ります。 
* 修正案：

```java
public final void start() {
    if (started) {
        return;
    }

    started = true;

    try {
        onStart();
    } catch (RuntimeException | Error e) {
        started = false;
        throw e;
    }
}

public final void end() {
    if (!started) {
        return;
    }

    try {
        onEnd();
    } finally {
        started = false;
    }
}
```

`SceneManager` 側も失敗時に不正な参照を残さないようにします。

```java
public void setScene(Scene scene) {
    if (scene == null) {
        throw new IllegalArgumentException(
                "scene must not be null.");
    }

    if (currentScene != null) {
        try {
            currentScene.end();
        } finally {
            currentScene = null;
        }
    }

    currentScene = scene;

    try {
        currentScene.attach(engine);
        currentScene.start();
    } catch (RuntimeException | Error e) {
        currentScene = null;
        throw e;
    }
}

public void endCurrentScene() {
    if (currentScene == null) {
        return;
    }

    try {
        currentScene.end();
    } finally {
        currentScene = null;
    }
}
```

---

---

* 重大度：🟡 警告
* 該当箇所：`GameSettings.Builder / build() / merged.txt 約504–557行`
* 指摘内容：`size()` と `title()` の各メソッドでは値を検証していますが、**それらを一度も呼ばずに `build()` することができます**。その場合 `width = 0`、`height = 0`、`title = null` の `GameSettings` が生成され、後段のウィンドウ初期化まで不正値が流れます。 
* 修正案：

```java
public GameSettings build() {
    if (width <= 0 || height <= 0) {
        throw new IllegalStateException(
                "size must be configured before build().");
    }

    if (title == null || title.isBlank()) {
        throw new IllegalStateException(
                "title must be configured before build().");
    }

    return new GameSettings(this);
}
```

## これにより不正な設定で深い位置まで進んでから失敗するのを防げます。

---

* 重大度：🟡 警告
* 該当箇所：`Sprite / getHeight() / merged.txt 約98–100行`
* 指摘内容：`getHeight()` が `image.getHeight()` ではなく **`image.getWidth()` を返しています**。正方形以外の画像では高さが必ず誤り、描画位置・領域計算・当たり判定など、幅と高さを利用する処理が誤動作します。
* 修正案：

```java
public int getHeight() {
    return image.getHeight();
}
```

---

---

* 重大度：🟡 警告
* 該当箇所：`GameWindow / focusLost(), windowLostFocus() / merged.txt 約2146–2173行`
* 指摘内容：ウィンドウやCanvasがフォーカスを失ったとき `Keyboard.clear()` のみ実行しています。マウスボタンを押した状態でフォーカスを失うと `mouseReleased` が届かない場合があり、`Mouse.currentPressed` が `true` のまま残り、「マウスボタンが永久に押されている」状態になる可能性があります。 
* 修正案：

```java
private final Keyboard keyboard;
private final Mouse mouse;

public GameWindow(
        GameSettings set,
        Keyboard keyboard,
        Mouse mouse) {

    this.set = set;
    this.keyboard = keyboard;
    this.mouse = mouse;

    // ...
}
```

フォーカス喪失時には両方をクリアします。

```java
canvas.addFocusListener(new FocusAdapter() {
    @Override
    public void focusLost(FocusEvent e) {
        GameWindow.this.keyboard.clear();
        GameWindow.this.mouse.clear();
    }
});

frame.addWindowFocusListener(new WindowAdapter() {
    @Override
    public void windowLostFocus(WindowEvent e) {
        GameWindow.this.keyboard.clear();
        GameWindow.this.mouse.clear();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        requestCanvasFocus();
    }
});
```

---

---

* 重大度：🟡 警告
* 該当箇所：`Keyboard / updateSnapshot()`、`Mouse / updateSnapshot()`
* 指摘内容：スナップショット更新の一部が `synchronized (lock)` の外側にあります。一方 `clear()` は別スレッドであるSwing/AWTイベントスレッドから呼ばれる可能性があります。そのため `pressed → previousPressed` のコピー中に `clear()` が割り込み、1回分の入力状態が混在する可能性があります。Keyboardではコピーがロック外、Mouseでも座標更新・`previousPressed` コピーがロック外です。  
* 修正案：

```java
// Keyboard
public void updateSnapshot() {
    synchronized (lock) {
        System.arraycopy(
                pressed,
                0,
                previousPressed,
                0,
                KEY_COUNT);

        System.arraycopy(
                currentPressed,
                0,
                pressed,
                0,
                KEY_COUNT);
    }
}
```

```java
// Mouse
public void updateSnapshot() {
    synchronized (lock) {
        previousX = x;
        previousY = y;

        System.arraycopy(
                pressed,
                0,
                previousPressed,
                0,
                BUTTON_COUNT);

        x = currentX;
        y = currentY;

        System.arraycopy(
                currentPressed,
                0,
                pressed,
                0,
                BUTTON_COUNT);

        wheelRotation = currentWheelRotation;
        currentWheelRotation = 0;
    }
}
```

フォーカス喪失時に `Mouse.clear()` も呼ぶ修正を入れるなら、ボタン状態の読み取りも同じロックで保護するのが安全です。

```java
public boolean isPressed(MouseButton button) {
    int i = index(button);
    synchronized (lock) {
        return pressed[i];
    }
}

public boolean isJustPressed(MouseButton button) {
    int i = index(button);
    synchronized (lock) {
        return pressed[i] && !previousPressed[i];
    }
}

public boolean isJustReleased(MouseButton button) {
    int i = index(button);
    synchronized (lock) {
        return !pressed[i] && previousPressed[i];
    }
}
```

---

---

* 重大度：🟡 警告
* 該当箇所：`AudioManager / loadClip(), playSe() / merged.txt 約126–148行`
* 指摘内容：`Clip` の取得後に `clip.open(stream)` が失敗した場合、作成済みの `Clip` を明示的に閉じないまま例外を投げています。また `playSe()` でもロード成功後に再生開始処理が例外になった場合、STOPイベントに到達しないため `clip.close()` が実行されない可能性があります。音声ラインは有限のネイティブリソースなので、失敗経路でも確実に解放する必要があります。
* 修正案：

```java
private Clip loadClip(String path) {
    if (path == null || path.isBlank()) {
        throw new IllegalArgumentException(
                "path must not be null or blank.");
    }

    Clip clip = null;

    try (AudioInputStream stream =
            AudioSystem.getAudioInputStream(new File(path))) {

        clip = AudioSystem.getClip();
        clip.open(stream);
        return clip;

    } catch (UnsupportedAudioFileException
            | IOException
            | LineUnavailableException e) {

        if (clip != null) {
            try {
                clip.close();
            } catch (RuntimeException closeError) {
                e.addSuppressed(closeError);
            }
        }

        throw new IllegalArgumentException(
                "Failed to load audio: " + path, e);
    }
}
```

`playSe()` も開始失敗時に閉じます。

```java
public void playSe(String path) {
    Clip clip = loadClip(path);

    try {
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                clip.close();
            }
        });

        clip.setFramePosition(0);
        clip.start();

    } catch (RuntimeException e) {
        clip.close();
        throw e;
    }
}
```

---

---

* 重大度：🟡 警告
* 該当箇所：`GameEngine / stop()`、`GameWindow / クラス全体`
* 指摘内容：`GameEngine.stop()` はゲームループと音声を停止しますが、作成した `JFrame` を `dispose()` していません。したがって `GameApplication.stop()` を正常に呼んでもウィンドウとそのネイティブリソースが残り得ます。現在の `GameWindow` にはウィンドウを破棄するメソッド自体がありません。 
* 修正案：

```java
// GameWindow
public void close() {
    frame.dispose();
}
```

```java
// GameEngine
public void stop() {
    loop.stop();

    try {
        audioManager.close();
    } finally {
        window.close();
    }
}
```

この修正を行う場合、ゲームループ自身から `stop()` が呼ばれた後に破棄済みCanvasへ描画しないよう、先ほどの `GameLoop.run()` の

```java
while (running && accumulator >= nsPerUpdate
        && updateCount < maxUpdatesPerFrame) {
    // ...
}

if (!running) {
    break;
}
```

## も併せて入れる必要があります。

### 優先して直すべき箇所

今回の範囲で特に先に修正すべきなのは、**`Scene.update()`、`Keyboard.isJustReleased()`、`targetUps` 検証、ゲームループ中の `ArrayList` 変更、GameLoopのスレッド終了状態管理**です。これらは単なるコード品質ではなく、現状のまま実際に誤動作・ゲームループ停止・例外終了へ直結します。

なお、抽出した27個のJavaソースについては **Java 21 の `javac` でコンパイル確認を行い、コンパイルエラーは検出されませんでした**。今回の問題は主として実行時・状態管理上のものです。
