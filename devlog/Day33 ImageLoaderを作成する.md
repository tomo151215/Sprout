# Day33: IMageLoaderを作成する
これまでは、`g.fillRect(x, y, width, height);`などを使ってオブジェクトを表現したいましたが、よりゲームのクオリティを上げるために画像を使用できるようにします。そこで、画像を読み込む専用クラスとして`ImageLoader`を作成します。

## ImageLoaderを作る意義
画像を表示するには、Javaでは`ImageIO.read()`を使います。
```java
BufferedImage image = ImageIO.read( new File("assets/images/player.png") );
```
このように書けば、画像ファイルを読み込めます。しかし、画像を使いたい場所ごとに毎回`ImageIO.read()`を直接書くと、次のような問題があります。
```text
画像読み込み処理があちこちに散らばる 
ファイルパスの書き方がばらばらになる 
読み込み失敗時の処理が毎回必要になる 
同じ画像を何度も読み込んでしまう可能性がある
```
画像は、ゲーム開始時やScene開始時に一度だけ読み込み、描画時には読み込んだ画像を使い回すべきです。そのため、画像読み込みを担当する`ImageLoader`を作成します。`ImageLoader`の役割は、画像ファイルを読み込んで`BufferedImage`として返すことです。
```text
画像ファイルのパスを受け取る 
画像を読み込む 
BufferedImageとして返す 
読み込みに失敗したら例外を出す
```
例えば次のように使えるようにします。
```java
BufferedImage playerImage = ImageLoader.load("assets/images/player.png");
```

## BufferedImageとは
`BufferedImage`は、Javaで画像データを扱うためのクラスです。画像を読み込むと、Javaプログラムの中では`BufferedImage`として扱えます。
```java
BufferedImage image;
```
この`BufferedImage`を`Graphics2D`の`drawImage()`に渡すと、画面に画像を描画できます。
```java
g.drawImage(image, x, y, null);
//引数は、(Image img, int x, int y, ImageObserver observer)
//Image 描画したいオブジェクト
//int x,y 描画を開始する座標
//ImageObserver 画像の読み込み状態を監視するオブジェクト（通常は this または null）
```
### observerオブジェクト
Javaでは、インターネットやローカルディスクから巨大な画像ファイルを読み込む際、画像のロード完了を待たずにプログラムの処理を進める（非同期処理） 仕様になっています。
1. 最初 drawImage() が呼ばれた時点では、画像が半分しかロードされていない可能性がある。
2. observer を渡しておくと、画像のロードが少しずつ進むたびに Java 内部で「準備がここまで進んだよ！」という通知が observer に届く。
3. 通知を受けた observer が画面を再描画（repaint()）することで、画像がパラパラ漫画のように徐々に表示される。

すでにメモリ上に完全に存在する画像（ImageIO.read() などで読み込み済みの BufferedImage）を使う場合は、ロード待ちが発生しないのでoberverはnullでOKです。

## ImageLoaderの実装
```java
public final class ImageLoader {
    private ImageLoader() {
    }

    public static BufferedImage load(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load image: " + path, e);
        }
    }
}
```

### privateコンストラクタ
`ImageLoader`はインスタンス化して使用することを想定していません。そのため`private`なコンストラクタに閉じています。使用したいメソッドは`load()`メソッドであり、これは`static`にすることでインスタンス化せずに使用できるようにしています。
```java
BufferedImage image = ImageLoader.load("assets/images/player.png");
```

### ImageIO.read()
`ImageIO.read()`は、画像ファイルを読み込むためのJava標準APIです。読み込みに成功すると、`BufferedImage`を返します。読み込みに失敗した場合は、`IOException`が発生します。
```java
BufferedImage image = ImageIO.read(new File(path));
```
`ImageIO.read()`は`IOException`を投げる可能性があります。そのため、本来は次のように書く必要があります。
```java
public static BufferedImage load(String path) throws IOException { 
    return ImageIO.read(new File(path)); 
}
```
しかし、この形にすると、画像を読み込むたびに呼び出し側で`try-catch`が必要になります。
```java
try { 
    BufferedImage image = ImageLoader.load("assets/images/player.png"); 
} catch (IOException e) { 
    e.printStackTrace(); 
}
```
そこで、`ImageLoader`の中で`IOException`を受け取り、`IllegalArgumentException`として投げ直します。


### null & 空文字チェック
画像パスが`null`や空文字の場合、分かりにくいエラーになる可能性があります。そこで、`load()`の先頭でチェックしておきます。
```java
if (path == null || path.isBlank()) {
    throw new IllegalArgumentException("path must not be null or blank.");
}
```
また、`ImageIO.read()`は、場合によっては例外ではなく`null`を返すことがあります。たとえば、ファイルは存在しているけれど、画像として読み込めない場合です。そこもチェックしておきます。
```java
try {
    BufferedImage image = ImageIO.read(new File(path));
    if (image == null) {
        throw new IllegalArgumentException("Unsupported image format: " + path);
    }
    return image;
} catch (IOException e) {
    throw new IllegalArgumentException("Failed to load image: " + path, e);
}
```

## 画像は毎フレーム読み込まない
画像の描画で最も重要なのは、画像を毎フレーム読み込まないことです。`draw()`は毎フレーム呼ばれるため、60FPSなら1秒に60回読み込むことになります。これは非常に無駄です。正しくは、Scene開始時やObject生成時に一度だけ読み込みます。