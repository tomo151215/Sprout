# Day18: InputManagerの改善
Day18ではInputManagerの改善を行います。具体的には以下のような機能を追加します。
1. 同じキーコードの重複登録を防ぐ 
2. キーマッピングを削除できるようにする 
3. 1つのアクションの割り当てをまとめて消せるようにする 
4. すべての割り当てを消せるようにする 
5. 登録済みキー一覧を取得できるようにする 

## 同じキーコードの重複登録を防ぐ
ひとつのアクションに対してListでキーを登録しているが、ArrayListなので重複したキーコードの登録ができてしまいます。そこで、重複登録ができないように`addMapping()`メソッドを以下の様に改良します。
```java
    public void addMapping(T action, int keycode) {
        List<Integer> keys = mappings.get(action);
        if (!keys.contains(keycode)) {
            keys.add(keycode);
        }
    }
```

### `mappings.get(action)`を補助メソッドにする
`mappings.get(action)`は複数回使用されるのでメソッド化しておきます。
```java
private List<Integer> getKeyList(T action) {
    if (action == null) {
        throw new IllegalArgumentException("action must not be null. ");
    }
    List<Integer> keys = mappings.get(action);
    if (keys == null) {
        throw new IllegalArgumentException("Unknown action: " + action);
    }
    return keys;
}
```

## キーマッピングを削除できるようにする 
アクションに対応するキーコードを削除する機能がないので作成します。これはプレイヤーが独自のキーマッピングを作れるようにする機能を作成するために追加します。
```java
public void removeMapping(T action, int keycode) {
    getKeyList(action).remove(Integer.valueOf(keycode));
}
```
Listの`remove()`メソッドは２種類あり、`remove(int index)`と `remove(Object o)`があります。前者はindex番目を削除し、後者はオブジェクトを指定して、そのものを削除します。keycodeはint型なのでそのまま指定するとkeycode番目の要素を削除してしまうので、keycodeをInteger型に変換して、オブジェクトを指定して削除します。

### アクションに対応するすべてのkeycodeを削除
```java
public void clearMapping(T action) {
    getKeyList(action).clear();
}
```
Listの要素をすべて削除して、要素数０にするには`clear()`メソッドを使用します。

### すべてのキーマッピングを削除
すべてのキーマッピングを削除し、新しく作り直すためのメソッドを用意します。
```java
public void clearAllMapping() {
    for (List<Integer> value : mappings.values()) {
        value.clear();
    }
}
```
Mapの「値」だけをListとして返すには`values()`メソッドを使用します。各値はListなので、その要素を`clear()`ですべて削除します。

## キー一覧を取得する
現在登録されているアクションに対するキー一覧を取得するために`getMappings()`を作ります。
```java
public List<Integer> getMappings(T action) {
    return List.copyOf(getKeyList(action));
}
```
`List.copyOf(List)`で引数Listのコピーリストを返します。このメソッドはpublicなのでgetKeyListの様に内部だけに使用を許可するものではありません。よって`return getKeyList(action)`のように内部リストの参照を直接返すと、呼び出し元で削除、追加などの改変が行われてしまう可能性があります。よって、リストのコピーを返すことで万が一意図しない変更を加えられても大丈夫なようにします。（防御的コピー）

## あるアクションに特定のキーが割り当てられているかどうかを確認
あるアクションに特定のキーが割り当てられているかを確認するために、`hasMapping()`もあると便利です。
```java
public boolean hasMapping(T action, int keyCode) {
    return getKeyList(action).contains(keyCode);
}
```
ただし、`addMapping()`はすでに重複登録を防ぐので、必ずしも毎回`hasMapping()`を使う必要はありません。

## 改善後のInputManager
```java
package engine.input;

import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public final class InputManager<T extends Enum<T>> {
    private final Keyboard keyboard;
    private final Map<T, List<Integer>> mappings;

    public InputManager(Keyboard keyboard, Class<T> actionClass) {
        this.keyboard = keyboard;
        this.mappings = new EnumMap<>(actionClass);
        for (T action : actionClass.getEnumConstants()) {
            mappings.put(action, new ArrayList<>());
        }
    }

    public void addMapping(T action, int keycode) {
        List<Integer> keys = getKeyList(action);
        if (!keys.contains(keycode)) {
            keys.add(keycode);
        }
    }

    public boolean isPressed(T action) {
        for (int keycode : getKeyList(action)) {
            if (keyboard.isPressed(keycode))
                return true;
        }
        return false;
    }

    public boolean isJustPressed(T action) {
        for (int keycode : getKeyList(action)) {
            if (keyboard.isJustPressed(keycode))
                return true;
        }
        return false;
    }

    public boolean isJustReleased(T action) {
        for (int keycode : getKeyList(action)) {
            if (keyboard.isJustReleased(keycode))
                return true;
        }
        return false;
    }

    private List<Integer> getKeyList(T action) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null. ");
        }
        List<Integer> keys = mappings.get(action);
        if (keys == null) {
            throw new IllegalArgumentException("Unknown action: " + action);
        }
        return keys;
    }

    public void removeMapping(T action, int keycode) {
        getKeyList(action).remove(Integer.valueOf(keycode));
    }

    public void clearMapping(T action) {
        getKeyList(action).clear();
    }

    public void clearAllMapping() {
        for (List<Integer> value : mappings.values()) {
            value.clear();
        }
    }

    public List<Integer> getMappings(T action) {
        return List.copyOf(getKeyList(action));
    }

    public boolean hasMapping(T action, int keyCode) {
        return getKeyList(action).contains(keyCode);
    }
}
```