# GameLoopを作る

## GameLoopとは
ゲームは、パラパラ漫画のように画面を何度も描き替えることで動いているように見せます。例えば、キャラクターの座標を少しずつ変えながら画面を描き直すと、キャラクターが移動しているように見えます。このように、ゲームが起動している間、

```text
ゲームの状態を更新する
↓
画面に描画する
↓
またゲームの状態を更新する
↓
また画面に描画する
```
という処理を繰り返す仕組みを **GameLoop** といいます

## GameLoopの基本

GameLoopの最も基本的な形は、次のようになります。

```java
while (running) {
    update();
    render();
}
```

`update()` は、ゲームの状態を更新する処理です。

例えば、次のような処理を行います。

```text
キャラクターの座標を更新する
敵を移動させる
弾を移動させる
当たり判定を行う
スコアを更新する
アニメーションを進める
```

一方、`render()` は、`update()` によって更新されたゲームの状態を画面に描画する処理です。

つまり、ゲームは基本的に、

```text
update() でゲームの状態を進める
render() で現在の状態を画面に描く
```

という流れを繰り返すことで動いています。

---

## 単純なGameLoopの問題点

先ほどのような単純なGameLoopには問題があります。

```java
while (running) {
    update();
    render();
}
```

この書き方では、`update()` と `render()` が可能な限り高速に繰り返されます。

そのため、1秒間に何回ループするかはPCの性能に依存します。

例えば、高性能なPCでは1秒間に何千回もループするかもしれません。

一方、低性能なPCでは1秒間に数十回しかループできないかもしれません。

もし `update()` が呼ばれるたびにキャラクターを1ピクセル動かすような処理を書いていた場合、高性能なPCではキャラクターが非常に速く動き、低性能なPCではキャラクターが遅く動いてしまいます。

つまり、単純に `update()` と `render()` を繰り返すだけでは、PCの性能によってゲーム内の時間の進み方が変わってしまう可能性があります。

これを防ぐためには、`update()` を実行する間隔を現実時間を基準に制御する必要があります。

---

## 固定時間ステップとは

ゲーム内の状態更新を安定させるために、`update()` を決まった時間間隔で実行する方式があります。

この方式を **固定時間ステップ** といいます。

例えば、`update()` を1秒間に60回実行したい場合、1回の `update()` の間隔は約16.67ミリ秒になります。

```text
1秒 ÷ 60回 = 約16.67ミリ秒
```

このように、

```text
一定時間たまったら update() を1回実行する
```

という仕組みにすると、PCの性能に関係なく、ゲーム内の状態更新を一定のペースで進めやすくなります。

ただし、固定時間ステップとは「どんなに重い処理でも完全に同じ速度で動く」という意味ではありません。

PCの性能が低すぎる場合や、一時的に処理が重くなった場合は、現実時間に追いつくために `update()` が連続で複数回実行されることがあります。

つまり、固定時間ステップとは、

```text
1回の update() で進めるゲーム内時間を一定にする方式
```

です。

---

## 今回作るGameLoopの種類

今回作るGameLoopは、**固定時間ステップ型かつ更新同期描画型** のGameLoopです。

具体的には、次のような構造になります。

```text
update は固定時間間隔で実行する
render は update が1回以上実行されたときだけ呼び出す
```

つまり、ゲームの状態が更新された場合だけ、画面を描画し直します。

この方式では、`update()` が発生していないループでは `render()` を呼び出しません。

そのため、無駄な描画をある程度抑えることができます。

---

## GameLoopはスレッドで動かす

JavaのSwingでは、ボタン操作、キー入力、マウス入力、画面の再描画など、多くのGUI処理が **EDT** という専用スレッドで処理されます。

EDTは、`Event Dispatch Thread` の略です。

もしEDT上で次のような無限ループを直接実行してしまうと、EDTがGameLoopに占有されてしまいます。

```java
while (running) {
    update();
    render();
}
```

その結果、キー入力やマウス入力、通常のSwingの再描画処理などが詰まってしまい、画面が固まったように見える可能性があります。

そのため、GameLoopはEDTとは別のスレッドで動かします。

Javaでは、別スレッドで実行したい処理を書く方法の1つとして、`Runnable` インターフェースがあります。

---

## GameLoopクラスを作る

GameLoopクラスを作ります。

GameLoopは別スレッドで動かしたいので、`Runnable` インターフェースを実装します。

```java
public class GameLoop implements Runnable {
}
```

