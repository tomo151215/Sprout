# Day08: Keyeventを取得
ゲームではプレイヤーがキーボード入力で操作するので入力システムを開発する必要があります。Javaは常に入力を監視するのではなく、キーボードを押したり、離したりを検知することができます。この通知を**イベント（Event）**といいます。

## KeyListenerインタフェース
キーイベントを受け取るために使うのKeyListenerインタフェースです。以下の3つのメソッドを実装することでJavaからキーイベントを受け取れるようになります。
```java
public interface KeyListener { 
    void keyTyped(KeyEvent e); 
    void keyPressed(KeyEvent e); 
    void keyReleased(KeyEvent e); 
}
```

### `KeyEvent`オブジェクト
`KeyEvent`クラスは、キーボード入力に関する詳細な情報を提供する非常に多機能なクラスです。各KeyListenerを実装したメソッドの引数に`KeyEvent`クラスのオブジェクトが与えられているので、このメソッドが呼び出されるときに、入力情報がこのオブジェクトを介して、与えられることになります。以下は主な`KeyEvent`クラスのメソッドです。

#### キーの種類・情報を取得するメソッド
|メソッド|戻り値|説明|
|---|---|---|
|`getKeyChar()`|`char`|入力された文字を返します（例：`a`, `A`, `1`）。keyTypedで必須。|
|`getKeyCode()`|`int`|キーの物理的な仮想キーコードを返します（例：KeyEvent.VK_ENTER）。|
|`getKeyText(int keyCode)`|`String`|キーコードに対応する人間が読める名前を返します（例：`Enter`, `Ctrl`）。|
|`getKeyLocation()`|`int`|キーの場所を返します（左Shiftと右Shiftの区別など）。|

`KeyEvent`クラスは、キーボード入力に関する詳細な情報を提供する非常に多機能なクラスです。頻繁に使用される主要なメソッドを目的別に分類して紹介します。

#### 修飾キー（Shift, Ctrl, Alt等）の状態を確認するメソッド

| メソッド | 戻り値 | 説明 |
| --- | --- | --- |
| `isShiftDown()` | `boolean` | Shiftキーが押されていれば `true`。 |
| `isControlDown()` | `boolean` | Ctrlキーが押されていれば `true`。 |
| `isAltDown()` | `boolean` | Altキーが押されていれば `true`。 |
| `isMetaDown()` | `boolean` | Metaキー（MacのCommandキーなど）が押されていれば `true`。 |
| `isActionKey()` | `boolean` | 矢印、Home、End、F1〜F12などの操作キーなら `true`。 |

### キーコード一覧
`getKeyCode()`などで使用するキーコードの定数です。内部的には`static final int`になっています。

