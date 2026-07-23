# Day15: GameApplication抽象クラスの作成
ゲーム制作者が継承して使うためのGameApplication抽象クラスを作成します。現在は、mainでGameEngineオブジェクトの作成や、GameObjectの作成、Gameの実行など複数の処理を行っています。GameApplicationを使って最終的にはmainは以下のように簡潔に実行することを目指します。
```java
new MyGame.run();
```
## GameApplicationとは
`GameApplication`は、ゲーム全体の起動手順を管理する抽象クラスです。従来のmainで個別の処理ごとに書く手法だと、ゲーム制作者によって手順がバラバラになる可能性があります。処理もこれから増えていくので、1つのクラスで手順を固定して、まとめるほうが効率的です。それが`GameApplication`クラスです。継承して各ゲームごとの処理を書いていくので抽象クラスにします。

### createSettings()メソッド
GameSettingsオブジェクトを生成するためのメソッドです。ゲームごとに設定は異なるので抽象メソッドにして、子クラスで内容を決めます。
```java
protected abstract GameSettings createSettings();
```
サブクラスでは以下のように実装します。
```java
@Override 
protected GameSettings createSettings() { 
    return new GameSettings( 
        800, 
        600, 
        "SampleGame", 
        true, 
        true, 
        true, 
        false 
        ); 
}
```

### targetUpsメソッド
`targetUps()`は、ゲームループの更新回数を指定するメソッドです。基本的に60UPSがよくつかわれるのでデフォルト実装しておきます。
```java
private final int DEFAULT_UPS = 60;
...
protected int targetUps() {
    return DEFAULT_UPS;
}
```
ゲームごとに変えたい場合はオーバーライドします。
```java
private final int UPS = 120;
...
@Overide
protected int targetUps() {
    return UPS;
}
```

### onInitメソッド
`onInit()`は、ゲーム開始前の初期化処理を書くためのメソッドです。初期オブジェクトの追加、入力設定、最初のシーンの作成などを行います。何も初期化するものがない場合、オーバーライドしなくて済むように、空のメソッドとして宣言します。
```java
protected void onInit() {}
```

### onShutDownメソッド
`onShutdown()`は、ゲーム終了時の片付け処理を書くためのメソッドです。
今後以下のような処理を追加する可能性があります。
- セーブデータ保存
- 設定保存
- 音声停止
- ログ出力
- アセット解放
- シーン終了処理
そのため、今のうちにメソッドだけ用意しておきます。何も処理するものがない場合、オーバーライドしなくて済むように、空のメソッドとして宣言します。
```java
protected void onShutdown() {}
```

### engineメソッド
子クラスから`GameEngine`を使うためengine()メソッドを用意します。
```java
private GameEngine engine;
...
protected final GameEngine engine() {
    if (engine == null) {
        throw new IllegalStateException("Engine is not initialized yet.");
    }
    return engine;
}
```
engineがまだ初期化されていない段階で使われたときにわかりやすいエラーが出るようにします。

### runメソッド
`run()`は、ゲームを起動するためのメソッドです。ゲームの起動順序はこのメソッドで定義します。
```java
public final void run() {
    GameSettings settings = createSettings();
    this.engine = new GameEngine(settings, targetUps());
    onInit();
    engine.start();
}
```
`final`をつける点が重要です。子クラスでオーバーライドして、起動順序をいじれないようにします。

### stopメソッド
`stop()`は、ゲームを停止するためのメソッドです。
```java
public final void stop() { 
    if (engine != null) { 
        engine.stop(); 
    } 
    onShutdown(); 
}
```
ただし、stopを複数回呼び出すとonShutdownを複数呼び出してしまうので、フラグを使ってGameApplicationを起動していたら（true）stopを呼び出せる、すでに実行が止まっていたら（false）stopお呼び出せないようにします。
```java
private boolean running;　　//falseで初期化されている
...
public final void stop() {
    if (!running) {
        return;
    }
    running = false;
    if (engine != null) {
        engine.stop();
    }
    onShutdown();
}
```
runメソッドも実行している場合は実行しないという処理を入れます。
```java
public final void run() {
    if (running) {
            eturn;
    }
    GameSettings settings = createSettings();
    this.engine = new GameEngine(settings, targetUps());
    onInit();
    engine.start();
    running = true;
}   
```

## MyGameの作成
GameApplicationを継承して個別のゲームを作成して実行してみます。
```java
public class MyGame extends GameApplication {
    private int UPS = 120;

    @Override
    protected GameSettings createSettings() {
        return new GameSettings(800, 600, "SampleGame", true, true, true, false);
    }

    @Override
    protected void onInit() {
        InputManager<Action> input = new InputManager<>(engine().getKeyboard(), Action.class);

        input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_LEFT);
        input.addMapping(Action.MOVE_UP, KeyEvent.VK_UP);
        input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_RIGHT);
        input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_DOWN);

        engine().addObject(new Block(input, 400, 200, 3));
    }

    @Override
    protected int targetUps() {
        return this.UPS;
    }

}
```
mainでは以下のように書きます。
```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        new MyGame().run();
    });
}
```