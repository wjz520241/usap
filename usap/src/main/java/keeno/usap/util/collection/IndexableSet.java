

package keeno.usap.util.collection;

import keeno.usap.util.Indexable;

import java.util.Iterator;

/**
 * An efficient set implementation for {@link Indexable} objects.
 *
 * <p>
 * NOTE: this set is not a fully-functional set.
 * It does not actually store the elements (it only stores the indexes),
 * so it cannot support operations on the elements, e.g., iterations.
 * This set is suitable for fast check on presence/absence of certain elements.
 * If you need a fully-functional set backing by bit set,
 * please use {@link IndexerBitSet}.
 * <p>
 * This set extend {@link AbstractSetEx} so that it can be used
 * (as large set) to construct hybrid set.
 *
 * @param <E> type of elements whose indexes are stored in this set
 * @see Indexable
 */
public class IndexableSet<E extends Indexable> extends AbstractSetEx<E> {

    private final IBitSet bitSet;

    public IndexableSet(boolean isSparse) {
        this.bitSet = IBitSet.newBitSet(isSparse);
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Indexable i && bitSet.get(i.getIndex());
    }

    @Override
    public boolean add(E e) {
        return bitSet.set(e.getIndex());
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof Indexable i && bitSet.clear(i.getIndex());
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException(
                getClass() + " does not support iteration");
    }

    @Override
    public int size() {
        return bitSet.cardinality();
    }
}