| カテゴリ | 定数名 | 数値 | 説明 |
| --- | --- | --- | --- |
| **文字/数字** | `VK_0` | 48 | 数字の0 |
|  | `VK_1` | 49 | 数字の1 |
|  | `VK_2` | 50 | 数字の2 |
|  | `VK_3` | 51 | 数字の3 |
|  | `VK_4` | 52 | 数字の4 |
|  | `VK_5` | 53 | 数字の5 |
|  | `VK_6` | 54 | 数字の6 |
|  | `VK_7` | 55 | 数字の7 |
|  | `VK_8` | 56 | 数字の8 |
|  | `VK_9` | 57 | 数字の9 |
|  | `VK_A` | 65 | アルファベットA |
|  | `VK_B` | 66 | アルファベットB |
|  | `VK_C` | 67 | アルファベットC |
|  | `VK_D` | 68 | アルファベットD |
|  | `VK_E` | 69 | アルファベットE |
|  | `VK_F` | 70 | アルファベットF |
|  | `VK_G` | 71 | アルファベットG |
|  | `VK_H` | 72 | アルファベットH |
|  | `VK_I` | 73 | アルファベットI |
|  | `VK_J` | 74 | アルファベットJ |
|  | `VK_K` | 75 | アルファベットK |
|  | `VK_L` | 76 | アルファベットL |
|  | `VK_M` | 77 | アルファベットM |
|  | `VK_N` | 78 | アルファベットN |
|  | `VK_O` | 79 | アルファベットO |
|  | `VK_P` | 80 | アルファベットP |
|  | `VK_Q` | 81 | アルファベットQ |
|  | `VK_R` | 82 | アルファベットR |
|  | `VK_S` | 83 | アルファベットS |
|  | `VK_T` | 84 | アルファベットT |
|  | `VK_U` | 85 | アルファベットU |
|  | `VK_V` | 86 | アルファベットV |
|  | `VK_W` | 87 | アルファベットW |
|  | `VK_X` | 88 | アルファベットX |
|  | `VK_Y` | 89 | アルファベットY |
|  | `VK_Z` | 90 | アルファベットZ |
| **制御/特殊** | `VK_ENTER` | 10 | Enterキー |
|  | `VK_BACK_SPACE` | 8 | BackSpaceキー |
|  | `VK_TAB` | 9 | Tabキー |
|  | `VK_CANCEL` | 3 | キャンセルキー |
|  | `VK_ESCAPE` | 27 | Escキー |
|  | `VK_SPACE` | 32 | スペースキー |
|  | `VK_SHIFT` | 16 | Shiftキー |
|  | `VK_CONTROL` | 17 | Ctrlキー |
|  | `VK_ALT` | 18 | Altキー |
|  | `VK_ALT_GRAPH` | 65406 | AltGraphキー |
|  | `VK_CAPS_LOCK` | 20 | CapsLockキー |
|  | `VK_NUM_LOCK` | 144 | NumLockキー |
|  | `VK_SCROLL_LOCK` | 145 | ScrollLockキー |
|  | `VK_PAUSE` | 19 | Pauseキー |
|  | `VK_PRINTSCREEN` | 154 | PrintScreenキー |
|  | `VK_HELP` | 156 | Helpキー |
|  | `VK_WINDOWS` | 524 | Windowsキー |
|  | `VK_META` | 157 | Metaキー (Commandキー) |
|  | `VK_CONTEXT_MENU` | 525 | コンテキストメニューキー |
| **ナビゲーション** | `VK_PAGE_UP` | 33 | PageUpキー |
|  | `VK_PAGE_DOWN` | 34 | PageDownキー |
|  | `VK_END` | 35 | Endキー |
|  | `VK_HOME` | 36 | Homeキー |
|  | `VK_LEFT` | 37 | 左矢印キー |
|  | `VK_UP` | 38 | 上矢印キー |
|  | `VK_RIGHT` | 39 | 右矢印キー |
|  | `VK_DOWN` | 40 | 下矢印キー |
|  | `VK_INSERT` | 155 | Insertキー |
|  | `VK_DELETE` | 127 | Deleteキー |
| **ファンクション** | `VK_F1` | 112 | F1キー |
|  | `VK_F2` | 113 | F2キー |
|  | `VK_F3` | 114 | F3キー |
|  | `VK_F4` | 115 | F4キー |
|  | `VK_F5` | 116 | F5キー |
|  | `VK_F6` | 117 | F6キー |
|  | `VK_F7` | 118 | F7キー |
|  | `VK_F8` | 119 | F8キー |
|  | `VK_F9` | 120 | F9キー |
|  | `VK_F10` | 121 | F10キー |
|  | `VK_F11` | 122 | F11キー |
|  | `VK_F12` | 123 | F12キー |
|  | `VK_F13` | 61440 | F13キー |
|  | `VK_F14` | 61441 | F14キー |
|  | `VK_F15` | 61442 | F15キー |
|  | `VK_F16` | 61443 | F16キー |
|  | `VK_F17` | 61444 | F17キー |
|  | `VK_F18` | 61445 | F18キー |
|  | `VK_F19` | 61446 | F19キー |
|  | `VK_F20` | 61447 | F20キー |
|  | `VK_F21` | 61448 | F21キー |
|  | `VK_F22` | 61449 | F22キー |
|  | `VK_F23` | 61450 | F23キー |
|  | `VK_F24` | 61451 | F24キー |
| **テンキー** | `VK_NUMPAD0` | 96 | テンキー0 |
|  | `VK_NUMPAD1` | 97 | テンキー1 |
|  | `VK_NUMPAD2` | 98 | テンキー2 |
|  | `VK_NUMPAD3` | 99 | テンキー3 |
|  | `VK_NUMPAD4` | 100 | テンキー4 |
|  | `VK_NUMPAD5` | 101 | テンキー5 |
|  | `VK_NUMPAD6` | 102 | テンキー6 |
|  | `VK_NUMPAD7` | 103 | テンキー7 |
|  | `VK_NUMPAD8` | 104 | テンキー8 |
|  | `VK_NUMPAD9` | 105 | テンキー9 |
|  | `VK_MULTIPLY` | 106 | テンキー * |
|  | `VK_ADD` | 107 | テンキー + |
|  | `VK_SEPARATOR` | 108 | テンキー 区切り記号 |
|  | `VK_SUBTRACT` | 109 | テンキー - |
|  | `VK_DECIMAL` | 110 | テンキー . |
|  | `VK_DIVIDE` | 111 | テンキー / |
| **記号** | `VK_COMMA` | 44 | , |
|  | `VK_PERIOD` | 46 | . |
|  | `VK_SLASH` | 47 | / |
|  | `VK_SEMICOLON` | 59 | ; |
|  | `VK_EQUALS` | 61 | = |
|  | `VK_OPEN_BRACKET` | 91 | [ |
|  | `VK_BACK_SLASH` | 92 | \ |
|  | `VK_CLOSE_BRACKET` | 93 | ] |
|  | `VK_QUOTE` | 222 | ' |
|  | `VK_BACK_QUOTE` | 192 | ` |
| **日本語入力** | `VK_KANA` | 21 | かなキー |
|  | `VK_KANJI` | 25 | 漢字キー |
|  | `VK_CONVERT` | 28 | 変換キー |
|  | `VK_NONCONVERT` | 29 | 無変換キー |
|  | `VK_ACCEPT` | 30 | 受け入れキー |
|  | `VK_MODECHANGE` | 31 | モード切替キー |

## Keyboardクラスの実装
KeyListenerを実装したKeyboardクラスを作成します。
```java
public class Keyboard implements KeyListener { 
    @Override 
    public void keyTyped(KeyEvent e) { 
        System.out.println(e.getKeyChar());
    } 
    
