

package keeno.usap.util.collection;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * Set implementation based on ArrayList. This class should only be
 * used for small set. Elements cannot be null.
 * Note that remove(Object) will shift the rest elements to the end.
 * TODO: if necessary, optimize remove(Object) and let add(Object) add
 *  element to empty hole of the array.
 */
public class ArraySet<E> extends AbstractSetEx<E>
        implements Serializable {

    public static final int DEFAULT_CAPACITY = 8;

    private static final String NULL_MESSAGE = "ArraySet does not permit null element";

    private final ArrayList<E> elements;

    private final int initialCapacity;

    private final boolean fixedCapacity;

    public ArraySet() {
        this(DEFAULT_CAPACITY, true);
    }

    public ArraySet(int initialCapacity) {
        this(initialCapacity, true);
    }

    public ArraySet(int initialCapacity, boolean fixedCapacity) {
        this.initialCapacity = initialCapacity;
        this.fixedCapacity = fixedCapacity;
        elements = new ArrayList<>(initialCapacity);
    }

    /**
     * Takes given array list as elements.
     * Note that the caller should ensure that {@code elements} contains
     * no duplicate elements.
     */
    public ArraySet(ArrayList<E> elements, boolean fixedCapacity) {
        // assert new java.util.HashSet<>(elements).size() == elements.size();
        this.elements = elements;
        this.initialCapacity = elements.size();
        this.fixedCapacity = fixedCapacity;
    }

    public ArraySet(Collection<? extends E> coll) {
        this(coll.size(), false);
        addAll(coll);
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @Override
    @Nonnull
    public Object[] toArray() {
        return elements.toArray();
    }

    @Override
    @Nonnull
    public <T> T[] toArray(@Nonnull T[] a) {
        //noinspection SuspiciousToArrayCall
        return elements.toArray(a);
    }

    @Override
    public boolean add(E e) {
        Objects.requireNonNull(e, NULL_MESSAGE);
        if (!elements.contains(e)) {
            ensureCapacity(size() + 1);
            elements.add(e);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(Object o) {
        return o != null && elements.remove(o);
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            changed |= add(e);
        }
        return changed;
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        return elements.retainAll(c);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    @Nonnull
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public SetEx<E> copy() {
        ArraySet<E> copy = new ArraySet<>(initialCapacity, fixedCapacity);
        copy.elements.addAll(elements);
        return copy;
    }

    @Override
    public SetEx<E> addAllDiff(Collection<? extends E> c) {
        ArrayList<E> diff;
        if (c instanceof Set) {
            diff = new ArrayList<>();
            for (E e : c) {
                if (add(e)) {
                    diff.add(e);
                }
            }
        } else {
            Set<E> diffSet = Sets.newHybridSet();
            for (E e : c) {
                if (add(e)) {
                    // use set to efficiently filter out duplicate elements
                    diffSet.add(e);
                }
            }
            diff = new ArrayList<>(diffSet);
        }
        return new ArraySet<>(diff, true);
    }

    private void ensureCapacity(int minCapacity) {
        if (fixedCapacity && minCapacity > initialCapacity) {
            throw new TooManyElementsException("Capacity of this ArraySet is fixed");
        }
    }
}
