# Day28: CameraLookAheadクラスの作成
カメラに「先読み（Look-Ahead）処理」を追加します。
プレイヤーの進行方向を少し先に映すことで、進行方向の地形や敵を把握しやすくする、アクションゲームにおいて非常に重要なカメラ制御です。

## 先読み処理とは
先読み処理とは、対象の移動方向に合わせてカメラの目標位置を少しずらす処理です。たとえば、プレイヤーが右へ移動しているなら、カメラの目標位置を右へ少しずらします。

通常の追従位置
```java
  targetX - viewportWidth / 2
```

先読みあり
```java
  targetX - viewportWidth / 2 + lookAheadX
```
`lookAheadX`が正の値なら、カメラは右側を多めに映します。

逆に、プレイヤーが左へ移動しているなら、`lookAheadX`を負の値にします。これにより、進行方向を少し先に見せることができます。

## CameraLookAheadクラスの実装
`CameraLookAhead`は、対象の移動方向を見て、カメラ用の補正量を計算します。あくまで対象の移動方向から、先読みすべき補正量を計算して保持するだけのクラスとし、直接カメラの座標を書き換えません。
### フィールド
`CameraLookAhead`には以下のような情報を持たせます。
```text
lookAheadX	   : 現在のX方向先読み量
lookAheadY	   : 現在のY方向先読み量
maxLookAheadX  : 目標とするX方向先読み量
maxLookAheadY  : 目標とするY方向先読み量
speed	       : 先読み量が目標へ近づく速さ
threshold      : 移動方向判断用しきい値
```
`speed`はLerp処理のために使用します。先読み量を一気に目標値へ切り替えると、右から左へ振り向いた瞬間に画面が激しく揺れてしまうので、現在の先読み量を少しずつ目標値へ近づける計算を行います。
```java
private double lookAheadX;
private double lookAheadY;
private double maxLookAheadX;
private double maxLookAheadY;
private double speed;
private double threshold = 0.01;
```

### コンストラクタ
```java
public CameraLookAhead(double maxLookAheadX, double maxLookAheadY, double speed) {
    validateSize(speed, maxLookAheadX, maxLookAheadY);
    this.maxLookAheadX = maxLookAheadX;
    this.maxLookAheadY = maxLookAheadY;
    this.speed = speed;
}

private void validateSize(double speed,double maxLookAheadX,double maxLookAheadY) {
    if (speed <= 0.0 || speed > 1.0) {
        throw new IllegalArgumentException("speed must be 0.0 < speed <= 1.0");
    }
    if(maxLookAheadX<0){
        throw new IllegalArgumentException("maxLookAheadX must be greater than 0.")
    }
    if(maxLookAheadY<0){
        throw new IllegalArgumentException("maxLookAheadY must be greater than 0.")
    }
}
```


### updateメソッド
先読み処理を実現するためには、以下の3つのステップで計算を行います。

- 進行方向の判定（微小な移動の無視）
進行方向は「現在位置」と「前回位置」の差分`(dx, dy)`で判定します。ただし、内部的な計算誤差でカメラがガタガタ揺れるのを防ぐため、「しきい値（threshold）」を設け、極端に小さな移動は「止まっている」とみなします。
    ```text
    dx > threshold → 右へ移動中
    dx < -threshold → 左へ移動中
    それ以外 → 停止中
    ```
- 目標とする先読み量（Target Offset）の決定
移動方向が分かったら、最終的にカメラをどれくらいずらしたいか（目標値）を決めます。
    ```text
    右移動中なら +maxLookAheadX
    左移動中なら -maxLookAheadX
   停止中なら 0.0（中央へ戻す）
    ```
- なめらかな変化（LERP処理）
先読み量を一気に目標値へ切り替えると、右から左へ振り向いた瞬間に画面が激しく揺れてしまいます。そこで、現在の先読み量を少しずつ目標値へ近づける計算を行います。
```java
public void update(GameObject target) {
    if (target == null) {
        return;
    }
    // 移動量計算
    double dx = distinctDirectionX(target);
    double dy = distinctDirectionY(target);
    // 先読み量決定
    double targetOffsetX = calculateTargetOffset(dx, maxLookAheadX);
    double targetOffsetY = calculateTargetOffset(dy, maxLookAheadY);
    // １回のupdateのカメラずらし幅
    lookAheadX += (targetOffsetX - lookAheadX) * speed;
    lookAheadY += (targetOffsetY - lookAheadY) * speed;
}

private double calculateTargetOffset(double diff, double maxLookAhead) {
    if (diff > threshold) {
        return maxLookAhead;
    }
    if (diff < threshold) {
        return -maxLookAhead;
    }
    return 0.0;
}
```

#### distinctDirectionメソッド
進行方向を判別します。GameObjectのgetX()メソッドとgetPreviousX()メソッドを使って差分が正、0、負だとそれぞれ右へ移動、停止、左へ移動と判断することができます。上下方向も同様です。
```java
public double distinctDirectionX(GameObject target) {
    return target.getX() - target.getPreviousX();
}

public double distinctDirectionY(GameObject target) {
    return target.getY() - target.getPreviousY();
}
```

### getter, setter
```java
public double getLookAheadX() {
    return lookAheadX;
}

public double getLookAheadY() {
    return lookAheadY;
}

public double getMaxLookAheadX() {
    return maxLookAheadX;
}

public double getMaxLookAheadY() {
    return maxLookAheadY;
}

public void setMaxLookAhead(double maxLookAheadX, double maxLookAheadY) {
    if (maxLookAheadX < 0.0 || maxLookAheadY < 0.0) {
        throw new IllegalArgumentException("maxLookAheadX and maxLookAheadY must be greater than 0.0.");
    }
    this.maxLookAheadX = maxLookAheadX;
    this.maxLookAheadY = maxLookAheadY;
}

public double getSpeed() {
    return speed;
}

public void setSpeed(double speed) {
    if (speed <= 0.0 || speed > 1.0) {
        throw new IllegalArgumentException("speed must be 0.0 < speed <= 1.0.");
    }
    this.speed = speed;
}

public double getThreshold() {
    return threshold;
}

public void setThreshold(double threshold) {
    if (threshold < 0.0) {
        throw new IllegalArgumentException("threshold must be threshold >= 0.0.");
    }
    this.threshold = threshold;
}

public void resetLookAhead() {
    this.lookAheadX = 0.0;
    this.lookAheadY = 0.0;
}
```

