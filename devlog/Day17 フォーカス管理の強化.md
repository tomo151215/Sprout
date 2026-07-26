# Day17: フォーカス管理の強化
現在、ゲーム画面表示を行ったときにCanvasにKeyListenerを登録し、キーボード入力を受け付けるようにしています。また、ウィンドウ表示時にフォーカスを要求しています。
```java
canvas.addKeyListener(k); 
canvas.setFocusable(true);
canvas.requestFocusInWindow();
```
この実装により、基本的なキー入力は受け取れます。しかし、たとえば、次のような操作をしたときに問題が起きる可能性があります。
- Alt + Tab で別ウィンドウに切り替える
- ゲーム画面外をクリックする
- ウィンドウが非アクティブになる
- キーを押したままウィンドウのフォーカスが外れる
- フォーカスが戻ったあとにキーが押されっぱなし扱いになる

## フォーカスとは何か
フォーカスとは、現在キーボード入力を受け取る対象のことです。キーボード入力を受け取れるのは、基本的にフォーカスを持っている部品です。

### フォーカスが喪失する例
プレイヤーが右キーを押すと、KeyPressedが実行されます。押したままの状態でAlt + Tab で別ウィンドウに切り替えたとするとゲームウィンドウはフォーカスを失うので、環境によってはkeyReleased がゲーム側に届かないことがあります。その結果、ゲーム画面に戻ったときに、プレイヤーが勝手に右へ動き続けるバグが発生します。

### Day17でやること
フォーカスを失ったとき、すべてのキー入力状態をリセットします。つまり、ウィンドウが非アクティブになったら、押下中のキーはすべて離された扱いにするということです。これにより、keyReleased が届かなかった場合でも、キーが押されっぱなしになる問題を防げます。具体的には以下のようなことを行います。
- Keyboardにclear()メソッドを追加する
- GameWindowでフォーカス喪失時にkeyboard.clear()を呼ぶ

## KeyBoardにclear()メソッド追加
フォーカスを失ったときは、`currentPressed`、`presed`、`previousPressed`の3つをすべて false にする必要があります。これをclear()メソッドの処理とします。
```java
public void clear() {
    synchronized (lock) {
        for (int i = 0; i < KEY_COUNT; i++) {
            currentPressed[i] = false;
            pressed[i] = false;
            previousPressed[i] = false;
        }
    }
}
```

## GameWindowでFocusListener, WindowFocusListenerを使う
### FocusListener
次に、GameWindow側で`FocusListener`を追加します。Canvas がフォーカスを失ったときに、keyboard.clear() を呼びます。`FocusListener`を直接実装すると、次の2つのメソッドをOverrideする必要があります。
```java
void focusGained(FocusEvent e); 
void focusLost(FocusEvent e);
```
`focusLost`が必要なメソッドなので2つ実装しなければないのは大変なので、`FocusAdapter`を使用します。`FocusAdapter`は、`FocusListener`の空実装を持つクラスです。必要なメソッドだけをオーバーライドできます。匿名クラスで書くとより簡潔になります。
```java
canvas.addFocusListener(new FocusAdapter() { 
    @Override 
    public void focusLost(FocusEvent e) { 
        keyboard.clear(); 
    } 
});
```

### WindowFocusListener
Canvas のフォーカスだけでなく、ウィンドウ全体のフォーカスも監視しておくと、より安全です。JFrame全体が非アクティブになったことを検知するために、次のように`WindowFocusListener`を追加します。
```java
frame.addWindowFocusListener(new WindowAdapter() {
    @Override
    public void windowLostFocus(WindowEvent e) {
        keyboard.clear();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        canvas.requestFocusInWindow();
    }
});
```
これにより、ウィンドウが非アクティブになったときにもキー状態をリセットできます。また、ウィンドウが再びアクティブになったときに、Canvas へフォーカスを戻しやすくなります。

`canvas.requestFocusInWindow();`をメソッド化しておくのもいいです。フォーカス要求を複数の場所から使うようになるからです。
```java
public void requestCanvasFocus() { 
    canvas.requestFocusInWindow(); 
}
```

### クリック時にもフォーカスを戻す
さらに安定させるため、Canvasをクリック時にもフォーカスを戻します。
```java
canvas.addMouseListener(new MouseAdapter() {
    @Override
    public void mousePressed(MouseEvent e) {
        requestCanvasFocus();
    }
});
```
これにより、何らかの理由で Canvas からフォーカスが外れていても、ゲーム画面をクリックすれば再びキー入力を受け取れるようになります。


