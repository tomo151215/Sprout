# Day19: マウス入力
Day19ではマウス入力を追加します。マウス入力では以下のようなことを検知する必要があります。
- 現在のマウス座標
- 前フレームのマウス座標
- マウスの移動量
- 左クリックが押されているか
- 右クリックが押されているか
- 中クリックが押されているか
- クリックされた瞬間か
- 離された瞬間か
- マウスホイールの回転量

Java Swingでは、マウス入力を受け取るために次のリスナーを使います。

- `MouseListener`
- `MouseMotionListener`
- `MouseWheelListener`

Day19では、これらをまとめて扱う`Mouse`クラスを作ります。

## MouseButton enumを作成
マウスボタンはデフォルトで定義されているものは3つあり、`MouseEvent`で、ボタンは以下の定数（数値）で表されます。ゲーム用など複数のマウス用のボタンがある場合はリテラルで4、5、6、...の様に続きますがこの時点ではデフォルトのまま実装します。
```java
MouseEvent.BUTTON1 //左ボタン 1
MouseEvent.BUTTON2 //中央ボタン 2
MouseEvent.BUTTON3 //右ボタン 3
```
しかしわかりにくいのでenum型で`LEFT`、`MIDDLE`、`RIGHT`として扱い、Mouseクラス内部で変換するようにします。
```java
public enum MouseButton {
    LEFT,
    MIDDLE,
    RIGHT
}
```
## Mouseクラス
Mouseの設計方針はKeyboardと揃えます。マウスイベントが発生した瞬間に、マウス状態を記録しておき、ゲームループ更新時にマウス状態を読み込みます。
Mouseクラスでは以下の情報をフィールドとして管理します。
### マウス座標
- イベントで受け取った最新のマウス座標（`currentX`、`currentY`）
- ゲーム更新で使う現在フレームのマウス座標（`x`、`y`）
- 前フレームのマウス座標（`previousX`、`previousY`）

### ボタン
- イベントで受け取った最新のボタン状態（`currentPressed`）
- 現在フレームで使うボタン状態（`pressed`）
- 前フレームのボタン状態（`previousPressed`）

### ホイール
ホイールについては、1フレーム内で回転した量を取得できるようにします。
- イベントで加算されるホイール回転量（`currentWheelRotation`）
- 現在フレームで使うホイール回転量（`wheelRotation`）

### Mouseクラスの実装
Mouseクラスは`MouseListener`, `MouseMotionListener`, `MouseWheelListener`を実装します。
```java
public final class Mouse implements MouseListener, MouseMotionListener, MouseWheelListener {}
```
#### 排他制御用lockオブジェクト
ゲームエンジン内では、主に以下の2つのスレッドが並行して動作し、同じ変数（`currentX`, `currentY`, `currentPressed`配列など）を共有します。

- EDT (Event Dispatch Thread): マウス操作などのイベントに応じて mousePressed や mouseMoved を非同期に呼び出し、座標や入力状態を書き換える。
- ゲームループスレッド: 毎フレーム入力状態を参照（読み取り）し、ゲームロジックを更新する。

ここで、EDTが値を書き換えている最中にゲームループが読み取りを行うと、データ更新途中の不一致（不整合な座標）を読み込んでしまう問題（データ競合） が発生します。そこで、該当の処理を`synchronized(lock)`ブロックで囲み、排他制御を適用します。これにより、書き込み（または読み取り）が完了するまでもう一方のスレッドのアクセスをブロックし、スレッドセーフな状態を保つことができます。

ロックオブジェクトをあらかじめ作成しておきます。
```java
private final Object lock = new Object();
```


#### MouseButtonの状態管理
MouseButtonの押下状態管理用のboolean配列を作成します。
```java
private final int BUTTON_COUNT = MouseButton.values().length;

private final boolean[] currentPressed = new boolean[BUTTON_COUNT];
private final boolean[] pressed = new boolean[BUTTON_COUNT];
private final boolean[] previousPressed = new boolean[BUTTON_COUNT];
```
Enum型の`values()`メソッドは各定数を持つ配列を返します。その`length`でボタンの個数（`BUTTON_COUNT`）を取得します。

#### MouseButtonをインデックス変換
```java
private int index(MouseButton button) {
    if (button == null) {
        throw new IllegalArgumentException("button must not be null.");
    }
    return button.ordinal();
}
```
Enum型は定義した順番に内部でindexがついており、`0`、`1`、`2`がそれぞれ`LEFT`、`MIDDLE`、`RIGHT`に対応しています。`ordinal()`メソッドで各MouseButtonオブジェクトをindexに変換できます。

