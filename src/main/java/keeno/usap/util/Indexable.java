

package keeno.usap.util;

/**
 * The instances of the classes that implement this interface can provide
 * a unique <b>non-negative</b> index, so that they can be stored in efficient
 * data structures (e.g., bit set).
 * <p>
 * Note that the index of each object might NOT be globally unique,
 * when the indexes are unique within certain scope (e.g., the indexes
 * of local variables are unique only in the same method), and thus
 * the client code should use the indexes carefully.
 */
public interface Indexable {

    /**
     * @return index of this object. The index should be a non-negative integer.
     */
    int getIndex();
}
