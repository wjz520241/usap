

package keeno.usap.util.collection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Hybrid map that uses hash map for large map.
 */
public final class HybridHashMap<K, V> extends AbstractHybridMap<K, V>
        implements Serializable {

    /**
     * Constructs a new empty hybrid map.
     */
    public HybridHashMap() {
    }

    /**
     * Constructs a new hybrid map from the given map.
     */
    public HybridHashMap(Map<K, V> m) {
        super(m);
    }

    @Override
    protected Map<K, V> newLargeMap(int initialCapacity) {
        return new HashMap<>(initialCapacity);
    }
}