#### マウスの座標フィールド
マウスの座標をフィールドとして保持しておきます。
```java
private int currentX; 
private int currentY; 
private int x; 
private int y; 
private int previousX; 
private int previousY;
```

#### マウス座標を更新する
マウス座標は、マウスが動いたとき（マウスイベント発生時）に更新します。`MouseMotionListener`には、次の2つのメソッドがあります。
```java
mouseMoved(MouseEvent e) 
mouseDragged(MouseEvent e)
```
`mouseMoved(MouseEvent e)`はボタンを押さずにマウスが動いたときに呼ばれます。`mouseDragged(MouseEvent e)`は、ボタンを押したままマウスが動いたとき（ドラッグ時）に呼ばれます。どちらの場合もマウスの座標を更新し、共通メソッドを作っておきます。
```java
private void updateCurrentPosition(MouseEvent e) {
    synchronized (lock) {
        currentX = e.getX();
        currentY = e.getY();
    }
}
```
```java
@Override 
public void mouseMoved(MouseEvent e) { 
    updateCurrentPosition(e); 
} 

@Override 
public void mouseDragged(MouseEvent e) { 
    updateCurrentPosition(e); 
}
```
#### マウスの押下を検知する
マウスボタンが押されたときには`mousePressed`、離されたときは`MouseReleased`が呼ばれます。押された場合、`currentPressed`配列の押したボタンに対応するindex番目をtrueに、離された場合falseにします。押した、離したボタンの情報は`MouseEvent.getButton()`でint型で取得でき、それを独自に作成したMouseButtonの定数（`currentPressed`配列のindex）に変換します。そのための`toMouseButton`メソッドを作成します。
```java
private MouseButton toMouseButton(int awtButtton) {
    return switch (awtButtton) {
        case MouseEvent.BUTTON1 -> MouseButton.LEFT;
        case MouseEvent.BUTTON2 -> MouseButton.MIDDLE;
        case MouseEvent.BUTTON3 -> MouseButton.RIGHT;
        default -> null;
    };
}
```
defaultで`null`を返すのは、現時点で対応していないボタンを無視するためです。

またマウスを押下する場合にもマウスの座標更新は行います。
`mousePressed`、`MouseReleased`メソッドは以下のようになります。
```java
@Override
public void mousePressed(MouseEvent e) {
    MouseButton button = toMouseButton(e.getButton());
    if (button != null) {
        synchronized (lock) {
            currentPressed[index(button)] = true;
        }
        updateCurrentPosition(e);
    }
}

@Override
public void mouseReleased(MouseEvent e) {
    MouseButton button = toMouseButton(e.getButton());
    if (button != null) {
        synchronized (lock) {
            currentPressed[index(button)] = false;
        }
        updateCurrentPosition(e);
    }
}
```

#### ホイール入力を検知する
マウスホイールが回転したときは、`mouseWheelMoved`が呼ばれます。どちらの方向にどれだけ回転したかと、マウスの座標更新を行います。回転量をフィールドとして保持します。
```java
private int currentWheelRotation; 
private int wheelRotation;
```

`MouseEvent.getWheelRotation()`は、ホイールがどちらにどれだけ回ったかを表します。
- 正の値：下方向にスクロール 
- 負の値：上方向にスクロール

ただし、環境やマウス設定によって感覚が異なる場合があるため、ゲーム側で必要に応じて調整します。

```java
@Override
public void mouseWheelMoved(MouseWheelEvent e) {
    synchronized (lock) {
        currentWheelRotation += e.getWheelRotation();
    }
    updateCurrentPosition(e);
}
```

#### updateSnapshotを作る
Keyboard と同じように、Mouseにも`updateSnapshot()`を作ります。処理の流れは次の通りです。

1. 現在フレームの座標を前フレーム座標へ移す
2. 現在フレームのボタン状態を前フレーム状態へ移す
3. イベント側の最新座標を現在フレーム座標へコピーする
4. イベント側のボタン状態を現在フレーム状態へコピーする
5. ホイール回転量を現在フレーム用に移す
6. イベント側のホイール回転量を0に戻す

ホイール入力は「押されている状態」ではなく、「そのフレームでどれだけ回ったか」という一時的な入力です。

そのため、`updateSnapshot()`で`wheelRotation`に移したあと、`currentWheelRotation`は0に戻します。

