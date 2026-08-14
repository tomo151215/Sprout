# Day32: SceneManagerの作成
前回、ゲーム画面を表す`Scene`クラスを作成しました。これにより、タイトル画面やプレイ画面を、次のようなクラスとして分けられるようになりました。
```text
TitleScene 
PlayScene 
ResultScene
```
しかし、Sceneはまだ自動では動きません。それを開始したり、毎フレーム更新したりする仕組みがないからです。そこで、現在のSceneを管理する`SceneManager`を作成します。`SceneManage` の役割は、次の通りです。
```text
現在のSceneを保持する
SceneにEngineを接続する
Sceneを開始する
Sceneを毎フレーム更新する
現在のSceneを終了する
```

## SceneManagerの実装
### SceneManagerはGameSystemにする
毎フレーム現在のSceneを更新する必要があります。そのため、`SceneManager`は`GameSystem`として作ります。
```java
public final class SceneManager implements GameSystem {}
```
これにより、`GameLoop`から毎フレーム`SceneManager.update()`が呼ばれるようになります。

### フィールド
`SceneManager`は、現在動いている`Scene`を1つ持ちます。さらに`Scene`の`attach()`を呼び出すために、`GameEngine`も持ちます。
```java
private Scene currentScene;
private final GameEngine engine;
```
`SceneManager`は、この`currentScene`に対して、開始・更新・終了の処理を呼び出します。`engine`はコンストラクタで受け取ります。
```java
public SceneManager(GameEngine engine){
    if(engine == null){
        throw new ILLegalArgumentException("engine must nut be null.");
    }
    this.engine = engine;
}
```
### setScene()メソッド
現在の`Scene`を設定するために、`setScene()`を作ります。役割は以下の通りです。
```text
新しいSceneがnullでないか確認する 
現在のSceneがあれば終了する 
新しいSceneをcurrentSceneにする 
新しいSceneにEngineを接続する 
新しいSceneを開始する
```
```java
public void setScene(Scene scene) {
    if (scene == null) {
        throw new IllegalArgumentException("scene must not be null.");
    }
    if (currentScene != null) {
        currentScene.end();
    }
    this.currentScene = scene;
    currentScene.attach(engine);
    currentScene.start();
}
```

### update()メソッド
`SceneManager`は`GameSystem`なので、`update()`を実装する必要があります。`update()`では現在の`Scene`を更新します。
```java
@Override
public void update() {
    if (currentScene == null) {
        return;
    }
    currentScene.update();
}
```
もし`currentScene`が`null`なら、まだ`Scene`が設定されていないということなので何もしません。`Scene`が設定されていれば、その`Scene`の`update()`を呼びます。

### endCurrentScene()メソッド
現在の`Scene`を終了したい場合に備えて、`endCurrentScene()`を作ります。
```java
public void endCurrentScene() {
    if (currentScene == null) {
        return;
    }
    currentScene.end();
    currentScene = null;
}
```
最後に`currentScene = null`とし、テストや終了処理で使えるようにします。

### getCurrentScene()メソッド
現在の`Scene`を確認できるように、`getCurrentScene()`を作ります。
```java
public Scene getCurrentScene() {
    return currentScene;
}
```

### hasScene()メソッド
現在`Scene`が存在するかどうかを確認するために、`hasScene()`も作ります。
```java
public boolean hasScene() { 
    return currentScene != null; 
}
```

## GameEngineにSceneManagerを持たせる
まずフィールドとして持たせます。
```java
private final SceneManager sceneManager;
```
コンストラクタで作成します。
```java
this.sceneManager = new SceneManager(this);
```
さらに、GameEngineのコンストラクタで`addSystem(SceneManager)`を呼びます。
```java
this.sceneManager = new SceneManager(this);
addSystem(sceneManager);
```

### getSceneManager()メソッド
ゲーム側からSceneを設定できるように、`GameEngine`にgetterを追加します。
```java
public SceneManager getSceneManager() {
    return sceneManager;
}
```
