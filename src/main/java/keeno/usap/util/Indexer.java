

package keeno.usap.util;

import java.io.Serializable;

/**
 * An indexer assigns each object a unique index, so that the objects
 * can be stored in efficient data structures. Symmetrically, an indexer
 * can map an index to the corresponding object.
 * <p>
 * Note that each object in the same indexer has a unique index,
 * but different indexers may map different objects (indexes)
 * to the same index (object), so it should be used with care.
 * <p>
 * The objects in an indexer {@code i} should preserve the invariant:
 * <code>e.equals(i.getObject(i.getIndex(e)))</code>.
 *
 * @param <E> type of objects to be indexed
 */
public interface Indexer<E> extends Serializable {

    /**
     * @return the index of the given object.
     */
    int getIndex(E o);

    /**
     * @return the corresponding object of the given index.
     */
    E getObject(int index);
}
