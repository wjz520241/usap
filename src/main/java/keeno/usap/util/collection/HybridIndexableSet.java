

package keeno.usap.util.collection;

import keeno.usap.util.Indexable;

import java.io.Serializable;
import java.util.Set;

/**
 * Hybrid set that uses indexable set for large set.
 */
public class HybridIndexableSet<E extends Indexable>
        extends AbstractHybridSet<E> implements Serializable {

    private final boolean isSparse;

    public HybridIndexableSet(boolean isSparse) {
        this.isSparse = isSparse;
    }

    @Override
    protected Set<E> newLargeSet(int unused) {
        return new IndexableSet<>(isSparse);
    }
}
