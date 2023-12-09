

package keeno.usap.util.collection;

import keeno.usap.util.Indexer;

import java.io.Serializable;

/**
 * This implementation leverages {@link Indexer} to take care of the mappings
 * between objects and indexes. The indexer itself acts as the context object.
 *
 * @param <E> type of elements
 * @see Indexer
 */
public class IndexerBitSet<E> extends GenericBitSet<E>
        implements Serializable {

    private final Indexer<E> indexer;

    public IndexerBitSet(Indexer<E> indexer, boolean isSparse) {
        super(isSparse);
        this.indexer = indexer;
    }

    @Override
    protected Object getContext() {
        return indexer;
    }

    @Override
    protected int getIndex(E o) {
        return indexer.getIndex(o);
    }

    @Override
    protected E getElement(int index) {
        return indexer.getObject(index);
    }

    @Override
    protected GenericBitSet<E> newSet() {
        return new IndexerBitSet<>(indexer, IBitSet.isSparse(bitSet));
    }
}
