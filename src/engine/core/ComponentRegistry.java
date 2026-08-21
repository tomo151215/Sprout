package engine.core;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

final class ComponentRegistry<T> implements Iterable<T> {
    private final CopyOnWriteArrayList<T> components = new CopyOnWriteArrayList<>();

    void add(T component) {
        components.addIfAbsent(Objects.requireNonNull(component, "component must not be null."));
    }

    void remove(T component) {
        if (component != null) {
            components.remove(component);
        }
    }

    void forEachComponent(Consumer<? super T> action) {
        components.forEach(Objects.requireNonNull(action, "action must not be null."));
    }

    @Override
    public Iterator<T> iterator() {
        return components.iterator();
    }
}
