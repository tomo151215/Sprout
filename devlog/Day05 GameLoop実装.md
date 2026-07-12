# Day05: GameLoopの作成

## GameLoopとは
ゲームは、パラパラ漫画のように画面を何度も描き替えることで動いているように見せます。例えば、キャラクターの座標を少しずつ変えながら画面を描き直すと、キャラクターが移動しているように見えます。このように、ゲームが起動している間、更新→描画→更新→描画→ という処理を繰り返す仕組みを **GameLoop** といいます。

## GameLoopの基本
GameLoopの最も基本的な形は、次のようになります。
```java
while (running) {
    update();
    render();
}
```
`update()` は、ゲームの状態を更新する処理です。具体的には座標更新、当たり判定、スコア更新などを行います。一方、`render()` は、`update()` によって更新されたゲームの状態を画面に描画する処理です。

### 単純なGameLoopの問題点

先ほどのような単純なGameLoopには問題があります。この書き方では、`update()` と `render()` が可能な限り高速に繰り返されます。そのため、1秒間に何回ループするかはPCの性能に依存します。もし `update()` が呼ばれるたびにキャラクターを1ピクセル動かすような処理を書いていた場合、高性能なPCではキャラクターが非常に速く動き、低性能なPCではキャラクターが遅く動いてしまいます。

つまり、単純に `update()` と `render()` を繰り返すだけでは、**PCの性能によってゲーム内の時間の進み方が変わってしまう**可能性があります。これを防ぐためには、`update()` を実行する間隔を現実時間を基準に制御する必要があります。

### 固定時間ステップとは
ゲーム内の状態更新をどの環境でも安定させるために、`update()` を決まった時間間隔で実行する方式があります。この方式を **固定時間ステップ** といいます。

例えば、`update()` を1秒間に60回実行したい場合、1回の `update()` の間隔は約16.67ミリ秒になります。
```text
1000ミリ秒 ÷ 60回 = 約16.67ミリ秒
```
このように、「一定時間たまったら update() を1回実行する」という仕組みにすると、PCの性能に関係なく、ゲーム内の状態更新を一定のペースで進めやすくなります。
ただし、固定時間ステップとは「どんなに重い処理でも完全に同じ速度で動く」という意味ではありません。PCの性能が低すぎる場合や、一時的に処理が重くなった場合は、現実時間に追いつくために `update()` が連続で複数回実行されることがあります。つまり、固定時間ステップとは、1回の update() で進めるゲーム内時間を一定にする方式です。

### 今回作るGameLoopの種類

今回作るGameLoopは、**固定時間ステップ型かつ更新同期描画型** のGameLoopです。具体的には、次のような構造になります。
```text
update は固定時間間隔で実行する
render は update が1回以上実行されたときだけ呼び出す
```
つまり、ゲームの状態が更新された場合だけ、画面を描画し直します。この方式では、`update()` が発生していないループでは `render()` を呼び出しません。この場合、規定時間が経過してないことから`update()`を実行していないので、実行を一時停止して時間稼ぎをすることで無駄な描画をある程度抑えることができます。

### GameLoopはスレッドで動かす
JavaのSwingでは、ボタン操作、キー入力、マウス入力、画面の再描画など、多くのGUI処理が **EDT** という専用スレッドで処理されます。EDTは、`Event Dispatch Thread` の略です。
もしEDT上でループを直接実行してしまうと、EDTがGameLoopに占有されてしまいます。
その結果、キー入力やマウス入力、通常のSwingの再描画処理などが詰まってしまい、画面が固まったように見える可能性があります。そのため、GameLoopはEDTとは別のスレッドで動かします。

Javaでは、別スレッドで実行したい処理を書く方法の1つとして、`Runnable` インターフェースの実装という方法があります。以下のようにGameLoopクラスを作成し`run()`メソッドを実装してスレッド内で実行する処理を記述します。
```java
public class GameLoop implements Runnable{
    @Override
    public void run(){}
}
```
## GameLoopの実装
GameLoopクラスを作ります。
GameLoopは別スレッドで動かしたいので、`Runnable` インターフェースを実装し、スレッドで実行される処理を `run()` メソッドに書きます。

