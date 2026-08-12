# Day30: GameSystemの作成
現在のエンジンでは、主に`GameObject`を使ってゲーム内のものを扱っています。これまでは、基本的に「更新される」かつ「描画される」ものとして扱ってきました。しかし、ゲームエンジンには、画面には直接描画されないけれど、毎フレーム更新したい処理もあります。たとえば、次のようなものです。
```text
CameraController 
SceneManager
AudioManager
CollisionSystem
```
描画されない更新処理を表すために`GameSystem`を作成します。

## GameSystemの作成
`GameSystem`インターフェースであり、更新できるものとして扱うために、`Updatable`を継承し、`update()`メソッドを持っています。各更新処理は`GameSystem`を実装することで実現します。
```java
public interface GameSystem extends Updatable {}
```
GameSystemを作らなくても例えば、`CameraController implements Updatable`としても、毎フレーム更新できます。しかし、それだと意味が少し曖昧になります。`Updatable`は、単に「更新できる」という能力を表しています。一方、`GameSystem`は、「エンジンの仕組みとして更新されるもの」を表します。


## CameraControllerをGameSystemにする
`CameraController`は、毎フレーム更新される必要があります。`CameraController`に`GameSystem`を実装させます。
```java
public final class CameraController implements GameSystem{
    @Override
    public void update() {
        if (target == null) {
            return;
        }
        if (deadZone != null) {
            updateDeadZone();
        } else {
            updateFollow();
        }
        applyBounds();
    }
}
```
もともと`update()`としていたところを`@Override`にして実装します。

## GameLoopにsystemsを追加する
`GameSystem`を作っただけでは、自動更新されません。ゲームループの中で、`GameSystem`のリストを更新する必要があります。そこで、`GameLoop`に`systems`を追加します。
```java
private final List<GameSystem> systems;
```
コンストラクタでも受け取れるようにします。
```java
public GameLoop(int targetUps, GameRenderer renderer, List<GameObject> renderObjects,List<GameObject> updateObjects, List<GameSystem> systems, Keyboard keyboard, Mouse mouse) {
    this.targetUps = targetUps;
    this.renderer = renderer;
    this.renderObjects = renderObjects;
    this.updateObjects = updateObjects;
    this.systems = systems;
    this.keyboard = keyboard;
    this.mouse = mouse;
}
```

### GameLoopのupdateを修正する
現在の`GameLoop`では、Objectを更新しています。ここに、`GameSystem`の更新を追加します。
```java
private void update() {
    keyboard.updateSnapshot();
    mouse.updateSnapshot();
    for (GameObject u : updateObjects) {
        u.onUpdate();
    }
    for (GameSystem system : systems) {
        system.update();
    }
}
```
ここで、必ずオブジェクトを更新したのちにシステムを更新する必要があります。なぜなら、オブジェクトの移動更新を見てカメラを動かしたりするので、必ず`GameSystem`の更新は後に行います。もし、オブジェクト更新の前にシステムを更新すると、カメラは1フレーム前のプレイヤー位置を見てしまう可能性があります。これでも大きな問題にならない場合はありますが、カメラ制御としては、Object更新後にSystem更新する方が自然です。

## GameEngineにsystemsを持たせる
`GameEngine`側に`GameSystem`のリストを持たせます。
```java
private final List<GameSystem> systems = new ArrayList<>();
```
コンストラクタの改変します。
```java
```

### addSystems()とremoveSystems()
外部から`GameSystem`を追加、削除できるように、`addSystem()`を作ります。
```java
public void addSystem(GameSystem system) {
    if (system == null) {
        throw new IllegalArgumentException("system must not be null.");
    }
    if (systems.contains(system)) { //同じGameSystemを重複登録することを防ぐ
        return;
    }
    systems.add(system);
}

public void removeSystem(GameSystem system) {
    if (system == null) {
        return;
    }
    systems.remove(system);
}
```