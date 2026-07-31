# Day21: 入力の無効化
現在のInputContextだと、ゲーム中のあらゆる場面で入力が反応してしまいます。たとえば、次のような場面では、入力を止めたいことがあります。
- ポーズ中
- シーン切り替え中 
- リザルト画面表示中 
- 会話イベント中 
- ロード中 
- メニューを開いている間 
- ゲームオーバー直後

このような場面に備えて、InputContextに入力の有効・無効を一時的に切り替える機能を追加します。

##　入力の一時的な無効化と有効化
`clear()`メソッドは現在の入力状態をすべて`false`にする。というもので、入力状態をリセットするものです。一方、今回作る入力の一時的な無効化は入力状態を見に行ったときに `false`を返す仕組みです。今回は以下のような機能をInputContextに追加します。
- 入力を有効化する 
- 入力を無効化する 
- 現在有効か確認する 
- 無効中はアクション入力をfalseにする 
- 無効中はマウスボタン入力をfalseにする 
- 無効化時に入力状態をclearする

### enabledフラグの追加
InputContextに、入力が有効かどうかを表すフィールド`enabled`を追加します。
```java
private boolean enabled = true;
```
デフォルトでは `true`（入力を有効化）にします。

### `enable()`メソッドと`disable()`メソッド

入力を有効化・無効化するメソッドを作成します。どちらのメソッド内でも、事前に clear() で入力状態を初期化します。例えば、ポーズ開始時や解除時に「押されていたキーの古い入力情報」が残ることで、ポーズ解除直後に意図しないジャンプや移動が発生するのを防ぐためです。
```java
// 入力の有効化
public void enable() {
    clear();
    enabled = true;
}

// 入力の無効化
public void disable() {
    clear();
    enabled = false;
}
```
### `isEnabled()`メソッド
現在、入力が有効かどうかを確認するために isEnabled()を作ります。
```java
public boolean isEnabled() {
    return enabled;
}
```
これにより、外部から現在の状態を確認できます。

## アクション、マウス入力を無効化に対応させる
`enabled`が`true`のときのみ入力を受け付けるようにします。
```java
public boolean isPressed(T action) {
    return enabled && this.inputManager.isPressed(action);
}

public boolean isJustPressed(T action) {
    return enabled && this.inputManager.isJustPressed(action);
}

public boolean isJustReleased(T action) {
    return enabled && this.inputManager.isJustReleased(action);
}
public boolean isMousePressed(MouseButton button) {
    return enabled && this.mouse.isPressed(button);
}

public boolean isMouseJustPressed(MouseButton button) {
    return enabled && this.mouse.isJustPressed(button);
}

public boolean isMouseJustReleased(MouseButton button) {
    return enabled && this.mouse.isJustReleased(button);
}
```
マウス座標取得メソッドは入力無効化できないようにします。それはマウス座標は入力操作ではなく、位置情報だからです。また、ホイール情報を無効化する場合は、以下の様にします。
```java
public int getMouseWheelRotation() {
    return enabled ? this.mouse.getWheelRotation() : 0;
}
```

## 無効化するべきでないもの
### マッピング
入力が無効な間でも、キーマッピングの変更はできてよいです。たとえば、設定画面でキー割り当てを変更している場合、ゲーム操作は無効にしつつ、マッピング自体は変更したいかもしれません。そのため、次のメソッドは enabledに影響されないようにします。
```java
addMapping() 
removeMapping() 
clearMapping() 
clearAllMappings() 
hasMapping() 
getMappings()
```

### getter
InputContext には、低レベル入力へのgetterがあります。これらを公開している場合、外部から`keyboard.isPressed(...)`を直接呼ぶと、InputContextの enabled を無視できてしまいます。

その一方で、デバックや特殊用途のためにgetterは残しておきたいので、ゲーム本体の操作の場合はなるべく使わないようにしてそのままにします。