### run()メソッドを実装する
`run()` メソッドをオーバーライドして、ゲームループの本体を書きます。
```java
@Override
public void run() {
    final double nsPerUpdate = 1_000_000_000.0 / targetUps;
    long lastTime = System.nanoTime();
    double accumulator = 0.0;

    while (running) {
        long now = System.nanoTime();
        long elapsed = now - lastTime;
        lastTime = now;

        accumulator += elapsed;

        boolean updated = false; 

        while (accumulator >= nsPerUpdate) {
            update();
            accumulator -= nsPerUpdate;
            updated = true;
        }

        if (updated) {
            render();
        } else {
            sleep();
        }
    }
}
```
このコードでは、現実時間の経過を `System.nanoTime()` で測定し、その経過時間を `accumulator` にためています。そして、`accumulator` に1回分の更新時間がたまった場合にだけ `update()` を実行します。さらに、`update()` が1回以上実行された場合だけ `render()` を呼び出します。

#### nsPerUpdate
`nsPerUpdate`(nano second per update) は、`update()` を1回実行するために必要な時間間隔です。１秒当たりtargetUps回 Updateされるので、１回のUpdate当たり`1 / targetUps`秒の間隔が必要と分かります。1秒は`1000000000`ミリ秒であることに注意してください。`System.nanoTime()` はナノ秒単位で時間を扱うため、ここでもナノ秒単位で計算します。
```java
final double nsPerUpdate = 1_000_000_000.0 / targetUps;
```
#### lastTime
```java
long lastTime = System.nanoTime();
```
`lastTime` は、前回ループを実行した時刻を保存する変数です。ゲームループでは、前回のループから今回のループまでにどれくらい時間が経過したかを知る必要があります。そのため、前回の時刻を `lastTime` に保存しておきます。
#### accumulator
`accumulator` は、まだ `update()` で消費されていない経過時間を蓄積しておく変数です。
```java
double accumulator = 0.0;
```
ゲームループは、毎回ちょうど `nsPerUpdate` の間隔で実行されるとは限りません。
例えば、`targetUps` が60の場合、1回の `update()` 間隔は約16.67ミリ秒です。しかし、実際のループでは、次のように経過時間にばらつきがあります。
```text
前回から5ミリ秒しか経っていない
前回から10ミリ秒しか経っていない
前回から20ミリ秒経っている
```
そこで、経過時間を一度 `accumulator` にためます。そして、`accumulator` に1回分の更新時間がたまったら、`update()` を実行します。
#### 現在時刻と経過時間を求める
while文の中でゲームループを実行していきます。まず、現在時刻と前回ループ開始時刻の差から「前回のループから今回のループまでに何ナノ秒経過したか」という経過時間を計測します。
```java
long now = System.nanoTime();
long elapsed = now - lastTime;
lastTime = now;
```
`now` には、現在時刻が入ります。次に、現在時刻 `now` から前回の時刻 `lastTime` を引くことで、前回のループから今回のループまでに経過した時間を求めます。そして、今回の現在時刻を次回の比較に使うために、`lastTime` を更新します。

