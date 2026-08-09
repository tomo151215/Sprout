# Day27:　`CameraDeadZone`クラスを作成します。
を作成
現在は、プレイヤーが少し動いただけでカメラがすぐに反応して動きます。これにより、画面が常に細かく動いてしまい、見づらくなることがあります。そこでここでは、カメラに「デッドゾーン」を追加します。デッドゾーンとは、プレイヤーがその範囲内にいる間は、カメラを動かさない領域のことです。
```text
プレイヤーがデッドゾーン内にいる 
↓ 
カメラは動かない 

プレイヤーがデッドゾーンの外へ出る 
↓ 
カメラが動く
```
これにより、画面が落ち着き、プレイヤーの小さな動きでカメラが揺れ続ける問題を防げます。

## デッドゾーンの実装
`CameraDeadZone`クラスを作成します。
### フィールド
`CameraDeadZone`クラスは以下のフィールドを持ちます。
```text
deadZoneX       : デッドゾーンの左端
deadZoneY       : デッドゾーンの上端
deadZoneWidth   : デッドゾーンの幅
deadZoneHeight  : デッドゾーンの高さ
```
ただし、これはワールド座標ではなく、画面上の座標として扱います。

### コンストラクタ
すべてのフィールドを初期化します。
```java
public CameraDeadZone(double deadZoneX, double deadZoneY, double deadZoneWidth, double deadZoneHeight) {
    validateSize(deadZoneWidth, deadZoneHeight);
    this.deadZoneX = deadZoneX;
    this.deadZoneY = deadZoneY;
    this.deadZoneWidth = deadZoneWidth;
    this.deadZoneHeight = deadZoneHeight;
}

private void validateSize(double width, double height) {
    if (width <= 0.0) {
        throw new IllegalArgumentException("width must be greater than 0.");
    }
    if (height <= 0.0) {
        throw new IllegalArgumentException("height must be greater than 0.");
    }
}
```

### applyメソッド
デッドゾーンの処理は、次のように考えます。
```text
対象の画面X座標がデッドゾーン左端より左にある → カメラを左へ動かす
対象の画面X座標がデッドゾーン右端より右にある → カメラを右へ動かす
対象の画面Y座標がデッドゾーン上端より上にある → カメラを上へ動かす
対象の画面Y座標がデッドゾーン下端より下にある → カメラを下へ動かす
```
対象がデッドゾーン内にいる場合は、カメラを動かしません。

```java
public void apply(Camera2D camera, GameObject target) {
    if (camera == null) {
        throw new IllegalArgumentException("camera must not be null.");
    }
    if (target == null) {
        throw new IllegalArgumentException("target must not be null.");
    }
    double deadZoneLeftX = deadZoneX;
    double deadZoneRightX = deadZoneX + deadZoneWidth;
    double deadZoneTopY = deadZoneY;
    double deadZoneBottomY = deadZoneY + deadZoneHeight;

    double targetScreenX = camera.worldToScreenX(target.getX());
    double targetScreenY = camera.worldToScreenY(target.getY());

    // 対象の画面X座標がデッドゾーン左端より左にある → カメラを左へ動かす
    if (targetScreenX < deadZoneLeftX) {
        camera.move(targetScreenX - deadZoneLeftX, 0);
    }
    // 対象の画面X座標がデッドゾーン右端より右にある → カメラを右へ動かす
    if (targetScreenX > deadZoneRightX) {
        camera.move(targetScreenX - deadZoneRightX, 0);
    }
    // 対象の画面Y座標がデッドゾーン上端より上にある → カメラを上へ動かす
    if (targetScreenY < deadZoneTopY) {
        camera.move(0, targetScreenY - deadZoneTopY);
    }
    // 対象の画面Y座標がデッドゾーン下端より下にある → カメラを下へ動かす
    if (targetScreenY > deadZoneTopY) {
        camera.move(0, targetScreenY - deadZoneTopY);
    }
}
```
### getter, setter
```java
public double getDeadZoneX() {
    return deadZoneX;
}

public double getDeadZoneY() {
    return deadZoneY;
}

public double getDeadZoneWidth() {
    return deadZoneWidth;
}

public double getDeadZoneHeight() {
    return deadZoneHeight;
}

public void setBounds(double deadZoneX, double deadZoneY, double deadZoneWidth, double deadZoneHeight) {
    validateSize(deadZoneWidth, deadZoneHeight);
    this.deadZoneX = deadZoneX;
    this.deadZoneY = deadZoneY;
    this.deadZoneWidth = deadZoneWidth;
    this.deadZoneHeight = deadZoneHeight;
}
```

## CameraFollowとCameraDeadZoneの関係
`CameraFollow`は、対象を中央に置くための処理でした。`CameraDeadZone`は、対象が一定範囲を出たときだけカメラを動かす処理です。この2つを同時にそのまま使うと、意味が重なる場合があります。たとえば、毎フレーム`CameraFollow.update()`で対象を中央に戻したあとに、`CameraDeadZone.apply()`を呼んでも、対象はすでに中央付近にいるため、デッドゾーンの効果が分かりにくくなります。よってどちらか一方を使うのが賢明です。

## デッドゾーンの大きさ
デッドゾーンを大きくすると、カメラはあまり動かなくなります。たとえば、画面のほとんどをデッドゾーンにすると、プレイヤーがかなり端まで行かないとカメラが動きません。一方で、デッドゾーンを小さくすると、カメラはプレイヤーに近い動きになります。極端に小さくすると、ほぼ通常の追従カメラと同じになります。
