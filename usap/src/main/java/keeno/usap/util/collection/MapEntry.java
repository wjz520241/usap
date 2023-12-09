

package keeno.usap.util.collection;

import keeno.usap.util.Hashes;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Map entry.
 * Pair of a key and a value.
 */
public class MapEntry<K, V> implements Entry<K, V>, Serializable {

    private final K key;

    private V value;

    /**
     * Constructs a new map entry.
     */
    public MapEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Entry<?, ?> e)) {
            return false;
        }
        return Objects.equals(key, e.getKey()) &&
                Objects.equals(value, e.getValue());
    }

    @Override
    public int hashCode() {
        return Hashes.safeHash(key, value);
    }

    @Override
    public String toString() {
        return "MapEntry{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
