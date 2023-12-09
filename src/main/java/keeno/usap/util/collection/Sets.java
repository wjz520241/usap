

package keeno.usap.util.collection;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static utility methods for {@link Set}.
 */
public final class Sets {

    private Sets() {
    }

    // Factory methods for sets and maps
    public static <E> Set<E> newSet() {
        return new HashSet<>();
    }

    public static <E> Set<E> newSet(Collection<? extends E> set) {
        return new HashSet<>(set);
    }

    public static <E> Set<E> newLinkedSet() {
        return new LinkedHashSet<>();
    }

    public static <E> Set<E> newSet(int initialCapacity) {
        if (initialCapacity <= ArraySet.DEFAULT_CAPACITY) {
            return newSmallSet();
        } else {
            return newSet();
        }
    }

    public static <E extends Comparable<E>> TreeSet<E> newOrderedSet() {
        return new TreeSet<>();
    }

    public static <E> TreeSet<E> newOrderedSet(Comparator<? super E> comparator) {
        return new TreeSet<>(comparator);
    }

    public static <E> Set<E> newSmallSet() {
        return new ArraySet<>();
    }

    public static <E> Set<E> newHybridSet() {
        return new HybridHashSet<>();
    }

    public static <E> Set<E> newHybridSet(Collection<E> c) {
        return new HybridHashSet<>(c);
    }

    public static <E> Set<E> newHybridOrderedSet() {
        return new HybridLinkedHashSet<>();
    }

    public static <E> Set<E> newConcurrentSet() {
        return ConcurrentHashMap.newKeySet();
    }

    /**
     * @return {@code true} if two sets have at least one overlapped element.
     */
    public static <E> boolean haveOverlap(Set<E> s1, Set<E> s2) {
        Set<E> small, large;
        if (s1.size() <= s2.size()) {
            small = s1;
            large = s2;
        } else {
            small = s2;
            large = s1;
        }
        for (E o : small) {
            if (large.contains(o)) {
                return true;
            }
        }
        return false;
    }
}