`Runnable` を実装すると、スレッドで実行される処理を `run()` メソッドに書くことができます。

---

## run()メソッドを実装する

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

        boolean updated = false; // updateしたかどうかのフラグ

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

このコードでは、現実時間の経過を `System.nanoTime()` で測定し、その経過時間を `accumulator` にためています。

そして、`accumulator` に1回分の更新時間がたまった場合にだけ `update()` を実行します。

さらに、`update()` が1回以上実行された場合だけ `render()` を呼び出します。

---

## nsPerUpdate

```java
final double nsPerUpdate = 1_000_000_000.0 / targetUps;
```

`nsPerUpdate` は、`update()` を1回実行するために必要な時間間隔です。

`System.nanoTime()` はナノ秒単位で時間を扱うため、ここでもナノ秒単位で計算します。

1秒は、

```text
1,000,000,000ナノ秒
```

です。

例えば、`targetUps` が `60` の場合は、次のようになります。

```text
1,000,000,000 ÷ 60 = 約16,666,666.67ナノ秒
```

つまり、約16.67ミリ秒ごとに `update()` を1回実行するという意味です。

`targetUps` は、`Updates Per Second` の略として考えると分かりやすいです。

つまり、

```text
1秒間に何回 update() を実行するか
```

を表す値です。

---

## lastTime

```java
long lastTime = System.nanoTime();
```

`lastTime` は、前回ループを実行した時刻を保存する変数です。

ゲームループでは、前回のループから今回のループまでにどれくらい時間が経過したかを知る必要があります。

そのため、前回の時刻を `lastTime` に保存しておきます。

---

## accumulator

```java
double accumulator = 0.0;
```

`accumulator` は、まだ `update()` に使われていない経過時間をためておく変数です。

日本語では「蓄積するもの」や「時間をためておく箱」と考えると分かりやすいです。

ゲームループは、毎回ちょうど `nsPerUpdate` の間隔で実行されるとは限りません。

例えば、`targetUps` が60の場合、1回の `update()` 間隔は約16.67ミリ秒です。

しかし、実際のループでは、次のように経過時間にばらつきがあります。

```text
前回から5ミリ秒しか経っていない
前回から10ミリ秒しか経っていない
前回から20ミリ秒経っている
```

そこで、経過時間を一度 `accumulator` にためます。

そして、`accumulator` に1回分の更新時間がたまったら、`update()` を実行します。

---

## 現在時刻と経過時間を求める

```java
long now = System.nanoTime();
long elapsed = now - lastTime;
lastTime = now;
```

`now` には、現在時刻が入ります。

```java
long now = System.nanoTime();
```

次に、現在時刻 `now` から前回の時刻 `lastTime` を引くことで、前回のループから今回のループまでに経過した時間を求めます。

```java
long elapsed = now - lastTime;
```

そして、今回の時刻を次回の比較に使うために、`lastTime` を更新します。

```java
lastTime = now;
```

この3行によって、

```text
前回のループから今回のループまでに何ナノ秒経過したか
```

を求めています。

---

## 経過時間をaccumulatorにためる

```java
accumulator += elapsed;
```

求めた経過時間 `elapsed` を `accumulator` に加算します。

例えば、1回目のループで5ミリ秒、2回目のループで6ミリ秒、3回目のループで7ミリ秒経過したとします。

すると、`accumulator` には次のように時間がたまります。

```text
1回目: 5ミリ秒
2回目: 11ミリ秒
3回目: 18ミリ秒
```

`targetUps` が60の場合、1回の更新間隔は約16.67ミリ秒です。

そのため、3回目の時点で `accumulator` が1回分の更新時間を超え、`update()` が実行されます。

---

## updatedフラグ

```java
boolean updated = false;
```

`updated` は、このループ内で `update()` が実行されたかどうかを記録するためのフラグです。

最初はまだ `update()` を実行していないので、`false` にしておきます。

この後、実際に `update()` が1回でも実行された場合は、`updated` を `true` にします。

---

## updateを固定間隔で実行する

```java
while (accumulator >= nsPerUpdate) {
    update();
    accumulator -= nsPerUpdate;
    updated = true;
}
```

ここが固定時間ステップ型GameLoopの中心です。

`accumulator` に1回分の更新時間である `nsPerUpdate` 以上の時間がたまっていれば、`update()` を実行します。

```java
update();
```