#### 経過時間をaccumulatorにためる
求めた経過時間 `elapsed` を `accumulator` に加算します。`accumulator` が1回分の更新時間を超えると、`update()` が実行されます。
```java
accumulator += elapsed;
```
#### updatedフラグ
`updated` は、このループ内で `update()`が実行されたかどうかを記録するためのフラグです。最初はまだ `update()` を実行していないので、`false` にしておきます。
```java
boolean updated = false;
```
#### updateを固定間隔で実行する
ここが固定時間ステップ型GameLoopの中心です。`accumulator` に1回分の更新時間である `nsPerUpdate` 以上の時間がたまっていれば、`update()` を実行し続けます。ただし、`update()` を1回実行したら、`accumulator` から1回分の更新時間を引きます。そして、`update()` が実行されたことを記録するために、`updated` を `true` にします。
```java
while (accumulator >= nsPerUpdate) {
    update();
    accumulator -= nsPerUpdate;
    updated = true;
}
```
#### なぜifではなくwhileを使うのか
ここで重要なのは、`if` ではなく `while` を使っていることです。もし次のように `if` を使った場合、1回のループで `update()` は最大1回しか実行されません。
```java
if (accumulator >= nsPerUpdate) {
    update();
    accumulator -= nsPerUpdate;
}
```
しかし、処理が一時的に重くなり、ゲームループが回るまでの間に3回分の更新時間が経過してしまうことがあります。その場合、accumulator には3回分の更新時間が蓄積されています。本来なら、その3回分のゲーム更新を行わなければゲーム内の時間が現実の時間より遅れてしまいます。そこで、`while` を使います。
```java
while (accumulator >= nsPerUpdate) {
    update();
    accumulator -= nsPerUpdate;
    updated = true;
}
```
これにより、`accumulator` にたまっている時間の分だけ、必要な回数 `update()` を実行できます。

#### updateされたときだけrenderする

```java
if (updated) {
    render();
} else {
    sleep();
}
```
このGameLoopでは、`update()` が1回以上実行されたときだけ `render()` を呼び出します。`updated` が `true` の場合は、このループ内で `update()` が行われています。つまり、ゲームの状態が変化しているので、画面を描画し直します。
一方、`updated` が `false` の場合は、まだ1回分の更新時間がたまっていないということです。つまり、ゲームの状態は前回の描画時から変わっていません。そのため、このタイミングで描画しても、基本的には同じ画面を描くだけになります。そこで、無駄にCPUを使い続けないように `sleep()` を呼び出します。
```java
    private void sleep() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
```
このように、`update()` が発生したときだけ `render()` する方式を、ここでは **更新同期描画型** と呼びます。したがって、今回のGameLoopは **固定時間ステップ型かつ更新同期描画型** といえます。

## `start()`メソッドと`stop()`メソッド
スレッドをスタートする、ストップするためのメソッドを作成します。
### `start()`メソッド
```java
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        th = new Thread(this);
        th.start();
    }
```
`start()`メソッドを呼び出すと、ゲームループを実行中であることを表すフラグ`running`を`true`に設定し、`Thread`オブジェクトを生成して`Thread.start()`を呼び出します。`Thread.start()`が呼ばれると新しいスレッドが開始され、そのスレッド上で`run()`メソッドが実行されます。また、ゲームループがすでに開始されている場合に重複して起動しないよう、最初に`if (running) { return; }`で`running`の状態を確認しています。

また、`start()`メソッドは複数のスレッドから同時に呼び出される可能性があるため、`synchronized`を付けない場合は以下のような不具合が発生する可能性があります。2つのスレッドA、Bがほぼ同時に`start()`を実行するとします。例えば、次のような実行順序になることがあります。
```text
Thread A
if (running)   // false

（OSがThread Bへ切り替える）

Thread B
if (running)   // Thread Aもまだrunning=trueを書き込んでいないためfalseになる
running = true
Thread.start()

（Thread Aへ戻る）

Thread A
running = true
Thread.start()
```
このように、両方のスレッドが`running`を`false`と判定してしまうと、ゲームループスレッドが2本起動する可能性があります。その結果、`update()`が1フレーム中に2回実行されるなど、ゲームの進行速度が速くなる、CPU使用率が上昇するなどの意図しない動作を引き起こすおそれがあります。

このような競合を防ぐため、`start()`メソッドには`synchronized`修飾子を付けています。`synchronized`を付けると、同じオブジェクトの`start()`メソッドをあるスレッドが実行中は、他のスレッドはその処理が終わるまで待機します。そのため、`if (running)`の判定から`running = true`の設定までが排他的に実行され、ゲームループが重複して起動することを防げます。

