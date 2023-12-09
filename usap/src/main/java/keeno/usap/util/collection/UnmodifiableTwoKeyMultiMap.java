

package keeno.usap.util.collection;

import keeno.usap.util.TriConsumer;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

class UnmodifiableTwoKeyMultiMap<K1, K2, V> implements
        TwoKeyMultiMap<K1, K2, V>, Serializable {

    private final TwoKeyMultiMap<K1, K2, V> m;

    UnmodifiableTwoKeyMultiMap(@Nonnull TwoKeyMultiMap<K1, K2, V> m) {
        this.m = Objects.requireNonNull(m);
    }

    @Override
    public boolean contains(K1 key1, K2 key2, V value) {
        return m.contains(key1, key2, value);
    }

    @Override
    public boolean containsKey(K1 key1, K2 key2) {
        return m.containsKey(key1, key2);
    }

    @Override
    public boolean containsKey(K1 key1) {
        return m.containsKey(key1);
    }

    @Override
    public boolean containsValue(V value) {
        return m.containsValue(value);
    }

    @Override
    public Set<V> get(K1 key1, K2 key2) {
        return m.get(key1, key2);
    }

    @Override
    public MultiMap<K2, V> get(K1 key1) {
        return m.get(key1);
    }

    @Override
    public boolean put(@Nonnull K1 key1, @Nonnull K2 key2, @Nonnull V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(K1 key1, K2 key2, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(K1 key1, K2 key2) {
        throw new UnsupportedOperationException();
    }

    private transient Set<Pair<K1, K2>> twoKeySet;

    @Override
    public Set<Pair<K1, K2>> twoKeySet() {
        if (twoKeySet == null) {
            twoKeySet = Collections.unmodifiableSet(m.twoKeySet());
        }
        return twoKeySet;
    }

    private transient Set<TwoKeyMap.Entry<K1, K2, V>> entrySet;

    @Override
    public Set<TwoKeyMap.Entry<K1, K2, V>> entrySet() {
        if (entrySet == null) {
            entrySet = Collections.unmodifiableSet(m.entrySet());
        }
        return entrySet;
    }

    @Override
    public void forEach(@Nonnull TriConsumer<K1, K2, V> action) {
        m.forEach(action);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean equals(Object obj) {
        return m.equals(obj);
    }

    @Override
    public int hashCode() {
        return m.hashCode();
    }

    @Override
    public String toString() {
        return m.toString();
    }
}
