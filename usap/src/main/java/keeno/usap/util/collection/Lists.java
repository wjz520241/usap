

package keeno.usap.util.collection;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * Utility methods for {@link List}.
 */
public final class Lists {

    private Lists() {
    }

    /**
     * Applies a mapper function on a given collection and returns
     * the results as a list. The resulting list is unmodifiable.
     */
    public static <T, R> List<R> map(Collection<? extends T> c,
                                     Function<T, R> mapper) {
        return c.isEmpty() ? List.of() : c.stream().map(mapper).toList();
    }

    /**
     * Tests the elements in a given collection and returns a list of elements
     * that can pass the test. The resulting list is unmodifiable.
     */
    public static <T> List<T> filter(Collection<T> c,
                                     Predicate<? super T> predicate) {
        List<T> result = c.stream().filter(predicate).toList();
        return result.isEmpty() ? List.of() : result;
    }

    /**
     * Converts an iterable object to a list.
     */
    public static <T> List<T> asList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }

    /**
     * Concatenates two lists and removes duplicate items in the resulting list.
     */
    public static <T> List<T> concatDistinct(
            List<? extends T> list1, List<? extends T> list2) {
        Set<T> set = new LinkedHashSet<>(list1);
        set.addAll(list2);
        return List.copyOf(set);
    }
}
