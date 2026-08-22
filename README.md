<div align="center">

<img width="400" src="devlog/img/Sprout_logo_dark_mode.jpg#gh-dark-mode-only" alt="Sprout Game Engine">
<img width="400" src="devlog/img/Sprout_logo_light_mode.jpg#gh-light-mode-only" alt="Sprout Game Engine">

# 🌱 Sprout Game Engine

### Lightweight Java 2D Game Engine

Java Swing / AWTをベースに、ゲームエンジンの仕組みを学びながら開発している軽量2Dゲームエンジンです。

[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://www.java.com/)
![Size](https://img.shields.io/github/repo-size/tomo151215/Sprout?style=flat&logo=codeforces)
![GitHub last commit](https://img.shields.io/github/last-commit/tomo151215/Sprout?style=flat&logo=databricks)
[![IDE](https://img.shields.io/badge/IDE-VS_Code-007ACC?style=flat&logo=visualstudiocode&logoColor=white)](https://code.visualstudio.com/)

</div>

---

## Sproutとは

Sprout Game Engineは、Java Swing / AWTの上に2Dゲーム開発で必要となる基本機能を段階的に構築しているゲームエンジンです。単にゲームを動かすだけでなく、ゲームループ・描画・入力・オブジェクト管理・画像・カメラ・Scene・Audio・エンジンライフサイクルをそれぞれ独立した責務として分離し、拡張しやすい構造を目指しています。

> **Note**
>
> 現在は開発・学習段階です。APIやディレクトリ構成は今後変更される可能性があります。

## 主な機能

| 機能               | 主なクラス              | 概要                                              |
| ------------------ | ----------------------- | ------------------------------------------------- |
| アプリケーション   | `GameApplication`       | エンジンの生成・起動・終了を統一                  |
| エンジン           | `GameEngine`            | Object、System、Input、Camera、Scene、Audioを統括 |
| ゲームループ       | `GameLoop`              | Fixed Timestepによる更新と描画タイミングを管理    |
| 描画               | `GameRenderer`          | `BufferStrategy` を使った描画                     |
| 描画設定           | `RendererConfig`        | 背景色、アンチエイリアス、補間を設定              |
| ゲームオブジェクト | `GameObject`            | 座標、更新、描画、前回座標、線形補間を共通化      |
| 画像読込           | `ImageLoader`           | ファイルから `BufferedImage` / `Sprite` を生成    |
| Sprite             | `Sprite`                | 画像を保持し、サイズ取得・各種リサイズを提供      |
| キーボード         | `InputContext`          | 物理キーとゲーム内Actionを分離                    |
| マウス             | `InputContext`          | 座標、移動量、ボタン、ホイールを取得              |
| カメラ             | `Camera2D`              | カメラ座標とWorld / Screen座標変換を管理          |
| カメラ制御         | `CameraController`      | Follow / Look Ahead / Dead Zone / Boundsを統括    |
| Scene              | `Scene`, `SceneManager` | Title、Gameplayなどゲーム状態を管理               |
| Audio              | `AudioManager`          | SE再生、BGMループ再生                             |
| System             | `GameSystem`            | GameObject以外の更新システムを登録                |

---

# 目次

- [プロジェクト構成](#プロジェクト構成)
- [導入](#導入)
- [Tutorial - Sprout Mini Worldを作る](#tutorial---sprout-mini-worldを作る)
  - [1. GameApplicationを作る](#1-gameapplicationを作る)
  - [2. GameObjectを作る](#2-gameobjectを作る)
  - [3. 画像を読み込んでSpriteを描画する](#3-画像を読み込んでspriteを描画する)
  - [4. キーボード入力を追加する](#4-キーボード入力を追加する)
  - [5. SpriteObjectを操作する](#5-spriteobjectを操作する)
  - [6. マウス入力を使う](#6-マウス入力を使う)
  - [7. カメラを追加する](#7-カメラを追加する)
  - [8. Look Aheadを追加する](#8-look-aheadを追加する)
  - [9. Camera Boundsを追加する](#9-camera-boundsを追加する)
  - [10. Dead Zoneを追加する](#10-dead-zoneを追加する)
  - [11. Audioを使う](#11-audioを使う)
  - [12. Sceneを使う](#12-sceneを使う)
  - [13. 完成したミニゲーム](#13-完成したミニゲーム)
- [API Quick Reference](#api-quick-reference)
- [Engine Architecture](#engine-architecture)
- [更新と描画](#更新と描画)
- [設計方針](#設計方針)
- [DevLog Archive](#devlog-archive)
- [Author](#author)

---

# プロジェクト構成

現在のSproutは、おおよそ次のようなパッケージ構成になっています。

```text
Sprout/
├─ devlog/
└─ src/
   ├─ engine/
   │  ├─ asset/
   │  │  ├─ ImageLoader.java
   │  │  └─ Sprite.java
   │  ├─ audio/
   │  │  └─ AudioManager.java
   │  ├─ core/
   │  │  ├─ ComponentRegistry.java
   │  │  ├─ GameApplication.java
   │  │  ├─ GameEngine.java
   │  │  └─ GameSettings.java
   │  ├─ graphics/
   │  │  ├─ GameRenderer.java
   │  │  ├─ Renderable.java
   │  │  ├─ RendererConfig.java
   │  │  └─ camera/
   │  │     ├─ Camera2D.java
   │  │     ├─ CameraBounds.java
   │  │     ├─ CameraController.java
   │  │     ├─ CameraDeadZone.java
   │  │     ├─ CameraFollow.java
   │  │     └─ CameraLookAhead.java
   │  ├─ input/
   │  │  ├─ InputContext.java
   │  │  ├─ InputManager.java
   │  │  ├─ Keyboard.java
   │  │  ├─ Mouse.java
   │  │  └─ MouseButton.java
   │  ├─ loop/
   │  │  └─ GameLoop.java
   │  ├─ object/
   │  │  └─ GameObject.java
   │  ├─ scene/
   │  │  ├─ Scene.java
   │  │  └─ SceneManager.java
   │  ├─ system/
   │  │  └─ GameSystem.java
   │  ├─ update/
   │  │  └─ Updatable.java
   │  └─ window/
   │     ├─ GameWindow.java
   │     └─ SwingExecutor.java
   └─ game/
      ├─ assets/
      │  ├─ player.png
      │  └─ audio/
      ├─ Action.java
      ├─ Main.java
      ├─ SampleGame.java
      ├─ SpriteObject.java
      └─ WorldObject.java
```

`engine` がゲームエンジン本体、`game` がSproutを利用して作るゲーム側のコードです。現在のサンプルではPlayer画像を `src/game/assets/player.png` から読み込んでいます。`ImageLoader` / `AudioManager` はファイルパスを受け取るため、実際の配置に合わせてパスを指定してください。

# 導入

## 必要環境

- JDK 21以上
- Git
- Java対応IDE / エディタ
  - IntelliJ IDEA
  - Visual Studio Code
  - Eclipse

## Clone

```bash
git clone https://github.com/tomo151215/Sprout.git
cd Sprout
```

現在は試作段階のため、ライブラリとして配布するのではなく、Sproutのソースコードを直接利用する形式を想定しています。ゲーム側のコードは基本的に次の場所へ作成します。
```text
src/game/
```
# Tutorial - Sprout Mini Worldを作る

このTutorialでは、Sproutの主要機能を確認しながら **Sprout Mini World** という小さな2Dサンプルを作ります。現在のサンプルでは `SpriteObject` をWASD / 矢印キーで操作し、広いワールドをCameraが追従します。

```text
Keyboard / Mouse
       │
       ▼
  InputContext
       │
       ▼
  SpriteObject ─── Sprite
       │
       ▼
   GameObject
       │
       ▼
CameraController
       │
       ▼
    Camera2D
```

| Step | 追加する機能 | 主なクラス |
| ---: | --- | --- |
| 1 | ゲームの起動 | `GameApplication`, `Main` |
| 2 | ゲームオブジェクト | `GameObject` |
| 3 | 画像 / Sprite | `ImageLoader`, `Sprite` |
| 4 | キーボード入力 | `InputContext` |
| 5 | 操作オブジェクト | `SpriteObject`, `InputContext` |
| 6 | マウス入力 | `InputContext`, `MouseButton` |
| 7 | Camera | `Camera2D`, `CameraController` |
| 8 | 進行方向の先読み | `CameraLookAhead` |
| 9 | ワールド範囲 | `CameraBounds` |
| 10 | Dead Zone | `CameraDeadZone` |
| 11 | Audio | `AudioManager` |
| 12 | Scene | `Scene`, `SceneManager` |
| 13 | 完成サンプル | 現在のサンプルコードを統合 |

# 1. GameApplicationを作る

Sproutを使うゲームは `GameApplication` を継承して作成し、`Main` から `run()` を呼び出して起動します。

```java
public final class SampleGame extends GameApplication {
    @Override
    protected GameSettings createSettings() {
        return GameSettings.builder().size(800, 600).title("Sprout Mini World").resizable(false).centerOnScreen(true).build();
    }

    @Override
    protected RendererConfig createRendererConfig() {
        return RendererConfig.builder().backgroundColor(Color.WHITE).antiAliasing(true).interpolation(true).build();
    }

    @Override
    protected void onInit() {
        // GameObjectやInput、Cameraなどを初期化
    }

    @Override
    protected int targetUps() {
        return 100;
    }
}
```

起動用クラスは次のようにします。

```java
public class Main {
    private Main() {
    }

    public static void main(String[] args) {
        new SampleGame().run();
    }
}
```

`GameApplication` がゲーム起動に必要な設定・初期化・終了順序を管理します。

```text
GameApplication
      │
      ▼
  GameEngine
      │
      ├─ Window
      ├─ Renderer
      ├─ GameLoop
      ├─ Keyboard / Mouse
      ├─ SceneManager
      └─ AudioManager
```

## `GameApplication` チートシート

| メソッド | 役割 |
| --- | --- |
| `createSettings()` | ウィンドウ設定を作成 |
| `createRendererConfig()` | 描画設定を作成 |
| `targetUps()` | 1秒あたりの更新回数を設定 |
| `onInit()` | ゲーム開始前の初期化 |
| `onShutdown()` | ゲーム終了時の処理 |
| `engine()` | `GameEngine` を取得 |
| `run()` | ゲームを起動 |
| `stop()` | ゲームを終了 |

`targetUps()` のデフォルト値は60です。現在のサンプルでは100へOverrideしています。

## `GameSettings` Builder

| メソッド | 説明 |
| --- | --- |
| `size(width, height)` | ウィンドウサイズ |
| `title(title)` | タイトル |
| `visible(boolean)` | 表示するか |
| `centerOnScreen(boolean)` | 画面中央へ配置するか |
| `exitOnClose(boolean)` | 閉じる操作で終了するか |
| `resizable(boolean)` | サイズ変更を許可するか |
| `build()` | 設定を生成 |

`size()` と `title()` は `build()` 前に設定する必要があります。

## `RendererConfig` Builder

| メソッド | 説明 |
| --- | --- |
| `backgroundColor(color)` | 背景色 |
| `antiAliasing(boolean)` | アンチエイリアス |
| `interpolation(boolean)` | 描画補間 |
| `build()` | 設定を生成 |

# 2. GameObjectを作る

Sprout上で更新・描画されるゲームオブジェクトは `GameObject` を継承して作成できます。まずは画像を使わず、四角形を描画する最小のオブジェクトを作ります。

```java
public final class Box extends GameObject {
    public Box(double x, double y) {
        super(x, y);
    }

    @Override
    protected void onUpdate() {
    }

    @Override
    protected void onDraw(Graphics2D graphics, double alpha) {
        int renderX = (int) interpolatedX(alpha);
        int renderY = (int) interpolatedY(alpha);
        graphics.setColor(Color.GREEN);
        graphics.fillRect(renderX, renderY, 48, 48);
    }
}
```

`onUpdate()` はゲーム状態を更新する場所、`onDraw()` は描画する場所です。Sproutは現在位置だけでなく前回位置も保持し、`interpolatedX(alpha)` / `interpolatedY(alpha)` によって描画用の補間座標を求められます。

## `GameObject` チートシート

| メソッド                                        | 説明                       |
| ----------------------------------------------- | -------------------------- |
| `onUpdate()`                                    | オブジェクト固有の更新処理 |
| `onDraw(graphics, alpha)`                       | オブジェクト固有の描画     |
| `getX()` / `getY()`                             | 現在座標を取得             |
| `getPreviousX()` / `getPreviousY()`             | 更新前の座標を取得         |
| `setX(x)` / `setY(y)`                           | 座標を設定                 |
| `setPosition(x, y)`                             | X / Y座標を同時に設定      |
| `move(dx, dy)`                                  | 現在位置から相対移動       |
| `interpolatedX(alpha)` / `interpolatedY(alpha)` | 補間された描画座標         |
| `lerp(start, end, alpha)`                       | 線形補間                   |

登録は `GameEngine` から行います。

```java
engine().addObject(new Box(100, 100));
```
登録された `GameObject` はエンジンによって自動的に更新・描画されます。

# 3. 画像を読み込んでSpriteを描画する

Sproutでは画像の読み込みとサイズ調整に `ImageLoader` と `Sprite` を使用します。

| クラス | 役割 |
| --- | --- |
| `ImageLoader` | 画像ファイルを読み込む |
| `Sprite` | 画像を保持し、サイズ取得・リサイズを行う |

```text
画像ファイル → ImageLoader → Sprite → GameObject → Graphics2D.drawImage()
```

## 画像を読み込む

```java
BufferedImage image = ImageLoader.load("src/game/assets/player.png");
Sprite sprite = ImageLoader.loadSprite("src/game/assets/player.png");
```

## `ImageLoader` チートシート

| メソッド | 戻り値 | 説明 |
| --- | --- | --- |
| `load(path)` | `BufferedImage` | 画像を読み込む |
| `loadSprite(path)` | `Sprite` | 画像を読み込みSpriteを生成 |

## Spriteを操作する

```java
BufferedImage image = sprite.getImage();
int width = sprite.getWidth();
int height = sprite.getHeight();

Sprite resized = sprite.resized(64, 64);
Sprite byWidth = sprite.resizedByWidth(64);
Sprite byHeight = sprite.resizedByHeight(64);
Sprite scaled = sprite.scaled(2.0);
Sprite fitted = sprite.resizedToFit(200, 200);
Sprite filled = sprite.resizedToFill(200, 200);
```

リサイズ系メソッドは元のSpriteを書き換えず、新しい `Sprite` を返します。現在のリサイズ補間には `NEAREST_NEIGHBOR` を使用しています。

## `Sprite` チートシート

| メソッド | 説明 |
| --- | --- |
| `getImage()` | `BufferedImage` を取得 |
| `getWidth()` | 画像幅を取得。画像がない場合は `0` |
| `getHeight()` | 画像高さを取得。画像がない場合は `0` |
| `resized(width, height)` | 指定した幅・高さへ変更 |
| `resizedByWidth(width)` | アスペクト比を維持して指定幅へ変更 |
| `resizedByHeight(height)` | アスペクト比を維持して指定高さへ変更 |
| `scaled(scale)` | 指定倍率で拡大・縮小 |
| `resizedToFit(maxWidth, maxHeight)` | アスペクト比を維持して指定範囲内へ収める |
| `resizedToFill(minWidth, minHeight)` | アスペクト比を維持して指定範囲を覆うサイズに変更 |

### リサイズ方法の使い分け

| 目的 | メソッド |
| --- | --- |
| 幅と高さを直接指定 | `resized()` |
| 幅だけ指定 | `resizedByWidth()` |
| 高さだけ指定 | `resizedByHeight()` |
| 倍率で指定 | `scaled()` |
| 枠内に画像全体を収める | `resizedToFit()` |
| 枠全体を画像で覆う | `resizedToFill()` |

`resized()` は縦横比が変化する場合があります。それ以外の上記リサイズメソッドはアスペクト比を維持します。

## GameObjectでSpriteを描画する

`Sprite` 自体は自動描画されないため、`GameObject` の `onDraw()` から描画します。

```java
public final class SpriteObject extends GameObject {
    private final Sprite sprite;

    public SpriteObject(double x, double y, Sprite sprite) {
        super(x, y);
        this.sprite = sprite;
    }

    @Override
    protected void onUpdate() {
    }

    @Override
    protected void onDraw(Graphics2D graphics, double alpha) {
        int renderX = (int) interpolatedX(alpha);
        int renderY = (int) interpolatedY(alpha);
        graphics.drawImage(sprite.getImage(), renderX, renderY, null);
    }
}
```

現在の完成サンプルでは、`SpriteObject` 側で描画幅を80pxに指定し、高さを元画像の比率から計算して描画しています。

# 4. キーボード入力を追加する

Sproutでは物理キーとゲーム内Actionを分離します。

```text
Keyboard
   │
   ▼
InputContext
   │
   ▼
 Action
   │
   ▼
Game Logic
```

まずゲーム内Actionを定義します。

```java
package game;

public enum Action {
    MOVE_LEFT,
    MOVE_UP,
    MOVE_RIGHT,
    MOVE_DOWN,
    JUMP
}
```

次に `InputContext` を作成し、Actionへ物理キーを割り当てます。

```java
private InputContext<Action> input;

private void configureInput() {
    input = engine().createInputContext(Action.class);
    input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_A);
    input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_LEFT);
    input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_D);
    input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_RIGHT);
    input.addMapping(Action.MOVE_UP, KeyEvent.VK_W);
    input.addMapping(Action.MOVE_UP, KeyEvent.VK_UP);
    input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_S);
    input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_DOWN);
    input.addMapping(Action.JUMP, KeyEvent.VK_SPACE);
}
```

1つのActionへ複数の物理キーを割り当てられます。

## キーマッピング チートシート

| メソッド                         | 説明                           |
| -------------------------------- | ------------------------------ |
| `addMapping(action, keyCode)`    | キーを追加                     |
| `removeMapping(action, keyCode)` | キーを削除                     |
| `clearMapping(action)`           | 指定Actionのマッピングを削除   |
| `clearAllMappings()`             | すべてのマッピングを削除       |
| `getMappings(action)`            | 割り当てられたキー一覧を取得   |
| `hasMapping(action, keyCode)`    | 指定キーが登録されているか確認 |

---

# 5. SpriteObjectを操作する

現在のサンプルでは、Player役を `SpriteObject` として実装しています。`InputContext` と `Sprite` を受け取り、入力に応じて移動しながら画像を描画します。

```java
public final class SpriteObject extends GameObject {
    private final Sprite sprite;
    private final int targetWidth = 80;
    private static final double SPEED = 3.0;
    private final InputContext<Action> input;

    public SpriteObject(double x, double y, InputContext<Action> input, Sprite sprite) {
        super(x, y);
        this.sprite = sprite;
        this.input = input;
    }

    @Override
    protected void onUpdate() {
        if (input.isPressed(Action.MOVE_RIGHT)) move(SPEED, 0.0);
        if (input.isPressed(Action.MOVE_LEFT)) move(-SPEED, 0.0);
        if (input.isPressed(Action.MOVE_DOWN)) move(0.0, SPEED);
        if (input.isPressed(Action.MOVE_UP)) move(0.0, -SPEED);
    }

    @Override
    protected void onDraw(Graphics2D graphics, double alpha) {
        int renderX = (int) interpolatedX(alpha);
        int renderY = (int) interpolatedY(alpha);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(sprite.getImage(), renderX, renderY, targetWidth, calculateHeight(), null);
    }

    private int calculateHeight() {
        int width = sprite.getWidth();
        if (width <= 0) return 0;
        double rate = (double) targetWidth / width;
        return (int) Math.round(sprite.getHeight() * rate);
    }
}
```

`targetWidth = 80` とし、高さは元画像のアスペクト比から計算しています。移動速度は1updateあたり3.0です。現在のサンプルは100 UPSなので、キーを押し続けた場合の理論上の移動量は約300 world units / 秒です。

## 入力状態

| メソッド | 意味 |
| --- | --- |
| `isPressed(action)` | 現在押されている |
| `isJustPressed(action)` | 今回のupdateで押された |
| `isJustReleased(action)` | 今回のupdateで離された |

現在の `Action.JUMP` はキー割り当てまで行っていますが、`SpriteObject` 側のジャンプ処理はまだ実装していません。

# 6. マウス入力を使う

マウス入力も同じ `InputContext` から取得できます。

## マウス座標

```java
int mouseX = input.getMouseX();
int mouseY = input.getMouseY();
int deltaX = input.getMouseDeltaX();
int deltaY = input.getMouseDeltaY();
```

| メソッド                                      | 説明             |
| --------------------------------------------- | ---------------- |
| `getMouseX()` / `getMouseY()`                 | 現在のマウス座標 |
| `getMousePreviousX()` / `getMousePreviousY()` | 前回のマウス座標 |
| `getMouseDeltaX()` / `getMouseDeltaY()`       | 前回からの移動量 |

## マウスボタン

```java
if (input.isMouseJustPressed(MouseButton.LEFT)) {
    System.out.println("Click!");
}
```

| 値                   | ボタン |
| -------------------- | ------ |
| `MouseButton.LEFT`   | 左     |
| `MouseButton.MIDDLE` | 中央   |
| `MouseButton.RIGHT`  | 右     |

| メソッド                      | 説明             |
| ----------------------------- | ---------------- |
| `isMousePressed(button)`      | 現在押されている |
| `isMouseJustPressed(button)`  | 今回押された     |
| `isMouseJustReleased(button)` | 今回離された     |

## マウスホイール

```java
int rotation = input.getMouseWheelRotation();
```

| メソッド                  | 説明                                 |
| ------------------------- | ------------------------------------ |
| `getMouseWheelRotation()` | 今回のupdateで取得したホイール回転量 |

## InputContextの有効・無効

```java
input.disable();
input.enable();
```

| メソッド      | 説明           |
| ------------- | -------------- |
| `enable()`    | 入力を有効化   |
| `disable()`   | 入力を無効化   |
| `isEnabled()` | 有効状態を取得 |

無効化するとActionとマウスボタンの判定は `false`、ホイール回転量は `0` になります。マウス座標と移動量は引き続き取得できます。

---

# 7. カメラを追加する

SproutのRendererは1つの `Camera2D` を持っています。

```java
Camera2D camera = engine().getCamera();
```

カメラは直接操作できます。

```java
camera.setPosition(100, 50);
camera.move(10, 0);
```

Playerを自動追従させる場合は `CameraController` を使います。

```java
CameraController cameraController = new CameraController(engine().getCamera(), playerObject, 800, 600);
engine().addSystem(cameraController);
```

`800 × 600` はCameraが表示するビューポートサイズです。`CameraController` は `GameSystem` として登録され、エンジンから自動的に更新されます。

## `Camera2D` チートシート

| メソッド                                  | 説明                            |
| ----------------------------------------- | ------------------------------- |
| `getX()` / `getY()`                       | Camera座標を取得                |
| `setPosition(x, y)`                       | 絶対位置を指定                  |
| `move(dx, dy)`                            | 相対移動                        |
| `screenToWorldX(x)` / `screenToWorldY(y)` | Screen座標をWorld座標へ変換     |
| `worldToScreenX(x)` / `worldToScreenY(y)` | World座標をScreen座標へ変換     |
| `apply(graphics)`                         | `Graphics2D` へCamera変換を適用 |

マウスのスクリーン座標をワールド座標へ変換する場合は次のように書けます。

```java
double worldX = camera.screenToWorldX(input.getMouseX());
double worldY = camera.screenToWorldY(input.getMouseY());
```

---

# 8. Look Aheadを追加する

`CameraLookAhead` はPlayerの移動方向に応じてCameraの追従位置を少し先へずらします。

```java
CameraLookAhead lookAhead = new CameraLookAhead(120, 80, 0.1);
cameraController.setLookAhead(lookAhead);
```

この例ではX方向に最大120、Y方向に最大80だけ先読みします。`0.1` は現在の先読み量が目標値へ近づく速度です。

> 現在の完成サンプルではLook Aheadの設定行をコメントアウトしています。使用する場合はコメントを解除してください。

## `CameraLookAhead` チートシート

| メソッド                                    | 説明                                   |
| ------------------------------------------- | -------------------------------------- |
| `update(target)`                            | ターゲットの移動方向から先読み量を更新 |
| `getLookAheadX()` / `getLookAheadY()`       | 現在の先読み量                         |
| `getMaxLookAheadX()` / `getMaxLookAheadY()` | 最大先読み量                           |
| `setMaxLookAhead(x, y)`                     | 最大先読み量を変更                     |
| `getSpeed()` / `setSpeed(speed)`            | 追従速度を取得・変更                   |
| `getThreshold()` / `setThreshold(value)`    | 移動判定しきい値を取得・変更           |
| `reset()`                                   | 現在の先読み量を0へ戻す                |

---

# 9. Camera Boundsを追加する

広いワールドでCameraが外側まで移動すると、ゲーム領域外を映してしまいます。`CameraBounds` を設定するとCameraの移動範囲を制限できます。

```java
CameraBounds bounds = new CameraBounds(0, 0, 2400, 1600);
cameraController.setBounds(bounds);
```

```text
World: 2400 × 1600

(0, 0)
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│       ┌──────── Camera Viewport: 800 × 600 ────────┐        │
│       │                                             │        │
│       │                  Player                     │        │
│       │                                             │        │
│       └─────────────────────────────────────────────┘        │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                                                   (2400, 1600)
```

ワールドが `2400 × 1600`、Viewportが `800 × 600` の場合、Camera左上座標が移動できる範囲は次のようになります。

```text
X : 0 ～ 2400 - 800 = 1600
Y : 0 ～ 1600 - 600 = 1000
```

## `CameraBounds` チートシート

| メソッド                               | 説明                         |
| -------------------------------------- | ---------------------------- |
| `constrain(camera, w, h)`              | Cameraをワールド範囲内へ補正 |
| `setBounds(x, y, w, h)`                | ワールド範囲を変更           |
| `getWorldX()` / `getWorldY()`          | ワールド開始座標を取得       |
| `getWorldWidth()` / `getWorldHeight()` | ワールドサイズを取得         |

`CameraController` へ設定している場合、ゲーム側から `constrain()` を毎回呼ぶ必要はありません。

---

# 10. Dead Zoneを追加する

`CameraDeadZone` を使うと、Playerが指定範囲内にいる間はCameraを動かさず、範囲を外れたときだけCameraを移動できます。

```java
CameraDeadZone deadZone = new CameraDeadZone(300, 200, 200, 200);
cameraController.setDeadZone(deadZone);
```

```text
Viewport
┌──────────────────────────────────┐
│                                  │
│          ┌────────────┐          │
│          │ Dead Zone  │          │
│          │     ■      │          │
│          └────────────┘          │
│                                  │
└──────────────────────────────────┘
```

## `CameraDeadZone` チートシート

| メソッド                     | 説明                                |
| ---------------------------- | ----------------------------------- |
| `apply(camera, target)`      | Dead Zoneに基づいてCamera位置を更新 |
| `setBounds(x, y, w, h)`      | Dead Zoneの位置・サイズを変更       |
| `getX()` / `getY()`          | Dead Zoneの位置を取得               |
| `getWidth()` / `getHeight()` | Dead Zoneの大きさを取得             |

> **現在の実装上の注意**
>
> `CameraDeadZone` が設定されている場合、Dead Zoneによる処理が通常の `CameraFollow` より優先されます。そのため現在の実装ではDead Zone使用中に `CameraLookAhead` は適用されません。`CameraBounds` はどちらの場合でも最後に適用されます。

## Cameraシステム全体

```text
                     CameraController
                           │
          ┌────────────────┼────────────────┐
          │                │                │
    CameraFollow     CameraLookAhead   CameraDeadZone
          │                │                │
          └────────────────┴────────────────┘
                           │
                     CameraBounds
                           │
                           ▼
                       Camera2D
```

| クラス             | 役割                             |
| ------------------ | -------------------------------- |
| `Camera2D`         | 実際のCamera座標と座標変換       |
| `CameraController` | Camera機能をまとめて制御         |
| `CameraFollow`     | ターゲットへの基本追従           |
| `CameraLookAhead`  | 移動方向を先読み                 |
| `CameraDeadZone`   | 一定範囲内ではCameraを動かさない |
| `CameraBounds`     | Cameraをワールド範囲内に制限     |

## `CameraController` チートシート

| メソッド                                     | 説明                     |
| -------------------------------------------- | ------------------------ |
| `setTarget(target)` / `getTarget()`          | 追従対象を変更・取得     |
| `setViewportSize(w, h)`                      | ビューポートサイズを変更 |
| `getViewportWidth()` / `getViewportHeight()` | ビューポートサイズを取得 |
| `setLookAhead(value)` / `getLookAhead()`     | Look Aheadを設定・取得   |
| `clearLookAhead()`                           | Look Aheadを解除         |
| `setDeadZone(value)` / `getDeadZone()`       | Dead Zoneを設定・取得    |
| `clearDeadZone()`                            | Dead Zoneを解除          |
| `setBounds(value)` / `getBounds()`           | Boundsを設定・取得       |
| `clearBounds()`                              | Boundsを解除             |

## `CameraFollow` チートシート

`CameraFollow` は `CameraController` 内部で生成され、ターゲットをViewport中央へ追従させます。

| メソッド                                     | 説明                     |
| -------------------------------------------- | ------------------------ |
| `update()`                                   | 通常追従                 |
| `update(offsetX, offsetY)`                   | オフセット付き追従       |
| `setTarget(target)` / `getTarget()`          | ターゲットを変更・取得   |
| `setViewportSize(w, h)`                      | ビューポートサイズを変更 |
| `setSmooth(boolean)` / `isSmooth()`          | スムーズ追従を設定・取得 |
| `setFollowSpeed(speed)` / `getFollowSpeed()` | 追従速度を設定・取得     |

`followSpeed` は `0.0 < speed <= 1.0` の範囲で指定します。

> 現在の `CameraController` は内部の `CameraFollow` を直接公開していないため、`CameraController` 経由で `setSmooth()` / `setFollowSpeed()` を設定するAPIはまだありません。

---

# 11. Audioを使う

`GameEngine` から `AudioManager` を取得できます。

```java
AudioManager audio = engine().getAudioManager();
```

## SEを再生する

```java
audio.playSe("src/game/assets/audio/click.wav");
```

マウスクリックに合わせてSEを鳴らす例です。

```java
if (input.isMouseJustPressed(MouseButton.LEFT)) {
    engine().getAudioManager().playSe("src/game/assets/audio/click.wav");
}
```

## BGMを再生する

```java
audio.playBgm("src/game/assets/audio/bgm.wav");
```

BGMはループ再生されます。別のBGMを `playBgm()` すると、現在のBGMを停止して新しいBGMへ切り替わります。

```java
audio.stopBgm();
```

## `AudioManager` チートシート

| メソッド        | 説明               |
| --------------- | ------------------ |
| `playSe(path)`  | SEを再生           |
| `playBgm(path)` | BGMをループ再生    |
| `stopBgm()`     | BGMを停止          |
| `close()`       | AudioManagerを終了 |

`GameEngine.stop()` 時には `AudioManager` も閉じられます。

---

# 12. Sceneを使う

ゲームが大きくなると、Title・Gameplay・Pause・Game Overのように複数の状態が必要になります。

```text
Title
  │
  ▼
Gameplay
  │
  ▼
Pause / Game Over
```

Sproutではこれらを `Scene` として管理できます。

## Sceneを作る

```java
package game.scene;

import engine.scene.Scene;

public final class TitleScene extends Scene {
    @Override
    protected void onStart() {
        System.out.println("Title Scene Started");
    }

    @Override
    protected void onUpdate() {
        // タイトル画面の更新
    }

    @Override
    protected void onEnd() {
        System.out.println("Title Scene Ended");
    }
}
```

Sceneを開始します。

```java
engine().getSceneManager().changeScene(new TitleScene());
```

Sceneのライフサイクルは `SceneManager` が管理します。

```text
changeScene()
     │
     ▼
  onStart()
     │
     ▼
 onUpdate()  ← 毎update
     │
     ▼
   onEnd()
```

## `Scene` チートシート

| メソッド      | 説明                          |
| ------------- | ----------------------------- |
| `onStart()`   | Scene開始処理                 |
| `onUpdate()`  | Scene更新処理                 |
| `onEnd()`     | Scene終了処理                 |
| `isStarted()` | Sceneが開始済みか取得         |
| `engine()`    | Sceneから `GameEngine` を取得 |

## `SceneManager` チートシート

| メソッド             | 説明                  |
| -------------------- | --------------------- |
| `changeScene(scene)` | Sceneを切り替える     |
| `endCurrentScene()`  | 現在Sceneを終了       |
| `getCurrentScene()`  | 現在Sceneを取得       |
| `hasScene()`         | Sceneが存在するか取得 |

> `Scene` 自体が `GameObject` を自動所有・自動削除する仕組みではありません。SceneごとにObjectを管理したい場合は、`onStart()` で `engine().addObject()`、`onEnd()` で `engine().removeObject()` するなど、Scene側でライフサイクルを対応させます。

---

# 13. 完成したミニゲーム

現在のサンプルゲームは、`SpriteObject` をWASD / 矢印キーで操作し、`CameraController` が追従する構成です。ワールド上にはCamera移動を確認するための `WorldObject` を配置しています。

```text
src/game/
├─ assets/
│  └─ player.png
├─ Action.java
├─ Main.java
├─ SampleGame.java
├─ SpriteObject.java
└─ WorldObject.java
```

## ワールドとCamera

```text
World: 2400 × 1600
Viewport: 800 × 600

(0, 0)
┌──────────────────────────────────────────────────────────────┐
│ ●                                                            │
│       ┌──────── Camera Viewport ────────────────┐            │
│       │                                         │            │
│       │     SpriteObject                        │      ●     │
│       │                                         │            │
│       └─────────────────────────────────────────┘            │
│                                          ●                   │
│       ●                                               ●      │
└──────────────────────────────────────────────────────────────┘
                                                   (2400, 1600)
```

`SpriteObject` が移動するとCameraが追従するため、ワールド座標に固定された `WorldObject` は画面上では反対方向へ流れて見えます。

## Action.java

```java
package game;

public enum Action {
    MOVE_LEFT,
    MOVE_UP,
    MOVE_RIGHT,
    MOVE_DOWN,
    JUMP
}
```

`JUMP` はSpaceキーへマッピングされていますが、現在の `SpriteObject` にはジャンプ処理を実装していません。

## SpriteObject.java

```java
public final class SpriteObject extends GameObject {
    private final Sprite sprite;

    private static final double SPEED = 3.0;
    private final InputContext<Action> input;

    public SpriteObject(double x, double y, InputContext<Action> input, Sprite sprite) {
        super(x, y);
        this.sprite = sprite;
        this.input = input;
    }

    @Override
    protected void onUpdate() {
        if (input.isPressed(Action.MOVE_RIGHT)) {
            move(SPEED, 0.0);
        }

        if (input.isPressed(Action.MOVE_LEFT)) {
            move(-SPEED, 0.0);
        }

        if (input.isPressed(Action.MOVE_DOWN)) {
            move(0.0, SPEED);
        }

        if (input.isPressed(Action.MOVE_UP)) {
            move(0.0, -SPEED);
        }
    }

    @Override
    protected void onDraw(Graphics2D graphics, double alpha) {
        int renderX = (int) interpolatedX(alpha);
        int renderY = (int) interpolatedY(alpha);
        graphics.drawImage(sprite.getImage(), renderX, renderY, null);
    }
}
```

画像は描画時に幅80pxへ拡縮し、高さは元画像のアスペクト比から計算します。`NEAREST_NEIGHBOR` を指定しているため、ドット絵を拡縮した際のぼやけを抑えられます。

## WorldObject.java

```java
public final class WorldObject extends GameObject {
    private static final int SIZE = 80;
    private final Color color;

    public WorldObject(double x, double y, Color color) {
        super(x, y);
        this.color = color;
    }

    @Override
    protected void onUpdate() {
    }

    @Override
    protected void onDraw(Graphics2D graphics, double alpha) {
        int renderX = (int) interpolatedX(alpha);
        int renderY = (int) interpolatedY(alpha);
        graphics.setColor(color);
        graphics.fillRect(renderX, renderY, SIZE, SIZE);
    }
}
```

`WorldObject` は移動しない固定オブジェクトです。Camera移動を視覚的に確認するために使用しています。

## SampleGame.java

```java
public final class SampleGame extends GameApplication {
    private final int TARGET_UPS = 100;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int WORLD_WIDTH = 2400;
    private static final int WORLD_HEIGHT = 1600;
    private InputContext<Action> input;

    @Override
    protected GameSettings createSettings() {
        return GameSettings.builder().size(WIDTH, HEIGHT).title("Sprout Mini World").resizable(false)
                .centerOnScreen(true).build();
    }

    @Override
    protected RendererConfig createRendererConfig() {
        return RendererConfig.builder().backgroundColor(Color.WHITE).antiAliasing(true).interpolation(true).build();
    }

    @Override
    protected void onInit() {
        configureInput();
        Sprite playerSprite = ImageLoader.loadSprite("src/game/assets/player.png").resizedByWidth(80);
        SpriteObject playerObject = new SpriteObject(200.0, 100.0, input, playerSprite);
        engine().addObject(playerObject);
        addWorldObjects();
        configureCamera(playerObject);
    }

    private void configureInput() {
        input = engine().createInputContext(Action.class);
        input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_A);
        input.addMapping(Action.MOVE_LEFT, KeyEvent.VK_LEFT);
        input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_D);
        input.addMapping(Action.MOVE_RIGHT, KeyEvent.VK_RIGHT);
        input.addMapping(Action.MOVE_UP, KeyEvent.VK_W);
        input.addMapping(Action.MOVE_UP, KeyEvent.VK_UP);
        input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_S);
        input.addMapping(Action.MOVE_DOWN, KeyEvent.VK_DOWN);
        input.addMapping(Action.JUMP, KeyEvent.VK_SPACE);
    }

    private void addWorldObjects() {
        engine().addObject(new WorldObject(100, 100, Color.RED));
        engine().addObject(new WorldObject(700, 300, Color.BLUE));
        engine().addObject(new WorldObject(1200, 700, Color.ORANGE));
        engine().addObject(new WorldObject(1800, 400, Color.MAGENTA));
        engine().addObject(new WorldObject(2100, 1200, Color.CYAN));
        engine().addObject(new WorldObject(500, 1300, Color.PINK));
    }

    private void configureCamera(SpriteObject sprite) {
        CameraController cameraController = new CameraController(engine().getCamera(), sprite, WIDTH, HEIGHT);
        cameraController.setLookAhead(new CameraLookAhead(120, 80, 0.1));
        cameraController.setBounds(new CameraBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT));
        engine().addSystem(cameraController);
    }

    @Override
    protected int targetUps() {
        return TARGET_UPS;
    }
}
```
## Main.java

```java
public class Main {
    private Main() {
    }

    public static void main(String[] args) {
        new SampleGame().run();
    }
}
```

実行開始点は `Main.main()` です。

## このミニゲームで使用している機能

| 機能 | 使用クラス | 役割 |
| --- | --- | --- |
| 起動 | `Main`, `GameApplication` | ゲームを起動 |
| 設定 | `GameSettings` | `800 × 600` の画面を設定 |
| 描画設定 | `RendererConfig` | 背景色・AA・補間 |
| 画像 | `ImageLoader` | `player.png` を読み込む |
| Sprite | `Sprite` | Player画像を保持 |
| Object | `GameObject` | SpriteObject / WorldObjectの基底 |
| 補間 | `interpolatedX/Y()` | Objectの描画位置を補間 |
| Input | `InputContext` | WASD / 矢印キー / Space |
| Camera | `Camera2D` | ワールドを見る位置 |
| Camera制御 | `CameraController` | SpriteObjectを追従 |
| Bounds | `CameraBounds` | ワールド外を映さない |
| Look Ahead | `CameraLookAhead` | 任意機能。現在は無効 |
| System | `GameSystem` | CameraControllerを自動更新 |

---



# DevLog Archive

Sproutはゲームエンジンを一度に完成させるのではなく、各システムを段階的に実装しながら成長させています。以下は現在の `devlog` フォルダに対応した開発記録です。

|    Day | 内容                                     | DevLog                                                                                                                |
| -----: | ---------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| Day 01 | ウィンドウ表示                           | [`Day01 ウィンドウ表示.md`](devlog/Day01%20ウィンドウ表示.md)                                                         |
| Day 02 | GameSettingsの作成                       | [`Day02 GameSettingsの作成.md`](devlog/Day02%20GameSettingsの作成.md)                                                 |
| Day 03 | Canvas生成                               | [`Day03 Canvas生成.md`](devlog/Day03%20Canvas生成.md)                                                                 |
| Day 04 | BufferStrategyによるダブルバッファリング | [`Day04 BufferStrategyによるダブルバッファリング .md`](devlog/Day04%20BufferStrategyによるダブルバッファリング%20.md) |
| Day 05 | GameLoop実装                             | [`Day05 GameLoop実装.md`](devlog/Day05%20GameLoop実装.md)                                                             |
| Day 06 | Lerp実装と可変FPS化                      | [`Day06 Lerp実装と可変FPS化.md`](devlog/Day06%20Lerp実装と可変FPS化.md)                                               |
| Day 07 | GameObject作成                           | [`Day07 GameObject作成.md`](devlog/Day07%20GameObject作成.md)                                                         |
| Day 08 | Keyevent取得                             | [`Day08 Keyevent取得.md`](devlog/Day08%20Keyevent取得.md)                                                             |
| Day 09 | Key押下状態を管理する                    | [`Day09 Key押下状態を管理する.md`](devlog/Day09%20Key押下状態を管理する.md)                                           |
| Day 10 | GameLoopとinputSystemの連携              | [`Day10 GameLoopとinputSystemの連携.md`](devlog/Day10%20GameLoopとinputSystemの連携.md)                               |
| Day 11 | isJustPressedとisJustReleasedの作成      | [`Day11 isJustPressedとisJustReleasedの作成.md`](devlog/Day11%20isJustPressedとisJustReleasedの作成.md)               |
| Day 12 | スナップショット方式の導入               | [`Day12 スナップショット方式の導入.md`](devlog/Day12%20スナップショット方式の導入.md)                                 |
| Day 13 | ActionベースのinputSystem                | [`Day13 ActionベースのinputSystem.md`](devlog/Day13%20ActionベースのinputSystem.md)                                   |
| Day 14 | GameEngineクラスの作成                   | [`Day14 GameEngineクラスの作成.md`](devlog/Day14%20GameEngineクラスの作成.md)                                         |
| Day 15 | GameApplication抽象クラスの作成          | [`Day15 GameApplication抽象クラスの作成.md`](devlog/Day15%20GameApplication抽象クラスの作成.md)                       |
| Day 16 | GameSettingsのBuilder化                  | [`Day16 GameSettingsのBuilder化.md`](devlog/Day16%20GameSettingsのBuilder化.md)                                       |
| Day 17 | フォーカス管理の強化                     | [`Day17 フォーカス管理の強化.md`](devlog/Day17%20フォーカス管理の強化.md)                                             |
| Day 18 | InputManagerの改善                       | [`Day18 InputManagerの改善.md`](devlog/Day18%20InputManagerの改善.md)                                                 |
| Day 19 | マウス入力                               | [`Day19 マウス入力.md`](devlog/Day19%20マウス入力.md)                                                                 |
| Day 20 | InputContextの作成                       | [`Day20 InputContextの作成.md`](devlog/Day20%20InputContextの作成.md)                                                 |
| Day 21 | 入力の無効化                             | [`Day21 入力の無効化.md`](devlog/Day21%20入力の無効化.md)                                                             |
| Day 22 | RendererConfigの作成                     | [`Day22 RendererConfigの作成.md`](devlog/Day22%20RendererConfigの作成.md)                                             |
| Day 23 | Graphics2Dへの移行                       | [`Day23 Graphics2Dへの移行.md`](devlog/Day23%20Graphics2Dへの移行.md)                                                 |
| Day 24 | Camera2Dクラスの作成                     | [`Day24 Camera2Dクラスの作成.md`](devlog/Day24%20Camera2Dクラスの作成.md)                                             |
| Day 25 | CameraFollowの作成                       | [`Day25 CameraFollowの作成.md`](devlog/Day25%20CameraFollowの作成.md)                                                 |
| Day 26 | CameraBoundsの作成                       | [`Day26 CameraBoundsの作成.md`](devlog/Day26%20CameraBoundsの作成.md)                                                 |
| Day 27 | CameraDeadZoneクラスを作成               | [`Day27 CameraDeadZoneクラスを作成.md`](devlog/Day27%20CameraDeadZoneクラスを作成.md)                                 |
| Day 28 | CameraLookAheadクラスの作成              | [`Day28 CameraLookAheadクラスの作成.md`](devlog/Day28%20CameraLookAheadクラスの作成.md)                               |
| Day 29 | CameraControllerクラスの作成             | [`Day29 CameraControllerクラスの作成.md`](devlog/Day29%20CameraControllerクラスの作成.md)                             |
| Day 30 | GameSystemの作成                         | [`Day30 GameSystemの作成.md`](devlog/Day30%20GameSystemの作成.md)                                                     |
| Day 31 | Sceneクラスの作成                        | [`Day31 Sceneクラスの作成.md`](devlog/Day31%20Sceneクラスの作成.md)                                                   |
| Day 32 | SceneManagerの作成                       | [`Day32 SceneManagerの作成.md`](devlog/Day32%20SceneManagerの作成.md)                                                 |
| Day 33 | ImageLoaderを作成する                    | [`Day33 ImageLoaderを作成する.md`](devlog/Day33%20ImageLoaderを作成する.md)                                           |
| Day 34 | Sprite画像を描画する                     | [`Day34 Sprite画像を描画する.md`](devlog/Day34%20Sprite画像を描画する.md)                                             |
| Day 35 | AudioManagerを作成する                   | [`Day35 AudioManagerを作成する.md`](devlog/Day35%20AudioManagerを作成する.md)                                         |

---

# Author

GitHub: [@tomo151215](https://github.com/tomo151215)

---


