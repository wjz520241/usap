

package keeno.usap.util;

/**
 * @param <T> type of copy object
 */
public interface Copyable<T> {

    /**
     * Creates and returns a copy of this object.
     *
     * @return a copy of this object.
     */
    T copy();
}
