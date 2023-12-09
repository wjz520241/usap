

package keeno.usap.util;

import keeno.usap.util.collection.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * This class helps eliminate redundant equivalent elements.
 *
 * @param <T> type of canonicalized elements.
 */
public class Canonicalizer<T> implements Serializable {

    private final Map<T, T> map = Maps.newConcurrentMap();

    public T get(T item) {
        T result = map.get(item);
        if (result == null) {
            result = map.putIfAbsent(item, item);
            if (result == null) {
                result = item;
            }
        }
        return result;
    }
}
