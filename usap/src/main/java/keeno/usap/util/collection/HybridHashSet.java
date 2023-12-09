

package keeno.usap.util.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Hybrid set that uses hash set for large set.
 */
public final class HybridHashSet<E> extends AbstractHybridSet<E>
        implements Serializable {

    /**
     * Constructs a new hybrid set.
     */
    public HybridHashSet() {
    }

    /**
     * Constructs a new hybrid set from the given collection.
     */
    public HybridHashSet(Collection<E> c) {
        super(c);
    }

    @Override
    protected Set<E> newLargeSet(int initialCapacity) {
        return new HashSet<>(initialCapacity);
    }

    @Override
    protected SetEx<E> newSet() {
        return new HybridHashSet<>();
    }
}