`update()` では、キャラクターの移動、敵の行動、当たり判定、スコアの更新など、ゲーム内の状態を進める処理を書きます。

`update()` を1回実行したら、`accumulator` から1回分の更新時間を引きます。

```java
accumulator -= nsPerUpdate;
```

これにより、「たまっていた時間のうち、1回分の更新に使った時間」を消費したことになります。

そして、`update()` が実行されたことを記録するために、`updated` を `true` にします。

```java
updated = true;
```

---

## なぜifではなくwhileを使うのか

ここで重要なのは、`if` ではなく `while` を使っていることです。

もし次のように `if` を使った場合、1回のループで `update()` は最大1回しか実行されません。

```java
if (accumulator >= nsPerUpdate) {
    update();
    accumulator -= nsPerUpdate;
}
```

しかし、処理が一時的に重くなって、3回分の更新時間がたまっていた場合、本来なら `update()` を3回実行する必要があります。

そこで、`while` を使います。

```java
while (accumulator >= nsPerUpdate) {
    update();
    accumulator -= nsPerUpdate;
}
```

これにより、`accumulator` にたまっている時間の分だけ、必要な回数 `update()` を実行できます。

例えば、1回の更新時間が16.67ミリ秒で、`accumulator` に50ミリ秒たまっていた場合、約3回分の更新時間がたまっていることになります。

その場合、この `while` 文によって `update()` が3回実行されます。

これにより、処理落ちが発生しても、ゲーム内時間が現実時間から大きくずれにくくなります。

---

## updateされたときだけrenderする

```java
if (updated) {
    render();
} else {
    sleep();
}
```

このGameLoopでは、`update()` が1回以上実行されたときだけ `render()` を呼び出します。

`updated` が `true` の場合は、このループ内で `update()` が行われています。

つまり、ゲームの状態が変化しています。

そのため、画面を描画し直します。

```java
render();
```

一方、`updated` が `false` の場合は、まだ1回分の更新時間がたまっていないということです。

つまり、ゲームの状態は前回の描画時から変わっていません。

そのため、このタイミングで描画しても、基本的には同じ画面を描くだけになります。

そこで、無駄にCPUを使い続けないように `sleep()` を呼び出します。

```java
sleep();
```

このように、`update()` が発生したときだけ `render()` する方式を、ここでは **更新同期描画型** と呼びます。

---

## このGameLoopの処理の流れ

このGameLoopの流れをまとめると、次のようになります。

```text
1. 現在時刻を取得する
2. 前回のループからの経過時間を求める
3. 経過時間を accumulator にためる
4. accumulator に1回分の更新時間がたまっていれば update() する
5. 必要なら update() を複数回実行する
6. update() が1回以上実行された場合だけ render() する
7. update() するほど時間がたまっていなければ sleep() する
8. running が true の間、これを繰り返す
```

---

## このGameLoopの特徴

このGameLoopには、次のような特徴があります。

```text
update() の間隔は固定される
render() は update() が行われたときだけ実行される
ゲーム内時間がPC性能に依存しにくい
無駄な描画を抑えやすい
処理が重い場合は update() を複数回実行して追いつこうとする
```

特に重要なのは、`update()` の間隔が固定されていることです。

キャラクターの移動や当たり判定を固定間隔で処理できるため、ゲームの挙動が安定しやすくなります。

---

## 可変時間ステップ型との違い

このGameLoopは、可変時間ステップ型ではありません。

可変時間ステップ型では、前回からの経過時間を `deltaTime` として `update()` に渡し、その時間に応じて移動量などを変えます。

例えば、可変時間ステップ型では次のように書きます。

```java
double deltaTime = elapsed / 1_000_000_000.0;
update(deltaTime);
render();
```

この場合、1回の `update()` で進む量が、経過時間によって変化します。

一方、今回のGameLoopでは、`update()` に経過時間を渡していません。

```java
update();
```

代わりに、次のようにして、一定時間がたまるたびに `update()` を1回実行しています。

```java
while (accumulator >= nsPerUpdate) {
    update();
    accumulator -= nsPerUpdate;
}
```

つまり、今回の方式は、

```text
1回の update() で進めるゲーム内時間は常に一定
```

という考え方です。

そのため、このGameLoopは **固定時間ステップ型** です。

---

## renderを毎回呼ぶ固定時間ステップ型との違い

固定時間ステップ型には、`render()` を毎ループ呼ぶタイプもあります。

例えば、次のような形です。

