# isJustPressed/isJustReleasedの作成
ゲームでは、押していることを検知するだけでは実現できない操作がたくさんあります。例えば、ジャンプ、攻撃、決定などがあります。isPressed/isReleasedはゲームループのアップデートごとに何度もpressed配列を確認して、押しているか否かを複数回検知してしまうが、キーを押した瞬間に1回だけ実行したいという場合のためにisJustPressed/isJustReleasedの作成を行います。

押した瞬間、離した瞬間を検知するにはpressed配列の要素の変化を見ればいいです。なので直前の状態を記録する`previousPressed`配列を作ります。

## `previousPressed`配列を作る
Keyboardクラスに以下のフィールドを作成します。
```java
private final boolean[] previousPressed = new boolean[KEY_COUNT];
```
ここで毎フレームの最後に、  pressed配列の内容をpreviousPressed配列にコピーするという処理が必要です。ですので以下のようなメソッドを作ります。
```java
public void endFrame() { 
    System.arraycopy( 
        pressed, 
        0, 
        previousPressed, 
        0, 
        KEY_COUNT 
        ); 
    }
```
`System.arraycopy`は高速に配列内容をコピーするメソッドです。

## `isJustPressed()`の実装
押した瞬間1回だけ実行するには、pressedが`false`から`true`になったタイミングで実行すればよいです。
```java
public boolean isJustPressed(int keyCode) { 
    return isPressed(keyCode) && !previousPressed[keyCode]; 
}
```

## `isJustReleased()`の実装
離した瞬間に実行を辞める場合、pressedが`true`から`false`になったタイミングで実行を辞めればよいです。
```java
public boolean isJustReleased(int keyCode) {
    return !isPressed(keyCode) && previousPressed[keyCode];
}
```
