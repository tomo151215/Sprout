# Day20: InputContextの作成
Keyboard、Mouse、InputManager をまとめて扱うための InputContext クラスを作成します。現時点では、これらの入力関連のクラスが少し分散しています。例えばゲーム側で、キーボード、マウス、InputManagerなどを別々に取得する必要があり、システムの内部構成を知りすぎています。InputContext を作ると、入力の入口を1つにできます。

## InputContextとは何か
InputContextは、ゲーム側から入力情報へアクセスするための窓口です。以下の３つをまとめます。
```java
Keyboard 
Mouse 
InputManager<T>
```
ゲーム側では、次のように、Keyboardと Mouseを別々に意識しすぎず、InputContextから入力状態を取得できるように各フィールドの持つメソッドをラッピングします。

## InputContextクラスの実装
### フィールドとコンストラクタ
```java
public final class InputContext<T extends Enum<T>> {
    private final Keyboard keyboard;
    private final Mouse mouse;
    private final InputManager<T> inputManager;

    public InputContext(Keyboard keyboard, Mouse mouse, Class<T> actionClass) {
        if (keyboard == null) {
            throw new IllegalArgumentException("keyboard must not be null.");
        }
        if (mouse == null) {
            throw new IllegalArgumentException("mouse must not be null.");
        }
        if (actionClass == null) {
            throw new IllegalArgumentException("actionClass must not be null.");
        }

        this.keyboard = keyboard;
        this.mouse = mouse;
        this.inputManager = new InputManager<>(keyboard, actionClass);
    }
}
```
`InputContext`は３つのフィールドを持ち、コンストラクタで初期化します。InputManagerはここで作成し、ゲーム側で直接作らなくてよくなります。

### InputManagerのラッピング
InputManagerのメソッドをラップしてInputContextからも使えるようにします。メソッド名は同じにします。
```java
// InputManagerのラップ

public void addMapping(T action, int keycode) {
    this.inputManager.addMapping(action, keycode);
}

public boolean isPressed(T action) {
    return this.inputManager.isPressed(action);
}

public boolean isJustPressed(T action) {
    return this.inputManager.isJustPressed(action);
}

public boolean isJustReleased(T action) {
    return this.inputManager.isJustReleased(action);
}

public void removeMapping(T action, int keycode) {
    this.inputManager.removeMapping(action, keycode);
}

public void clearMapping(T action) {
    this.inputManager.clearAllMapping();
}

public void clearAllMapping() {
    this.inputManager.clearAllMapping();
}

public List<Integer> getMappings(T action) {
    return this.inputManager.getMappings(action);
}

public boolean hasMapping(T action, int keyCode) {
    return this.inputManager.hasMapping(action, keyCode);
}
```

### Mouseのラッピング
InputContextからMouseの機能を使えるようにします。
```java
// Mouseのラップ
public int getMouseX() {
    return this.mouse.getX();
}

public int getMouseY() {
    return this.mouse.getY();
}

public int getMousePreviousX() {
        eturn this.mouse.getPreviousX();
}

public int getMousePreviousY() {
    return this.mouse.getPreviousY();
}

public int getMouseDeltaX() {
    return this.mouse.getDeltaX();
}

public int getMouseDeltaY() {
    return this.mouse.getDeltaY();
}

public boolean isMousePressed(MouseButton button) {
    return this.mouse.isPressed(button);
}

public boolean isMouseJustPressed(MouseButton button) {
    return this.mouse.isJustPressed(button);
}

public boolean isMouseJustReleased(MouseButton button) {
    return this.mouse.isJustReleased(button);
}

public int getMouseWheelRotation() {
    return this.mouse.getWheelRotation();
}
```

### KeyboardとMouseを直接取得するgetter
InputContextは、基本的には入力を問い合わせるためのメソッドを提供します。しかし、場合によっては低レベルな Keyboard や Mouse を直接使いたいことがあります。そのため、getterも用意しておきます。
```java
// getter
public Keyboard getKeyboard() {
    return keyboard;
}

public Mouse getMouse() {
    return mouse;
}

public InputManager<T> getInputManager() {
    return inputManager;
}
```

### 入力状態をリセットする
`Keyboard.clear()`と`Mouse.clear()`をInputContextからまとめてリセットできるようなメソッドを作ります。
```java
public void clear() {
    keyboard.clear();
    mouse.clear();
}
```
これにより、シーン切り替えやポーズ解除のときに、入力状態をまとめて消せます。