```java
while (running) {
    long now = System.nanoTime();
    long elapsed = now - lastTime;
    lastTime = now;

    accumulator += elapsed;

    while (accumulator >= nsPerUpdate) {
        update();
        accumulator -= nsPerUpdate;
    }

    render();
}
```

このタイプでは、`update()` は固定間隔で行い、`render()` はループのたびに呼び出します。

つまり、

```text
update() は固定時間ステップ
render() は可能な範囲で繰り返す
```

という形です。

一方、今回のGameLoopでは、次のようになっています。

```java
if (updated) {
    render();
} else {
    sleep();
}
```

そのため、`update()` が発生しなかったループでは描画しません。

つまり、今回のGameLoopは、

```text
update() は固定時間ステップ
render() は update() に同期する
```

という構造です。

したがって、今回のGameLoopは **固定時間ステップ型かつ更新同期描画型** といえます。

---

## 注意点1：処理が重すぎる場合

このGameLoopでは、処理が重くなった場合、たまった時間に追いつくために `update()` が連続で実行されます。

これはゲーム内時間のずれを小さくするためには有効ですが、処理が重すぎる場合には注意が必要です。

なぜなら、次のような悪循環が起こる可能性があるからです。

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

このような状態は、一般に **spiral of death** と呼ばれることがあります。

対策としては、1回のループで実行する `update()` の最大回数を制限する方法があります。

例えば、次のようにします。

```java
int updateCount = 0;
int maxUpdatesPerFrame = 5;

while (accumulator >= nsPerUpdate && updateCount < maxUpdatesPerFrame) {
    update();
    accumulator -= nsPerUpdate;
    updated = true;
    updateCount++;
}

if (updateCount == maxUpdatesPerFrame) {
    accumulator = 0.0;
}
```

これにより、処理が重くなった場合でも、1回のループで無制限に `update()` が実行されることを防げます。

ただし、`accumulator = 0.0;` とすると、たまっていた時間を捨てることになります。

そのため、ゲーム内時間が一時的に現実時間から遅れる可能性があります。

しかし、無限に `update()` が連続実行されてゲームが固まるよりは安全です。

---

## 注意点2：Swingでの描画

Swingでゲームを作る場合、描画方法によって注意点が変わります。

`Canvas` と `BufferStrategy` を使って自前で描画する場合は、GameLoopのスレッドから描画処理を呼び出す構成にしやすいです。

一方、`JPanel` の `paintComponent()` を使う場合は、Swingの描画はEDT上で行われるため、GameLoopのスレッドから直接 `paintComponent()` を呼び出すのではなく、`repaint()` を呼び出して描画を依頼する形にします。

つまり、Swingでは次の点に注意します。

```text
EDTをGameLoopで占有しない
Swingコンポーネントを別スレッドから直接操作しすぎない
Canvas + BufferStrategy ならGameLoopから能動的に描画しやすい
JPanel + paintComponent なら repaint() を使って描画を依頼する
```

---

## 注意点3：runningの扱い

`running` は、GameLoopを続けるか止めるかを管理する変数です。

```java
while (running) {
    ...
}
```

別スレッドから `running` の値を変更してGameLoopを止める場合は、スレッド間で値の変更が正しく見えるようにする必要があります。

簡単な方法としては、`running` を `volatile` にします。

```java
private volatile boolean running;
```

`volatile` を付けることで、あるスレッドで変更した `running` の値が、GameLoop側のスレッドからも見えやすくなります。

---

## まとめ

今回のGameLoopは、**固定時間ステップ型かつ更新同期描画型** のGameLoopです。

`System.nanoTime()` を使って現実時間の経過を測定し、その経過時間を `accumulator` にためます。

そして、`accumulator` に1回分の更新時間である `nsPerUpdate` がたまった場合にだけ `update()` を実行します。

この仕組みにより、ゲーム内の状態更新はPCの性能に依存しにくくなります。

また、このコードでは、`update()` が1回以上実行された場合にだけ `render()` を呼び出します。

そのため、描画は更新処理に同期して行われます。

このGameLoopの構造を簡単に表すと、次のようになります。

```text
現実時間を測る
↓
経過時間を accumulator にためる
↓
一定時間たまったら update() する
↓
update() した場合だけ render() する
↓
まだ update() する時間でなければ sleep() する
```

この方式を使うことで、ゲームの状態更新を安定させつつ、不要な描画やCPU使用をある程度抑えることができます。
