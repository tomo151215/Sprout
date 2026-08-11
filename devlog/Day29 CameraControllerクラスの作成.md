# Day29: CameraControllerクラスの作成
カメラ制御をまとめる`CameraController`クラスを作成します。現在までに、
- `Camera2D`（カメラ位置と座標変換を管理する）
- `CameraFollow`（対象を中央へ追従する）
- `CameraBounds	`（カメラ位置を範囲内に制限する）
- `CameraDeadZone`（対象が範囲外へ出たときだけ動かす）
- `CameraLookAhead`（進行方向への先読み量を計算する）

を作成しました。ここでは、これらをまとめて扱う`CameraController`を作成します。

## CameraControllerとは
`CameraController`は、`Camera2D`をどのように動かすかを管理するクラスです。`Camera2D` はカメラ本体です。一方、`CameraController`はカメラの動かし方を担当します。まとめることで得られる利点は以下です。
- カメラ更新を１か所にまとめられる
  毎回各機能を持つクラスを組み合わせてカメラ処理を行うのは大変なので、一つにまとめる方が良いです。`CameraController`にまとめれば、ゲーム側は次のように書けます。

  ```java
  cameraController.update();
  ```
- MyGameをシンプルにできる
  `MyGame`は、ゲーム固有の初期設定を書く場所です。カメラの細かい更新ロジックは、エンジン側に寄せた方が自然です。毎フレームの詳しい処理は、`CameraController`に任せられます。
- 今後のカメラ機能を追加しやすくなる
  今後、カメラにはさらに機能を追加する可能性があります。

  ```text
  画面揺れ 
  ズーム 
  固定カメラ 
  イベント用カメラ 
  複数対象を映すカメラ 
  カメラ演出
  ```
これらをすべて`MyGame`側で組み合わせると、コードが複雑になります。`CameraController` を中心にしておけば、カメラ制御を拡張しやすくなります。

## CameraControllerの実装
### フィールド、コンストラクタ
```java
private Camera2D camera;
private GameObject target;
private double viewportWidth;
private double viewportHeight;
private CameraDeadZone deadZone;
private CameraBounds bounds;
private CameraLookAhead lookAhead;

public CameraController(Camera2D camera, GameObject target, double viewportWidth, double viewportHeight) {
    if (camera == null) {
        throw new IllegalArgumentException("camera must not be null.");
    }
    validateViewportSize(viewportWidth, viewportHeight);
    this.camera = camera;
    this.target = target;
    this.viewportWidth = viewportWidth;
    this.viewportHeight = viewportHeight;
}
```
#### targetをnullにできる設計
`CameraController`のコンストラクタでは、`target`に対する`null`チェックをしていません。
```java
this.target = target;
```
これは、後から追従対象を設定できるようにするためです。たとえば、ゲーム開始時点ではプレイヤーがまだ作られていない場合があります。
その場合、先に`CameraController`だけ作っておき、あとで対象を設定できます。
```java
cameraController.setTarget(player);
```
`target`が`null`の間は、`update()`しても何もしません。
```java
if (target == null) {
    return;
}
```
このようにしておくと、カメラ制御を柔軟に扱えます。

###  updateメソッド
`CameraController`のupdate()メソッドでは、以下のフローで毎フレームカメラを更新（update）します。デッドゾーンと先読み処理を複雑に絡ませないよう、安全なルールを設けています。
- ターゲット不在チェック: ターゲットが null なら何もしない。
- モード分岐:
  - デッドゾーン設定あり: CameraDeadZone を使ってカメラを動かす。（※このモードでは先読みを無効化し、競合を防ぎます）  
  - デッドゾーン設定なし: ターゲットを画面中央に置く「通常追従」を行う。もし CameraLookAhead が設定されていれば、先読み補正量を足し合わせる。  
- 範囲制限: 最後に必ず CameraBounds を適用し、カメラがワールド外に出るのを防ぐ。
```java
public void update() {
    if (target == null) {
        return;
    }
    if (deadZone != null) {
        updateDeadZone();
    } else {
        updateFollow();
    }
    applyBounds();
}

public void updateFollow() {
    double cameraX = target.getX() - viewportWidth / 2.0;
    double cameraY = target.getY() - viewportHeight / 2.0;
    if (lookAhead != null) {
        lookAhead.update(target);
        cameraX += lookAhead.getLookAheadX();
        cameraY += lookAhead.getLookAheadY();
    }
    camera.setPosition(cameraX, cameraY);
}

public void updateDeadZone() {
    deadZone.apply(camera, target);
}

public void applyBounds() {
    if (bounds != null) {
        bounds.constrain(camera, viewportWidth, viewportHeight);
    }
}
```
CameraFollowクラスのupdateメソッドだと先読み処理を統合できないので使用しません。直接updateFollow()メソッドを作成しています。

### getter,setter,resetter
```java
public void setTarget(GameObject target) {
    this.target = target;
    if (lookAhead != null) {
        lookAhead.resetLookAhead(); 
    }
}

public GameObject getTarget() {
    return target;
}

public void setViewportSize(double viewportWidth, double viewportHeight) {
    validateViewportSize(viewportWidth, viewportHeight);
    this.viewportWidth = viewportWidth;
    this.viewportHeight = viewportHeight;
}

public double getViewportWidth() {
    return viewportWidth;
}

public double getViewportHeight() {
    return viewportHeight;
}

public void setBounds(CameraBounds bounds) {
    this.bounds = bounds;
}

public CameraBounds getBounds() {
    return bounds;
}
public void setDeadZone(CameraDeadZone deadZone) {
    this.deadZone = deadZone;
}

public CameraDeadZone getDeadZone() {
    return deadZone;
}

public void setLookAhead(CameraLookAhead lookAhead) {
    this.lookAhead = lookAhead;
}

public CameraLookAhead getLookAhead() {
    return lookAhead;
}

public void clearDeadZone() {
    this.deadZone = null;
}

public void clearLookAhead() {
    this.lookAhead = null;
}

public void clearBounds() {
    this.bounds = null;
}

private void validateViewportSize(double viewportWidth, double viewportHeight) {
    if (viewportWidth <= 0.0 || viewportHeight <= 0.0) {
        throw new IllegalArgumentException("Viewport dimensions must be greater than 0.");
    }
}
```
#### setTargetでlookAheadをresetする理由
追従対象を変更したとき、`CameraLookAhead`の値をリセットしています。
```java
public void setTarget(GameObject target) {
    this.target = target;

    if (lookAhead != null) {
        lookAhead.reset();
    }
}
```
理由は、前の対象の先読み量が残ると不自然だからです。たとえば、前の対象が右へ移動中で、`lookAheadX`が正の値になっていたとします。
その状態で別の対象へ切り替えると、新しい対象が止まっていても、カメラが右側へ寄ったままになる可能性があります。対象を切り替えたときは、一度先読み量を0へ戻す方が自然です。

#### clear系メソッドを用意する理由
`CameraController`には、次のメソッドを用意しています。
```java
public void clearDeadZone()
public void clearLookAhead()
public void clearBounds()
```
これらは、カメラ制御の一部をあとから無効にするためです。たとえば、通常ステージでは範囲制限を使い、演出中だけ一時的に解除したい場合があります。
```java
cameraController.clearBounds();
```
また、デッドゾーンを解除して中央追従に戻したい場合もあります。
```java
cameraController.clearDeadZone();
```
`null`を直接渡しても同じことはできます。
```java
cameraController.setDeadZone(null);
```
ただし、`clearDeadZone()`のようなメソッドがあると、意図が分かりやすくなります。