### `stop()`メソッド

ゲームループを安全に停止し、ゲームループスレッドの終了を待つためのメソッドです。

```java
public synchronized void stop() {
    running = false;

    if (th != null && Thread.currentThread() != th) {
        try {
            th.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```
まず、`running = false;`によってゲームループの終了を指示します。`while(running){}`の、`running`が`false`になると次回のループ判定で`while`を抜け、`run()`メソッドが終了します。

次に、`th != null`でゲームループスレッドが生成済みであることを確認しています。`th`が`null`の状態で`join()`を呼び出すと`NullPointerException`が発生するためです。また、`Thread.currentThread() != th`では、現在`stop()`を実行しているスレッドがゲームループスレッド自身ではないことを確認しています。なぜなら、`th.join()`はゲームループスレッドが完全に終了するまで待機するメソッドであり、ゲームループスレッド自身の中でこれを呼び出すと永遠と待機し続けて終了できないデッドロックが発生するからです。

`Thread.currentThread().interrupt()`は、Javaのスレッドで割り込み（interrupt）の情報を失わないようにするためのコードです。`th.join()`実行中に`InterruptedException`を検知すると、Javaは割り込みフラグを自動的にクリアします。ゆえに、`Thread.currentThread().interrupt()`で再び割り込みフラグを立たせることで、後続の処理に「割り込みフラグが立った」という事実を残すことができます。Javaでは慣習的な手法です。

なお、`start()`と同様に`stop()`にも`synchronized`を付けることで、複数のスレッドから同時に呼び出された場合でも競合が発生しないようにしています。


## 注意：処理が重すぎる場合
このGameLoopでは、処理が重くなった場合、たまった時間に追いつくために `update()` が連続で実行されます。これはゲーム内時間のずれを小さくするためには有効ですが、処理が重すぎる場合には注意が必要です。なぜなら、次のような悪循環が起こる可能性があるからです。
```text
処理が重い
↓
accumulator に時間がたまる
↓
update() を何回も実行する
↓
さらに処理が重くなる
↓
もっと時間がたまる
```
このような状態は、一般に **spiral of death** と呼ばれることがあります。対策としては、1回のループで実行する `update()` の最大回数を制限する方法があります。
```java
int updateCount = 0;
int maxUpdatesPerFrame = 5;  // 1フレームで実行するupdate()の上限回数

while (accumulator >= nsPerUpdate && updateCount < maxUpdatesPerFrame) {
    update();
    accumulator -= nsPerUpdate;
    updated = true;
    updateCount++;
}

if (updateCount == maxUpdatesPerFrame) {
    // update()が追いつかない状態（Spiral of Death）を防ぐため、
    // 残りの更新時間を破棄して次のフレームから再開する
    accumulator = 0.0;
}
```
これにより、処理が重くなった場合でも、1回のループで無制限に `update()` が実行されることを防げます。ただし、`accumulator = 0.0;` とすると、たまっていた時間を捨てることになります。そのため、ゲーム内時間が一時的に現実時間から遅れる可能性があります。しかし、無限に `update()` が連続実行されてゲームが固まるよりは安全です。

## 注意：`running` の扱い
`running` は、GameLoopを続けるか止めるかを管理するフラグです。
```java
while (running) {
    ...
}
```
GameLoopは別スレッドで実行されることが多く、`running` の値は別のスレッドから変更される場合があります。Javaでは、各スレッドが変数の値をCPUのキャッシュやレジスタに保持することがあるため、あるスレッドで変更した値が、他のスレッドからすぐに見えない場合があります。
そのため、`running` に `volatile` を付けて、他のスレッドから最新の値が必ず見える（可視性）ことを保証しなければなりません。
```java
private volatile boolean running;
```
`volatile` を付けることで、あるスレッドで変更した `running` の値が他のスレッドからも確実に見えるようになり、GameLoopを安全に停止できます。

