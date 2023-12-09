

package keeno.usap.util.collection;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

class UnmodifiableMultiMap<K, V> implements MultiMap<K, V>, Serializable {

    private final MultiMap<K, V> m;

    UnmodifiableMultiMap(@Nonnull MultiMap<K, V> m) {
        this.m = Objects.requireNonNull(m);
    }

    @Override
    public boolean contains(K key, V value) {
        return m.contains(key, value);
    }

    @Override
    public boolean containsKey(K key) {
        return m.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return m.containsValue(value);
    }

    @Override
    public Set<V> get(K key) {
        return m.get(key);
    }

    @Override
    public boolean put(@Nonnull K key, @Nonnull V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean putAll(@Nonnull K key, @Nonnull Collection<? extends V> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean putAll(@Nonnull MultiMap<? extends K, ? extends V> multiMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(K key, Collection<? extends V> values) {
        throw new UnsupportedOperationException();
    }

    /**
     * The cache of {@link UnmodifiableMultiMap#keySet()}.
     */
    private transient Set<K> keySet;

    /**
     * The cache of {@link UnmodifiableMultiMap#values()}.
     */
    private transient Collection<V> values;

    /**
     * The cache of {@link UnmodifiableMultiMap#entrySet()}.
     */
    private transient Set<Map.Entry<K, V>> entrySet;

    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = Collections.unmodifiableSet(m.keySet());
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        if (values == null) {
            values = Collections.unmodifiableCollection(m.values());
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = Collections.unmodifiableSet(m.entrySet());
        }
        return entrySet;
    }

    @Override
    public void forEachSet(@Nonnull BiConsumer<K, Set<V>> action) {
        m.forEachSet(action);
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
    public boolean equals(Object o) {
        return o == this || m.equals(o);
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
