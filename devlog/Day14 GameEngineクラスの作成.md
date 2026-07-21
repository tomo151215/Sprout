# Day15：Engineクラスを作成する
ゲームエンジン全体をまとめる `GameEngine` クラスを作成します。これまでの構成では、`Main` クラス側で以下のような部品を直接作成していたはずです。

* `GameSettings`
* `Keyboard`
* `GameWindow`
* `GameRenderer`
* `GameLoop`
* `GameObject` のリスト

このままでも動作はしますが、ゲームが大きくなると `Main` がどんどん複雑になります。そこで、これらをまとめて管理する `GameEngine` クラスを作成し、`Main` はできるだけシンプルにします。つまり、`Main` は「設定を作る」「オブジェクトを追加する」「起動する」だけにします。

## なぜEngineクラスが必要なのか
現在のエンジンには、すでに重要な部品があります。

* ウィンドウを作る `GameWindow`
* 描画を行う `GameRenderer`
* ゲームループを回す `GameLoop`
* キーボード入力を管理する `Keyboard`
* ゲーム内オブジェクトを表す `GameObject`

しかし、これらをすべて `Main` で直接組み立てると、次のような問題が起こります。

### 問題1：Mainの責務が大きくなる

`Main` は本来、アプリケーションの入口です。

しかし、そこに

* ウィンドウ生成
* Renderer生成
* GameLoop生成
* Keyboard生成
* オブジェクトリスト管理
* 起動処理

をすべて書くと、`Main` がエンジン内部の構造を知りすぎてしまいます。これは、設計上あまりよくありません。

### 問題2：部品同士の接続が毎回必要になる

たとえば、現在の構成では `GameRenderer` を作るために `Canvas` が必要です。その `Canvas` は `GameWindow` から取得します。さらに、`GameLoop` を作るには、`renderer`、オブジェクトリスト、`keyboard` が必要です。このような接続処理は、ゲーム本体のロジックではなく、エンジンの初期化処理です。そのため、`Main` ではなく `Engine` に隠すべきです。

### 問題3：将来の拡張が難しくなる
今後、以下のような機能を追加していきます。

* マウス入力
* シーン管理
* アセット管理
* オーディオ管理
* デバッグ表示
* 物理エンジン
* UI管理

これらを毎回 `Main` に追加していくと、`Main` が巨大になります。そこで、ゲームエンジン全体の司令塔として `GameEngine` クラスを作ります。

## GameEngineクラスの責務

`GameEngine` クラスの役割は、ゲームエンジンの主要部品をまとめて管理することです。現時点では、次の責務を持たせます。

* `Keyboard` を作成する
* `GameWindow` を作成する
* `GameRenderer` を作成する
* `GameLoop` を作成する
* 更新対象オブジェクトを管理する
* 描画対象オブジェクトを管理する
* ゲームを開始する
* ゲームを停止する
* 必要な部品を外部から取得できるようにする


## GameEngineクラス実装
GameEngineクラスを実装します。

```java
public class GameEngine {
    private final GameSettings setttings;
    private final Keyboard keyboard;
    private final GameWindow window;
    private final GameRenderer renderer;
    private final GameLoop loop;
    private final List<GameObject> renderObjects = new ArrayList<>();
    private final List<GameObject> updateObjects = new ArrayList<>();

    public GameEngine(GameSettings settings, int targetUps) {
        this.setttings = settings;
        this.keyboard = new Keyboard();
        this.window = new GameWindow(settings, keyboard);
        this.renderer = new GameRenderer(window.getCanvas());
        this.loop = new GameLoop(targetUps, renderer, renderObjects, updateObjects, keyboard);
    }

    public void start() {
        window.show();
        loop.start();
    }

    public void stop() {
        loop.stop();
    }

    public void addObject(GameObject object) {
        updateObjects.add(object);
        renderObjects.add(object);
    }

    public void removeObject(GameObject object) {
        updateObjects.remove(object);
        renderObjects.remove(object);
    }

    public GameSettings getSetttings() {
        return setttings;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public GameWindow getWindow() {
        return window;
    }

    public GameRenderer getRenderer() {
        return renderer;
    }

    public GameLoop getLoop() {
        return loop;
    }

    public List<GameObject> getRenderObjects() {
        return renderObjects;
    }

    public List<GameObject> getUpdateObjects() {
        return updateObjects;
    }

    
}

```

### startメソッド

`Engine.start()` では、ウィンドウを表示してからゲームループを開始します。

```java
public void start() {
    window.show();
    loop.start();
}
```

順番は重要です。先にウィンドウを表示しておくことで、`Canvas` が画面上に表示され、フォーカスも取得しやすくなります。

### stopメソッド

ゲームを止めるために `stop()` も用意します。

```java
public void stop() {
    loop.stop();
}
```
現時点では、`GameLoop` の停止だけで十分です。将来的には、ここに以下の処理も追加されます。

* 音声の停止
* アセットの解放
* セーブ処理
* シーン終了処理
* ログ出力


### addObjectメソッド

ゲーム内オブジェクトを追加するためのメソッドを作ります。現在の `GameObject` は `Renderable` と `Updatable` を両方実装しているため、更新リストと描画リストの両方に追加します。ただし、将来的には

* 更新だけするオブジェクト
* 描画だけするオブジェクト
* UI用オブジェクト
* 非表示オブジェクト

などが出てきます。そのため、現時点では単純に両方へ追加し、後に設計を改善します。

### removeObjectメソッド

追加だけでなく、削除用メソッドも用意しておきます。ただし、これはまだ簡易版です。ゲームループでリストを走査している最中に削除すると、`ConcurrentModificationException` が起こる可能性があります。そのため、本格的な削除処理のちに改善します。

### getterを用意する
ゲーム側から一部の部品を取得できるようにします。
```java
    public GameSettings getSetttings() {
        return setttings;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public GameWindow getWindow() {
        return window;
    }

    public GameRenderer getRenderer() {
        return renderer;
    }

    public GameLoop getLoop() {
        return loop;
    }

    public List<GameObject> getRenderObjects() {
        return renderObjects;
    }

    public List<GameObject> getUpdateObjects() {
        return updateObjects;
    }
```

## Mainクラスの変更

これまで `Main` に書いていたエンジン初期化処理を、`Engine` に任せます。

```java
public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            GameEngine engine = new GameEngine(set, 120);
            InputManager<Action> input = new InputManager<>(engine.getKeyboard(), Action.class);

            input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_LEFT);
            input.addMapping(Action.MOVE_UP, KeyEvent.VK_UP);
            input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_RIGHT);
            input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_DOWN);

            engine.addObject(new Block(input, 100, 200, 2));
            engine.start();
        });
    }
```
これにより、`Main` はかなり読みやすくなります。なお、 `GameEngine` に `InputManager` まで持たせる必要はありません。理由は、`InputManager<Action>` の `Action` はゲームごとに異なるからです。たとえば、あるゲームでは次のような `Action` を使うかもしれません。
```java
public enum Action {
    LEFT,
    RIGHT,
    JUMP,
    ATTACK
}
```
別のゲームでは、次のようになるかもしれません。
```java
public enum Action {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    SHOT,
    BOMB
}
```
つまり、`InputManager<T>` はエンジン本体よりも、ゲーム側で作る方が自然です。



