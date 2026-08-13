# Day31: Sceneクラスの作成
`Scene`クラスを作成します。現在のエンジンでは、`GameApplication`の`onInit()`などで、ゲーム開始時に必要なObjectやSystemを追加できます。しかし、ゲームには複数の画面があります。これらをすべて`onInit()`に直接書くと、コードがすぐに複雑になります。そこで、ゲーム画面を1つの単位として扱うために`Scene`クラスを作成します。

## Sceneとは
`Scene`とは、ゲーム内の「画面」や「状態」を表すクラスです。以下のようなSceneを作れるようにします。
```text
TitleScene    : タイトル画面 
PlayScene     : ゲーム本編画面 
ResultScene   : 結果画面
```
それぞれのSceneには、その画面で必要な処理を書きます。各Sceneで1つのファイルで記述します。

### Sceneのライフサイクル
Sceneには、開始・更新・終了があります。
```text
start()  : Sceneを開始する
update() : Sceneを毎フレーム更新する
end()    : Sceneを終了する
```
ただし、ゲーム制作者が直接`start()`や`end()`の中身を書き換えるのは避けたいです。Sceneの開始や終了の順番は、エンジン側が管理するべきだからです。そこで、`start()`、`update()`、`end()`は`final`にします。ゲーム制作者は、その中から呼ばれる`onStart()`、`onUpdate()`、`onEnd()`をオーバーライドします。
#### startedフラグ
Sceneには、開始済みかどうかを表す`started`フラグを持たせます。
```java
private boolean started;
```
このフラグにより、同じSceneを二重に開始することを防げます。
#### start()
`start()`は、Sceneを開始するためにエンジン側が呼ぶメソッドです。
```java
public final void start() { 
    if (started) { 
        return; 
    } 
    started = true; 
    onStart(); 
}
```
`onStart()`は、ゲーム制作者がScene開始時の処理を書くためのメソッドです。Sceneには必ず開始時の処理があるので、abstractをつけて必ずOverrideさせます。
```java
protected abstract void onStart();
```
このように分けることで、エンジン側の開始処理と、ゲーム側の初期化処理を分離できます。

#### update()
`update()`は、Sceneを毎フレーム更新するためにエンジン側が呼ぶメソッドです。
```java
public final void update() { 
    if (!started) { 
        return; 
    } 
    onStart(); 
}
```
Sceneが開始されていない場合は、何もしません。`onUpdate()`は、ゲーム制作者が毎フレームの処理を書くためのメソッドです。Sceneによっては毎フレームの特別な処理が不要な場合もあるので、空実装にします。
```java
protected void onUpdate() {};
```

#### end()
`end()`は、Sceneを終了するためにエンジン側が呼ぶメソッドです。
```java
public final void end() { 
    if (!started) { 
        return; 
    } 
    onEnd(); 
    started = false; 
}
```
`onEnd()`は、ゲーム制作者がScene終了時の処理を書くためのメソッドです。Sceneによっては毎フレームの終了処理が不要な場合もあるので空実装にします。
```java
protected void onEnd() {};
```

#### SceneからGameEngineにアクセスする
Sceneの中では、ObjectやSystemを追加したいです。そのため、Sceneは`GameEngine`を持つ必要があります。ただし、ゲーム制作者が自由に`engine`を書き換えられると危険です。そこで、`engine`は`private`にして、取得用に`engine()`メソッドを用意します。
```java
private GameEngine engine;
...
protected final GameEngine engine() {
    if (engine == null) {
        throw new IllegalStateException(
                "Scene is not attached to an engine."
        );
    }
    return engine;
}
```

#### attach()
SceneにGameEngineを接続するために、`attach()`メソッドを作ります。
```java
final void attach(GameEngine engine) { 
    if (engine == null) { 
        throw new IllegalArgumentException( "engine must not be null." ); 
    } 
    this.engine = engine; 
}
```
`attach()`は`public`にしません。パッケージプライベートにします。理由は、SceneとGameEngineの接続は、ゲーム制作者が自由に行う処理ではなく、後で作る`SceneManager`が担当する処理だからです。

#### isStarted
```java
public final boolean isStarted() { 
    return started; 
}
```


## Sceneの実装
```java
package engine.scene;

import engine.core.GameEngine;

public abstract class Scene {
    private GameEngine engine;
    private boolean started;

    public final void start() {
        if (started) {
            return;
        }
        started = true;
        onStart();
    }

    public final void update() {
        if (!started) {
            return;
        }
        onStart();
    }

    public final void end() {
        if (!started) {
            return;
        }
        onEnd();
        started = false;
    }

    public abstract void onStart();

    public void onUpdate() {
    };

    public void onEnd() {
    };

    protected final GameEngine engine() {
        if (engine == null) {
            throw new IllegalStateException(
                    "Scene is not attached to an engine.");
        }
        return engine;
    }

    final void attach(GameEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("engine must not be null.");
        }
        this.engine = engine;
    }

    public final boolean isStarted() {
        return started;
    }
}
```