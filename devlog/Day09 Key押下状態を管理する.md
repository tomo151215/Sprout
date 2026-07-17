# Day09: Key押下状態を管理する

## KeyPressedは押された瞬間しかわからない
KeyListenerを実装して`keyPressed()`、`keyReleased()`メソッドをOverrideしても、押された瞬間、離された瞬間しかわかりません。押し続けても事実上1度しか押したことはカウントされません。（実際には、OSが気を利かせて「キーリピート」という押し続けている間ずっと押されている判定をする入力補助機能を実行しています。これは内部で何度もkeyPressedを呼び出して疑似的に「押し続けている」という状態を検知しているにすぎません。）

ゲームでは押し続けてオブジェクトを移動させるような機能をよく持つので、「押し続ける」という状態を管理するための仕組みが必要です。

## Keyのステート管理を行うboolean配列の作成
キーボードの入力操作をプログラムで正確に扱うためには、KeyListener が受け取る「瞬間的なイベント」を、プログラム側で「継続的な状態」へと変換して保持する仕組みが不可欠です。ここで、キーの識別子（KeyCode）を添え字（インデックス）として利用する boolean 型の配列を作成し、現在のキーの押下状態を保持する手法を取ります。
- 押下時（keyPressed）: 押されたキーに対応する配列の要素を true に書き換えます。
- 解放時（keyReleased）: 離されたキーに対応する配列の要素を false に戻します。

これにより、OS側のリピート機能によって keyPressed イベントが何度も発生したとしても、配列の中身は常に true で上書きされるだけであり、プログラム側は「対象のキーが現在押し続けられている」という一貫した状態を維持できます。

## Keyboardクラスの追記
Keyboardクラスにステート管理用のboolean型配列を書きます。boolean型の配列の要素数は512とします。理由は、キーボードの全キーおよび想定される特殊キーをカバーしつつ、メモリ効率とアクセス速度を最適化するための、十分に余裕のある最小の2の累乗だからです。

```java
private static final int KEY_COUNT = 512; 
private final boolean[] pressed = new boolean[KEY_COUNT];
```
配列をフィールドとして宣言すると自動でfalseに初期化されるので、まだ押していないという状態を表現することができます。

ここで例えば`←(Left Arrow)`はキーコード`KeyEvent.VK_LEFT (= 37)`なので、キーが押されたときには
```java
pressed[KeyEvent.VK_LEFT] = true;
```
とすればいいです。

### KeyPressed()で状態を更新する
`keyPressed()`の中身を記述していきます。
```java
@Override
public void keyTyped(KeyEvent e) {
    int code = e.getKeyCode();
    if (0 <= code && code < pressed.length) {
        pressed[code] = true;
    }
}
```
`0 <= code && code < pressed.length`はpressedの範囲外にアクセスして、`ArrayIndexOutOfBoundsException`の発生を防ぐ目的で記述しています。

### keyReleased()で状態を更新する
`keyReleased()`の中身を記述していきます。
```java
@Override
public void keyTyped(KeyEvent e) {
    int code = e.getKeyCode();
    if (0 <= code && code < pressed.length) {
        pressed[code] = false;
    }
}
```

### isPressed()でpressed配列の値を取得できるようにする
ゲームはpressed配列の値を見て、押されているか離されているかどうかを確認します。配列自体はprivateで隠しているので、getter相当のメソッドを作っておきます。
```java
public boolean isPresed(int keyCode){
    return 0 <= keyCode && keyCode < pressed.length && pressed[keyCode];
}
```

### 配列範囲チェックをメソッド化しておく
`0 <= keyCode && keyCode < pressed.length`は何度も出てくるので`isWithinBounds`メソッドとして切り分けます。
```java
    private boolean isWithinBounds(int keyCode) {
        return keyCode >= 0 && keyCode < pressed.length;
    }
```
こうすることでKeyBoardクラスは以下のようになります。
```java
public class Keyboard implements KeyListener {

    private static final int KEY_COUNT = 512;
    private final boolean[] pressed = new boolean[KEY_COUNT];

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (isWithinBounds(code)) {
            pressed[code] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (isWithinBounds(code)) {
            pressed[code] = false;
        }
    }

    public boolean isPresed(int keyCode) {
        return isWithinBounds(keyCode) && pressed[keyCode];
    }

    private boolean isWithinBounds(int keyCode) {
        return keyCode >= 0 && keyCode < pressed.length;
    }
}
```