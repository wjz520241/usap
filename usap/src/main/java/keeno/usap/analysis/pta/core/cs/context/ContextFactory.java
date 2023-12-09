

package keeno.usap.analysis.pta.core.cs.context;

/**
 * Factory of contexts, which provides convenient APIs to create contexts.
 *
 * @param <T> type of elements of created contexts.
 */
public interface ContextFactory<T> {

    /**
     * @return the empty context.
     */
    Context getEmptyContext();

    /**
     * @return the context with one element.
     */
    Context make(T elem);

    /**
     * @return the context that consists of given elements.
     */
    Context make(T... elems);

    /**
     * @return a context with last k elements of given context.
     */
    Context makeLastK(Context context, int k);

    /**
     * Constructs a context by appending a context element to a parent context.
     * The length of the resulting context will be restricted by given limit.
     *
     * @return the resulting context.
     */
    Context append(Context parent, T elem, int limit);
}
