# Day01: ウィンドウ表示を行う

Swingの `JFrame` を使って空のウィンドウを表示します。ゲームの画面はまだ作りません。「実行し、GUIウィンドウを持ち、閉じると終了する」という最小単位を確実に実装します。

## Swingとは

SwingはJava標準ライブラリに含まれるGUIツールキットです。`javax.swing` パッケージにあります。ゲームエンジン専用ではありませんが、標準ライブラリだけでウィンドウ、入力、描画を試すには十分です。
Swingの代表的な部品は以下のようなものがあります。
```text
JFrame: ウィンドウ
JPanel: Swingの描画や部品配置によく使うパネル
JButton: ボタン
JLabel: ラベル
```
ここで、`JFrame` はゲーム画面そのものではありません。ウィンドウの外枠です。ゲーム画面は後で `Canvas` として中に置きます。

## EDTとは

Swingには Event Dispatch Thread、略してEDTという専用スレッドがあります。EDTは以下のようなGUIイベントを処理します。
```text
マウスクリック
キー入力
ウィンドウ再描画
ボタン操作
GUI部品の更新
```
Swingの部品を作ったり変更したりする処理は、基本的にEDT上で実行します。そのため `SwingUtilities.invokeLater` を使います。Swingでは、ウィンドウを表示するとEDTなどのスレッドが動き続けるため、ウィンドウを閉じるまでアプリケーションが残ります。
```java
SwingUtilities.invokeLater(() -> {
    // Swingの部品作成・表示
});
```

## ウィンドウ表示の最小限のコード

```java
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("My Java Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
```

## それぞれの行の意味

```java
JFrame frame = new JFrame("My Java Game");
```

ウィンドウを作ります。文字列はタイトルバーに表示されます。

```java
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
```

ウィンドウを閉じたらJavaプロセスも終了する設定です。これを指定しないと、ウィンドウを閉じてもプロセスが残ることがあります。

```java
frame.setSize(800, 600);
```

ウィンドウ全体のサイズを指定します。タイトルバーや枠も含む点に注意してください。後日、ゲーム描画領域そのものは `Canvas` に指定します。

```java
frame.setLocationRelativeTo(null);
```

画面中央付近に表示します。

```java
frame.setVisible(true);
```

ウィンドウを実際に表示します。作っただけでは画面に出ません。


