

package keeno.usap.util.collection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Utility methods for {@link Collection}.
 * We name it CollectionUtils instead of Collections to avoid name collision
 * with {@link java.util.Collections}.
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * Iterates the elements in the specific collection, in the order they are
     * returned by the collection's iterator, and finds the first element
     * of given collection that satisfies the predicate. If not such element
     * is found, returns {@code null}.
     */
    @Nullable
    public static <T> T findFirst(Collection<? extends T> c,
                                  Predicate<? super T> p) {
        for (T e : c) {
            if (p.test(e)) {
                return e;
            }
        }
        return null;
    }

    /**
     * @return an arbitrary element of the given collection.
     */
    public static <T> T getOne(Collection<T> c) {
        return c.iterator().next();
    }

    /**
     * Creates a list of given collection, appends a specific element to
     * the list and returns it.
     */
    public static <T> List<T> append(Collection<? extends T> c, T e) {
        List<T> result = new ArrayList<>(c.size() + 1);
        result.addAll(c);
        result.add(e);
        return result;
    }

    /**
     * Maps each element in given collection to an integer and computes
     * the sum of the integers.
     */
    public static <T> long sum(Collection<? extends T> c, ToIntFunction<T> toInt) {
        long sum = 0;
        for (var e : c) {
            sum += toInt.applyAsInt(e);
        }
        return sum;
    }

    /**
     * Converts a collection to a string.
     * The elements in the collection are <b>sorted</b> by their
     * string representation (in alphabet order) in the resulting string.
     * This is particularly useful for comparing expected results
     * with the ones given by the analysis.
     */
    public static <T> String toString(Collection<T> c) {
        return Streams.toString(c.stream());
    }

    /**
     * Converts a collection to a set.
     */
    public static <T> Set<T> toSet(Collection<T> c) {
        if (c instanceof Set) {
            return Collections.unmodifiableSet((Set<T>) c);
        } else {
            return Collections.unmodifiableSet(Sets.newHybridSet(c));
        }
    }
}