    @Override 
    public void keyPressed(KeyEvent e) { 
        System.out.println("Pressed");
    } 
    
    @Override 
    public void keyReleased(KeyEvent e) { 
        System.out.println("Released");
    } 
}
```

### `keyTyped()`メソッド
`keyTyped()`メソッドは文字入力を扱うイベントで、入力された文字を取得できます。
```java
@Override
    public void keyTyped(KeyEvent e) {
        //入力された文字を表示
        System.out.println(e.getKeyChar());
    }
```
とすると入力された文字を表示することができます。ゲームでは文字を入力するよりもShift、Ctrl、矢印キーなどを扱うことが多いので通常は使用しないことが多いです。この場合、`keyTyped()`メソッドでは無視されます。純粋な文字キーにのみに反応します。もし、これらのキーで反応してほしい場合は以下のメソッド（`keyPressed()`、`keyReleased()`）を使います。

### `keyPressed()`メソッド
`keyPressed()`メソッドはキーを押した瞬間に自動的に呼ばれます。
```java
@Override 
public void keyPressed(KeyEvent e) { 
    System.out.println("押された"); 
}
```
とするとキーを押すたびに「押された」と表示されます。



### `keyReleased()`メソッド
`keyReleased()`メソッドはキーを離した瞬間に自動的に呼ばれます。
```java
@Override 
public void keyReleased(KeyEvent e) { 
    System.out.println("離された"); 
}
```
とするとキーを離すたびに「離された」と表示されます。



## GameWindowクラスのCanvasにKeyListenerを追加
Keyboardクラスを作っただけでは何も入力を検知できません。Canvasに対してKeyListenerを追加することで検知することができます。

現在はGameWindowにCanvasオブジェクトを持っているので、そこで`canvas.addKeyListener(keyboard);`とすることで検知可能になります。

また、Canvasはフォーカス（現在キーボード入力を受け取ることが可能な状態）を持つことで入力を検知できます。以下の設定はもうすでにGameWindowで行っています。
```java
canvas.setFocusable(true); 
canvas.requestFocusInWindow();
```
### GameWindowクラスの修正
gameWindowクラスのコンストラクタ内で`canvas.addKeyListener(keyboard);`を追加します。
```java
public GameWindow(GameSettings set) {
        this.set = set;

        // Canvasの準備
        canvas.setPreferredSize(new Dimension(set.getWidth(), set.getHeight()));
        canvas.addKeyListener(new Keyboard());　　//追加
        canvas.setFocusable(true);

        // ウィンドウの基本設定
        frame.setTitle(set.getTitle());
        frame.setResizable(set.isResizable());

        if (set.isExitOnClose()) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }

        // コンポーネントの配置
        frame.add(canvas);
        frame.pack();

        // サイズ確定後に中央寄せ
        if (set.isCenterOnScreen()) {
            frame.setLocationRelativeTo(null);
        }
    }
```

このように書くことで、キーの入力を検知できるようになります。