# Day13: ActionベースのinputSystem

これまで、キーコードを直接使って入力を判定していました。
```java
if (keyboard.isPressed(KeyEvent.VK_LEFT)) { 
    player.moveLeft(); 
}
```
しかし、ゲームが知りたいのはキーコードではなく、「左へ移動したい」などのゲーム上の意味（Action）です。そこで、入力システムをActionベースに変更し、ゲームエンジンらしい設計へ進化させます。

## Actionの作成
Actionはenum型で宣言された、ゲームが理解できる言葉です。以下の様に作ります。
```java
public enum Action { 
    MOVE_LEFT, 
    MOVE_RIGHT, 
    MOVE_UP, 
    MOVE_DOWN, 
    JUMP, 
    ATTACK, 
    PAUSE 
}
```
これで、ゲームで使用される操作を定義できましたが、内部ではキーコードと対応付けないといけません。この対応付けを**キーマッピング**といいます。例えば以下の様にします。
| Action     | キー      |
| ---------- | --------- |
| MOVE_LEFT  | VK_LEFT   |
| MOVE_RIGHT | VK_RIGHT  |
| MOVE_UP    | VK_UP     |
| MOVE_DOWN  | VK_DOWN   |
| JUMP       | VK_SPACE  |
| PAUSE      | VK_ESCAPE |
この時、キーとActionの仲介をする機能が必要です。そこで以下のInputManagerクラスを作成します。

## inputManagerの作成
inputmanagerはActionとキーコードの橋渡しをして、`isJustPressed(Action.JUMP)`のように、アクション名で指定してキー押下状況を検知します。
```java
public final class InputManager<T extends Enum<T>> {
    private final Keyboard keyboard;  //委譲
    private final Map<T, List<Integer>> mappings;

    public InputManager(Keyboard keyboard, Class<T> actionClass) {
        this.keyboard = keyboard;
        this.mappings = new EnumMap<>(actionClass);
        for (T action : actionClass.getEnumConstants()) {
            mappings.put(action, new ArrayList<>());
        }
    }

    // キーマッピングを行う
    public void addMapping(T action, int keycode) {
        mappings.get(action).add(keycode);
    }

    // 指定したアクションが押されているかどうか判定
    public boolean isPressed(T action) {
        for (int keycode : mappings.get(action)) {
            if (keyboard.isPressed(keycode))
                return true;
        }
        return false;
    }

    // 指定したアクションが押された瞬間かどうかを判定
    public boolean isJustPressed(T action) {
        for (int keycode : mappings.get(action)) {
            if (keyboard.isJustPressed(keycode))
                return true;
        }
        return false;
    }

    // 指定したアクションが離された瞬間かどうかを判定
    public boolean isJustReleased(T action) {
        for (int keycode : mappings.get(action)) {
            if (keyboard.isJustReleased(keycode))
                return true;
        }
        return false;
    }
}
```
キーマップは`Map<T, List<Integer>>`で表現します。なぜ`Map<T, Integer>`ではないのかというと、アクションに対して複数のキーコードを指定できるようにするためです。WASDと矢印キーなどがその典型です。

またmapは`EnumMap`を使用します。`HashMap`でもいいですが、キーにはEnum以外入らず、EnumMapのほうがハッシュ化などをする`HashMap`よりもパフォーマンスがいいです。EnumMapのコンストラクタにはキーのEnumのクラス名を与えます。ここではInputManagerのコンストラクタ引数に`Class<T>`というTというEnum型の型情報を渡します。`Action.class`で与えることができます。

`actionClass.getEnumConstants()`でActionの各定数を配列に埋め込んで返します。この配列の要素をそれぞれEnumMapにキーとして`put`します。値には`ArrayList<>()`を入れます。

Keyboardの`isPressed`、`isJustPressed`、`isJustReleased`をActionでせていできるようなメソッドを作ります。便利なのでメソッド名は同じにします。

## 実際にゲームで使用する
BlockクラスをActionを使用して書き換えます。
```java
public enum Action {
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_UP,
    MOVE_DOWN
}
```

```java
public class Block extends GameObject {
    private int width;
    private int height;
    private int speed;
    private final InputManager<Action> input;

    public Block(InputManager<Action> input, int width, int height, int speed) {
        super(400, 300);
        this.input = input;
        this.width = width;
        this.height = height;
        this.speed = speed;
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
        if (input.isPressed(Action.MOVE_LEFT)) {
            setX(getX() - speed);
        }

        if (input.isPressed(Action.MOVE_UP)) {
            setY(getY() - speed);
        }

        if (input.isPressed(Action.MOVE_RIGHT)) {
            setX(getX() + speed);
        }

        if (input.isPressed(Action.MOVE_DOWN)) {
            setY(getY() + speed);
        }
    }

}
```
Mainクラスでキーマッピングを行い、inputManagerを使用するよう改変します。
```java
public class Main {
    public static void main(String[] args) {
        GameSettings set = new GameSettings(800, 600, "Sample Frame", true, true, true, false);
        SwingUtilities.invokeLater(() -> {
            Keyboard k = new Keyboard();
            InputManager<Action> input = new InputManager<>(k, Action.class);
            input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_LEFT);
            input.addMapping(Action.MOVE_UP, KeyEvent.VK_UP);
            input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_RIGHT);
            input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_DOWN);

            GameWindow window = new GameWindow(set, k);
            window.show();
            GameRenderer renderer = new GameRenderer(window.getCanvas());
            Block block = new Block(input, 100, 50, 2);
            List<GameObject> r = new ArrayList<>();
            List<GameObject> u = new ArrayList<>();
            r.add(block);
            u.add(block);
            GameLoop loop = new GameLoop(120, renderer, r, u, k);
            loop.start();
        });
    }
}
```