```java
public void updateSnapshot() {
    previousX = x;
    previousY = y;

    System.arraycopy(
        pressed, 
        0, 
        previousPressed, 
        0, 
        BUTTON_COUNT
    );
    synchronized (lock) {
        x = currentX;
        y = currentY;

        System.arraycopy(
            currentPressed, 
            0, 
            pressed, 
            0, 
            BUTTON_COUNT
        );

        wheelRotation = currentWheelRotation;
        currentWheelRotation = 0;
    }
}
```

#### 座標のGetterの作成
ゲーム側から座標を取得できるようにします。
```java
public int getX() {
    return x;
}

public int getY() {
    return y;
}

public int getPreviousX() {
    return previousX;
}

public int getPreviousY() {
    return previousY;
}
```
また、移動量も取得できると便利です。マウスが前フレームからどれだけ動いたかを確認できます。
```java
public int getDeltaX() {
    return x - previousX;
}

public int getDeltaY() {
    return y - previousY;
}
```

#### ボタン状態取得メソッド
キーボードと同じ考え方で、マウスボタンにも次の3つを用意します。
```java
public boolean isPressed(MouseButton button) {
    return pressed[index(button)];
}

public boolean isJustPressed(MouseButton button) {
    int i = index(button);
    return pressed[i] && !previousPressed[i];
}

public boolean isJustReleased(MouseButton button) {
    int i = index(button);
    return !pressed[i] && previousPressed[i];
}
```

#### ホイール取得メソッド
ホイール回転量は、次のように取得できるようにします。
```java
public int getWheelRotation() {
    return wheelRotation;
}
```

#### `clear()`メソッドを作る
Keyboard と同じように、Mouseにも `clear()`を用意します。
```java
public void clear() {
    synchronized (lock) {
        Arrays.fill(currentPressed, false);
        Arrays.fill(pressed, false);
        Arrays.fill(previousPressed, false);
        currentWheelRotation = 0;
        wheelRotation = 0;
    }
}
```
フォーカスを失ったときは、マウスボタンも押されっぱなし扱いにならないようにリセットします。座標は必ずしもリセットしなくて構いません。理由は、マウス座標は「最後に分かっている位置」として残しておいても問題になりにくいからです。ただし、ボタン状態とホイール回転量はリセットします。


## GameWindowにMouseを登録する
MouseをGameWindowのcanvasに登録します。コンストラクタに`Mouse`を渡し、`addMouseListener`、`addMouseMotionListener`、`addMouseWheelListener`に登録します。
```java
public GameWindow(GameSettings set, Keyboard keyboard, Mouse mouse) {
    this.set = set;
    this.keyboard = keyboard;

    canvas.setPreferredSize(new Dimension(set.getWidth(), set.getHeight()));
    canvas.addKeyListener(keyboard);
    canvas.addMouseListener(mouse);
    canvas.addMouseMotionListener(mouse);
    canvas.addMouseWheelListener(mouse);
    ...
}
```

## GameLoopでMouseのスナップショットを更新する
MouseもKeyboardと同じように、ゲームループの更新時に updateSnapshot() を呼ぶ必要があります。Mouseフィールドを用意し、コンストラクタで初期化し、`update()`メソッドで`updateSnapshot()`を呼ぶ
```java
private final Mouse mouse;
...
public GameLoop(int targetUps, GameRenderer renderer, List<GameObject> renderObjects,List<GameObject> updateObjects, Keyboard keyboard, Mouse mouse) {
    this.targetUps = targetUps;
    this.renderer = renderer;
    this.renderObjects = renderObjects;
    this.updateObjects = updateObjects;
    this.keyboard = keyboard;
    this.mouse = mouse;
}
...
private void update() {
    keyboard.updateSnapshot();
    mouse.updateSnapshot();
    for (GameObject u : updateObjects) {
        u.onUpdate();
    }
}
```

## GameEngineにMouseを追加する
GameEngineにMouseを追加します。また、ゲーム側から使えるようにゲッターを追加します。
```java
private final Mouse mouse;
...
public GameEngine(GameSettings settings, int targetUps) {
    this.setttings = settings;
    this.keyboard = new Keyboard();
    this.mouse = new Mouse();
    this.window = new GameWindow(settings, keyboard, mouse);
    this.renderer = new GameRenderer(window.getCanvas());
    this.loop = new GameLoop(targetUps, renderer, renderObjects, updateObjects, keyboard, mouse);
}
...
public Mouse getMouse() {
    return mouse;
}
```