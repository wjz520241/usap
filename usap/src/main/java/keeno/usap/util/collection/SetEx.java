

package keeno.usap.util.collection;

import keeno.usap.util.Copyable;

import java.util.Collection;
import java.util.Set;

/**
 * This interface extends {@link Set} to provide more useful APIs.
 *
 * @param <E> type of elements in this set
 */
public interface SetEx<E> extends Set<E>, Copyable<SetEx<E>> {

    /**
     * Adds all elements in collection {@code c}, and returns the difference set
     * between {@code c} and this set (before the call).
     *
     * @return a set of elements that are contained in {@code c} but
     * not in this set before the call.
     */
    SetEx<E> addAllDiff(Collection<? extends E> c);

    /**
     * @return {@code true} if this set has at least one element
     * contained in the given set.
     */
    boolean hasOverlapWith(Set<E> other);
}
