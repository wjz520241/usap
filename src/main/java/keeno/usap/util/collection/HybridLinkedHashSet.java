

package keeno.usap.util.collection;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Hybrid set that uses linked hash set for large set.
 */
public final class HybridLinkedHashSet<E> extends AbstractHybridSet<E>
        implements Serializable {

    @Override
    protected Set<E> newLargeSet(int initialCapacity) {
        return new LinkedHashSet<>(initialCapacity);
    }

    @Override
    protected SetEx<E> newSet() {
        return new HybridLinkedHashSet<>();
    }
}
