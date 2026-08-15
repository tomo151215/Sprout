# Day35: AudioManagerを作成する
ここでは、BGMやSEを再生するための土台として、`AudioManager`を作成します。このクラスは以下のことができるようにします。
```text
音声ファイルを読み込む 
SEを再生する 
BGMをループ再生する 
BGMを停止する 
使い終わった音声を閉じる
```

## BGMとSEの違い
ゲーム音声は、大きく分けるとBGMとSEがあります。
```text
BGM(Background Music)
 背景音楽 
 長めの音楽 
 ループ再生することが多い 

SE(Sound Effect) 
 効果音 
 短い音 
 操作やイベントに合わせて鳴らす
```
たとえば、タイトル画面で流れる音楽はBGMです。一方、Enterキーを押したときの決定音はSEです。

## Javaで音を鳴らす方法
Java標準機能で音を鳴らす場合、`javax.sound.sampled`パッケージを使えます。代表的に使うのは`Clip`です。`Clip`は、短い音声データをメモリに読み込んで再生するためのクラスです。ゲームのSEや短めのBGMを扱うには分かりやすいです。ただし、`wav`形式に対応しているので、`mp3`形式には対応していないことに注意。今回は`wav`形式として作ります。

## AudioManagerの実装
`AudioManager`の必要最低限の機能に絞って開発します。機能は次の４つに絞ります。
```text
SEを再生する 
BGMをループ再生する 
BGMを停止する 
現在のBGMを閉じる
```

### Clipを読み込むメソッド
音声ファイルから`Clip`を作る処理を用意します。このメソッドは、指定したパスの音声ファイルを読み込み、再生可能な`Clip`として返します。
```java
private Clip loadClip(String path) {
    if (path == null || path.isBlank()) {
        throw new IllegalArgumentException("path must not be null or blank.");
    }
    try (AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path))) {
        Clip clip = AudioSystem.getClip();
        clip.open(stream);
        return clip;
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
        throw new IllegalArgumentException("Failed to load audio: " + path, e);
    }
}
```
#### 処理の主な流れ
1. 入力値の検証
    `path == null || path.isBlank()`で、引数のパスが空や空白でないかをチェックしています。もし無効な場合は`IllegalArgumentException`（引数が不適切であることを示す例外）を投げます。

2. 音声ファイルの読み込みとClipのリソース確保
    - `try-with-resources`構文`try (AudioInputStream ...)`を使用し、音声データの入力ストリーム `AudioInputStream`を作成します。処理が終わるとストリームは自動的に閉じられます。
    - `AudioSystem.getClip()`で再生用の`Clip`インスタンスを取得し、`clip.open(stream)`で音声データをメモリ上にロードします。

3. Clipの返却
    準備ができた`Clip`オブジェクトを戻り値として返します。呼び出し側はこのオブジェクトを使って `clip.start()`などの再生処理を行います。

4. 例外処理
    対応していないフォーマット（`UnsupportedAudioFileException`）、ファイル読み込みエラー（`IOException`）、オーディオラインが利用できない（`LineUnavailableException`）などのエラーが発生した場合、それを捕捉してオリジナルの例外メッセージを含めた`IllegalArgumentException`にラップして投げ直します。

### playSe()メソッド
`playSe()`は、音声SEを読み込んで最初から再生します。
```java
public void playSe(String path){
    loadClip(path).start();
}
```
`Clip`は使い終わったら閉じるべきです。SEは短い音なので、再生が終わったら自動で閉じるようにします。`Clip`には、音声の状態が変わったときに通知を受け取る仕組みがあります。`LineListener`です。匿名クラスを使って以下の様に書けます。
```java
clip.addLineListener(new LineListener() {
    @Override
    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) {
            clip.close();
        }
    }
});
```
ラムダ式で書けるので以下の様に簡略化します。
```java
clip.addLineListener(event -> {
    if (event.getType() == LineEvent.Type.STOP) {
        clip.close();
    }
});
```
さらに、音声を最初から再生するために、`clip.setFramePosition(0);`で再生位置（シークバー）を一番最初（0フレーム目）に巻き戻します。その後`clip.start();`で再生を開始します。よって`playSe()`メソッドは以下の様になります。
```java
public void playSe(String path) {
    Clip clip = loadClip(path);
    clip.addLineListener(event -> {
        if (event.getType() == LineEvent.Type.STOP) {
            clip.close();
        }
    });
    clip.setFramePosition(0);
    clip.start();
}
```

### BGMを再生する
BGMは、長めの音楽です。タイトル画面やプレイ画面で流します。BGMはループ再生したいので、`loop()`を使います。
```java
bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
```
BGM用には、現在再生中の`Clip`をフィールドで持ちます。
```java
private Clip bgmClip;
```
これにより、あとで停止できます。

#### playBgm()メソッド
BGMを再生する`playBgm()`を作ります。
```java
public void playBgm(String path) {
    stopBgm();
    bgmClip = loadClip(path);
    bgmClip.setFramePosition(0);
    bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
    bgmClip.start();
}
```
最初に`stopBgm()`を呼びます。これはすでにBGMが流れている場合は止めてから、新しいBGMを流せるからです。

#### stopBgm()メソッド
現在流れているBGMを止めるために、`stopBgm()`を作ります。
```java
public void stopBgm() {
    if (bgmClip == null) {
        return;
    }
    bgmClip.stop();
    bgmClip.close();
    bgmClip = null;
}
```
`bgmClip`が`null`の場合は、まだBGMがないので何もしません。BGMがある場合は、停止して閉じます。

### close()メソッド
ゲーム終了時に、音声リソースを閉じられるように`close()`を作っておきます。
```java
public void close() { 
    stopBgm(); 
}
```
`AudioManager`が保持する長期的な`Clip`はBGMだけです。そのため、`close()`では`stopBgm()`を呼ぶだけで十分です。

## GameEngineへAudioManagerの導入
`AudioManager`は、ゲーム全体で使いたい機能です。そのため、`GameEngine`に持たせます。
まず、フィールドを追加します。
```java
private final AudioManager audioManager;
```
コンストラクタで生成します。
```java
this.audioManager = new AudioManager();
```
getterを追加します。
```java
public AudioManager getAudioManager() { 
    return audioManager; 
}
```
さらに`GameEngine.stop()`で`audioManager.close()`を呼びます。
```java
public void stop() {
    loop.stop();
    audioManager.close();
}
```