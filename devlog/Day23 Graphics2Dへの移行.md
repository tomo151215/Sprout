# Day23: Graphics2Dへの移行
ここでは描画処理で使う型を`Graphics`から`Graphics2D`へ移行します。以下のような高度な機能を扱いやすくするには`Graphics`より`Graphics2D`を標準にしたほうがいいです。
```text
画像の拡大縮小 
画像の回転 
透明度を変えた描画 
線幅を変えた描画 
座標変換 
カメラによる平行移動 
ズーム 
アンチエイリアス
```
`Graphics2D`は`Graphics`の子クラスなので、基本的な`Graphics`のメソッドもそのまま使えます。

## RenderableをGraphics2D対応にする
```java
public interface Renderable {
    void draw(Graphics2D g, double alpha);
}
```
これにより、`Renderable`を実装するクラスは、`Graphics2D`を使って描画できるようになります。

## GameObjectをGraphics2D対応にする
`onDraw()`メソッドの引数をGraphics2Dにします。
```java
public final void onDraw(Graphics2D g, double alpha) {
        draw(g, alpha);
}
```

## GameRenderer側の修正
`Graphics2D g = (Graphics2D) bs.getDrawGraphics();`として`g`に統一します。
```java
public void render(List<GameObject> objects, double alpha) {
    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
    try {
        clearScreen(g);
        applyRenderingHints(g);
        for (GameObject o : objects) {
            o.onDraw(g, alpha);
        }
        if (config.isDebugRender()) {
            renderDebug(g);
        }
    } finally {
        g.dispose();
    }
    bs.show();
}
```
