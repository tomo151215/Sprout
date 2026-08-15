# Day34: Sprite画像を描画する
Day38で読み込めるようになった画像を、ゲームオブジェクトとして描画しやすくするために`Sprite`クラスを作成します。画像をそのまま`BufferedImage`として扱い続けると、ゲーム側のコードが少し読みにくくなります。今後のことを考えると、画像をゲーム用の部品として扱えるようにしておいた方がよいです。

## Spriteクラスの意義
2Dゲームでは、プレイヤー、敵、アイテム、背景、タイトルロゴなどを画像として表示します。
```text
Player画像 
Enemy画像 
Block画像 
Item画像 
Background画像 
TitleLogo画像
```
これらの画像をゲーム内で扱いやすくするために、`Sprite`というクラスで包みます。`BufferedImage`をそのまま使っても、画像は描画できますが、今後ゲームエンジンを拡張していくと、画像には次のような情報や処理が欲しくなります。
```text
画像の幅 
画像の高さ 
画像の中心位置 
画像の一部だけを切り出す 
画像を拡大縮小して描画する 
画像を左右反転する 
アニメーションの1フレームとして使う
```
これらを毎回`BufferedImage`に直接書いていくと、コードが散らばります。そこで、画像を`Sprite`としてまとめておくと、後から拡張しやすくなります。


## Spriteクラスの実装
`Sprite`は、まず画像1枚を持つだけのシンプルなクラスにします。必要な情報は次の通りです。
```text
BufferedImage image  : 画像情報
int width            : 画像の幅 
int height           : 画像の高さ
```
`BufferedImage`自体にも幅と高さを取得するメソッドがあります。
```java
image.getWidth();
image.getHeight();
```
しかし、`Sprite`側にも `getWidth()`と`getHeight()`を用意しておくと、ゲーム側から使いやすくなります。
```java
public class Sprite {
    private final BufferedImage image;

    public Sprite(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image must nut be null.");
        }
        this.image = image;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getWidth();
    }
}
```
### コンストラクタ
`Sprite`のコンストラクタでは、`BufferedImage`を受け取ります。
```java
public Sprite(BufferedImage image) {
    if (image == null) {
        throw new IllegalArgumentException("image must nut be null.");
    }
    this.image = image;
}
```
`image`が`null`の場合は、例外を出します。画像がないSpriteは描画できないからです。もし`null`を許してしまうと、後で `drawImage()`するときに分かりにくい不具合になります。

### getImage()
`Sprite`は内部に`BufferedImage`を持っています。実際に描画するときには、`Graphics2D.drawImage()`に`BufferedImage`を渡す必要があります。そのため、`getImage()`を用意します。
```java
public BufferedImage getImage() {
    return image;
}
```

### getWidth(), getHeight()
`Sprite`には、画像の幅と高さを返すメソッドも用意します。
```java
public int getWidth() {
    return image.getWidth();
}

public int getHeight() {
    return image.getWidth();
}
```
これにより、ゲーム側で画像サイズを簡単に取得できます。

## ImageLoaderとSpriteを組み合わせる
`ImageLoader`と`Sprite`を組み合わせると、以下の様に書けます。
```java
Sprite sampleSprite = new Sprite(ImageLoader.load("asset/sample.png"));
```
ただし、この書き方は少し長いです。そこで、`ImageLoader`に`loadSprite()`メソッドを作成します。
```java
public static Sprite loadSprite(String path) {
    return new Sprite(load(path));
}
```
これにより以下の様に短縮して記述できます。
```java
Sprite sampleSprite = ImageLoader.loadSprite("asset/sample.png");
```