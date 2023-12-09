

package keeno.usap.analysis.dataflow.fact;

import keeno.usap.util.Copyable;
import keeno.usap.util.collection.CollectionUtils;
import keeno.usap.util.collection.GenericBitSet;
import keeno.usap.util.collection.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Represents set-like data-flow facts.
 *
 * @param <E> type of elements
 */
public class SetFact<E> implements Copyable<SetFact<E>>, Iterable<E> {

    protected final Set<E> set;

    public SetFact(Collection<E> c) {
        if (c instanceof GenericBitSet<E> s) {
            set = s.copy();
        } else {
            set = Sets.newHybridSet(c);
        }
    }

    public SetFact() {
        this(Collections.emptySet());
    }

    /**
     * @return true if this set contains the specified element, otherwise false.
     */
    public boolean contains(E e) {
        return set.contains(e);
    }

    /**
     * Adds an element to this fact.
     *
     * @return true if this fact changed as a result of the call, otherwise false.
     */
    public boolean add(E e) {
        return set.add(e);
    }

    /**
     * Removes an element from this fact.
     *
     * @return true if an element was removed as a result of the call, otherwise false.
     */
    public boolean remove(E e) {
        return set.remove(e);
    }

    /**
     * Removes all the elements of this fact that satisfy the given predicate.
     *
     * @return true if any elements were removed as a result of the call,
     * otherwise false.
     */
    public boolean removeIf(Predicate<E> filter) {
        return set.removeIf(filter);
    }

    /**
     * Removes all elements of other fact.
     *
     * @return true if this fact changed as a result of the call, otherwise false.
     */
    public boolean removeAll(SetFact<E> other) {
        return set.removeAll(other.set);
    }

    /**
     * Unions other fact into this fact.
     *
     * @return true if this fact changed as a result of the call, otherwise false.
     */
    public boolean union(SetFact<E> other) {
        return set.addAll(other.set);
    }

    /**
     * @return a new fact which is the union of this and other facts.
     */
    public SetFact<E> unionWith(SetFact<E> other) {
        SetFact<E> result = copy();
        result.union(other);
        return result;
    }

    /**
     * Intersects this fact with other fact.
     *
     * @return true if this fact changed as a result of the call, otherwise false.
     */
    public boolean intersect(SetFact<E> other) {
        return set.retainAll(other.set);
    }

    /**
     * @return a new fact which is the intersection of this and other facts.
     */
    public SetFact<E> intersectWith(SetFact<E> other) {
        SetFact<E> result = copy();
        result.intersect(other);
        return result;
    }

    /**
     * Sets the content of this set to the same as other set.
     */
    public void set(SetFact<E> other) {
        if (set instanceof GenericBitSet<E> s) {
            s.setTo(other.set);
        } else {
            clear();
            union(other);
        }
    }

    /**
     * Creates and returns a copy of this fact.
     */
    @Override
    public SetFact<E> copy() {
        return new SetFact<>(this.set);
    }

    /**
     * Clears all content in this fact.
     */
    public void clear() {
        set.clear();
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public Stream<E> stream() {
        return set.stream();
    }

    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    public void forEach(Consumer<? super E> action) {
        set.forEach(action);
    }

    public int size() {
        return set.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SetFact<?> that)) {
            return false;
        }
        return set.equals(that.set);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @Override
    public String toString() {
        return CollectionUtils.toString(set);
    }
}
