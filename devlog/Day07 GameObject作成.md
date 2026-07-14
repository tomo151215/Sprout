# Day07: GameObjectクラスの作成

ゲームにはプレイヤー、敵、弾、アイテムなど、さまざまなオブジェクトが登場します。これらのオブジェクトはそれぞれ異なる見た目や動きを持っていますが、「更新処理を行う」「描画を行う」「座標を保持する」といった基本的な機能は共通しています。

これらの共通機能を各クラスで何度も実装するとコードの重複が増え、保守性も低下してしまいます。そこで、ゲーム内オブジェクトの共通処理をまとめた抽象クラス `GameObject` を作成し、すべてのゲームオブジェクトがこのクラスを継承する構成にします。これにより、ゲームオブジェクトを作成する際には、そのオブジェクト固有の処理だけを実装すればよくなり、開発効率を向上させることができます。

## GameObjectクラス

```java
package engine.object;

import engine.graphics.Renderable;
import engine.update.Updatable;
import java.awt.Graphics;

public abstract class GameObject implements Renderable, Updatable {
    // 現在位置
    private double x;
    private double y;

    // 前フレームの位置
    private double previousX;
    private double previousY;

    public GameObject(double x, double y) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
    }

    public final void onUpdate() {
        this.previousX = x;
        this.previousY = y;
        update();
    }

    public final void onDraw(Graphics g, double alpha) {
        draw(g, alpha);
    }

    // Lerpメソッド
    protected final double lerp(double start, double end, double alpha) {
        return (1 - alpha) * start + alpha * end;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getPreviousX() {
        return previousX;
    }

    public double getPreviousY() {
        return previousY;
    }
}
```

`GameObject` は `Renderable` と `Updatable` を実装しているため、すべてのゲームオブジェクトは描画処理を行う `draw()` メソッドと更新処理を行う `update()` メソッドを持つことになります。ただし、プレイヤーや敵などによって描画内容や動作は異なるため、この抽象クラスではこれらのメソッドを実装せず、継承先のクラスでそれぞれ実装します。

## `onUpdate()`メソッド

```java
public final void onUpdate() {
    this.previousX = x;
    this.previousY = y;
    update();
}
```

`onUpdate()` は、すべてのゲームオブジェクトに共通する更新処理を担当します。

まず、現在の座標を `previousX` と `previousY` に保存し、その後 `update()` を呼び出して次のフレームの状態へ更新します。これにより、「前フレームの座標」と「現在の座標」の両方を保持できるようになります。

この情報は、固定時間ステップのゲームループで描画補間（Interpolation）を行う際に利用します。描画時には前フレームと現在の座標を線形補間（Lerp）することで、更新回数（UPS）が描画回数（FPS）より少ない場合でも、滑らかな移動を表現できます。

また、このメソッドは `final` として宣言しています。これは、継承先でオーバーライドして処理の順序を変更されないようにするためです。もし `update()` を先に実行してしまうと、前フレームの座標が正しく保存されず、描画補間が正常に動作しなくなります。

このように、「共通処理は基底クラスが担当し、個別の処理だけを継承先で実装する」という設計は、**Template Methodパターン**と呼ばれる代表的なオブジェクト指向設計の一つです。

## `onDraw()`メソッド

```java
public final void onDraw(Graphics g, double alpha) {
    draw(g, alpha);
}
```

`onDraw()` は、ゲームオブジェクトの描画処理を呼び出すための共通メソッドです。

実際の描画内容は継承先で実装した `draw()` メソッドに任せていますが、ゲームループからはすべてのオブジェクトに対して `onDraw()` を呼び出すだけで描画を行えるようになります。

また、引数 `alpha` は描画補間に使用する補間係数です。`alpha` の値は 0.0〜1.0 の範囲となり、前フレームと現在フレームの間のどの位置を描画するかを表します。

例えば描画時には、

```java
double drawX = lerp(getPreviousX(), getX(), alpha);
```

のように補間座標を求めることで、更新処理は一定間隔のまま、描画だけを滑らかにすることができます。

## `lerp()`メソッド

```java
protected final double lerp(double start, double end, double alpha) {
    return (1 - alpha) * start + alpha * end;
}
```

`lerp()` は **Linear Interpolation（線形補間）** を行うメソッドです。

`start` と `end` の間を `alpha` の割合だけ補間した値を返します。

* `alpha = 0.0` の場合は `start`
* `alpha = 1.0` の場合は `end`
* `alpha = 0.5` の場合は両者の中間

となります。

このメソッドを `GameObject` に用意しておくことで、プレイヤーや敵などすべてのゲームオブジェクトが共通の補間処理を利用でき、コードの重複を防ぐことができます。
