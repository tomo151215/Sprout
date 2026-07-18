# GameLoopとinputSystemの連携

## keyPressedにゲーム内の処理を書いてはいけない
keyPressedに以下のようなコードを書いてはいけません。
```java
@Override 
public void keyPressed(KeyEvent e) { 
    playerX -= 4; 
}
```
なぜならこのkeyPressedは押されたら1回だけ呼び出されるメソッドであり、押してる間ずっと動かすという意図は反映されないからです。このkeyPressedはあくまでイベントを通知するだけであり、押し続けているというような状態を取得するものではありません。この状態の取得はGaeObjectを継承したゲームオブジェクトのクラスのupdateメソッドで行います。

例えば、`←`を押している間は左に座標更新し続けるという操作は以下の様に書きます。
```java
@Override
public void update() { 
    if (keyboard.isPressed(KeyEvent.VK_LEFT)) { 
        playerX -= 4; 
    } 
}
```

## KeyBoardインスタンス作成に注意

現在はGameWindowクラス内でKeyboardインスタンスを生成し、Canvasに登録している処理があります。仮にGameObjectを継承した各オブジェクトとCanvasで別のKeyboardインスタンスを見ているとすると、うまく押したこと、離したことが通知されず動きません。よってKeyboardインスタンスはCanvasと共有する必要があります。そこで以下の様にGameWindowクラスのコンストラクタを改変します。
```java
...
public GameWindow(GameSettings set, Keyboard k) {
        this.set = set;

        // Canvasの準備
        canvas.setPreferredSize(new Dimension(set.getWidth(), set.getHeight()));
        canvas.addKeyListener(k);  //外部からKeyboardインスタンスを与えます
        canvas.setFocusable(true);
        ...
}
```

## GameObjectの作成
具体的にキーボードで操作できるオブジェクトを作成してみます。今回は、矢印キーの方向に進む長方形ブロックというシンプルなものを作ります。
```java
public class Block extends GameObject {
    private int width;
    private int height;
    private int speed;
    private final Keyboard k;

    public Block(Keyboard k, int width, int height,int speed) {
        super(400, 300);　　//とりあえず400×300のブロックにする
        this.k = k;
        this.width = width;
        this.height = height;
        this.speed=speed;
    }

    @Override
    public void draw(Graphics g, double alpha) {
        int drawX = (int) lerp(getPreviousX(), getX(), alpha);
        int drawY = (int) lerp(getPreviousY(), getY(), alpha);
        g.setColor(Color.BLUE);
        g.fillRect(drawX, drawY, width, height);
    }

    @Override
    public void update() {
        if (k.isPressed(KeyEvent.VK_LEFT)) {
            setX(getX() - speed);
        }

        if (k.isPressed(KeyEvent.VK_UP)) {
            setY(getY() - speed);
        }

        if (k.isPressed(KeyEvent.VK_RIGHT)) {
            setX(getX() + speed);
        }

        if (k.isPressed(KeyEvent.VK_DOWN)) {
            setY(getY() + speed);
        }
    }

}
```
このクラスでも外部からKeyboardクラスを渡しており、GameWindowと共有できるようにしています。またmainメソッドは以下の様にします。
```java
public static void main(String[] args) {
        GameSettings set = new GameSettings(
            800, 
            600, 
            "Sample Frame", 
            true, 
            true, 
            true, 
            false
        );
        SwingUtilities.invokeLater(() -> {
            Keyboard k = new Keyboard();
            GameWindow window = new GameWindow(set, k);
            window.show();
            GameRenderer renderer = new GameRenderer(window.getCanvas());
            Block block = new Block(k, 100, 50, 2);
            List<GameObject> r = new ArrayList<>();
            List<GameObject> u = new ArrayList<>();
            r.add(block);
            u.add(block);
            GameLoop loop = new GameLoop(130, renderer, r, u);
            loop.start();
        });
    }
```
実行すると、矢印で長方形を動かせるようになるはずです。


