

package keeno.usap.util.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Set;

public abstract class AbstractSetEx<E> extends AbstractSet<E>
        implements SetEx<E> {

    @Override
    public SetEx<E> copy() {
        SetEx<E> copy = newSet();
        copy.addAll(this);
        return copy;
    }

    @Override
    public SetEx<E> addAllDiff(Collection<? extends E> c) {
        SetEx<E> diff = newSet();
        for (E e : c) {
            if (add(e)) {
                diff.add(e);
            }
        }
        return diff;
    }

    /**
     * Creates and returns a new set. The type of the new set should be the
     * corresponding subclass.
     * This method is provided to ease the implementation of {@link #copy()}
     * and {@link #addAllDiff(Collection)}. If a subclass overwrites
     * above two methods, it does not need to re-implement this method.
     */
    protected SetEx<E> newSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasOverlapWith(Set<E> other) {
        return Sets.haveOverlap(this, other);
    }
}
