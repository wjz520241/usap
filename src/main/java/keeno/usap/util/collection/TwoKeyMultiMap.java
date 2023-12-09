

package keeno.usap.util.collection;

import keeno.usap.util.TriConsumer;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

/**
 * A collection that maps two-key pairs to values, similar to {@link TwoKeyMap},
 * but in which each two-key pair may be associated with <i>multiple</i> values.
 * The values associated with the same two-key pair contain <i>no</i> duplicates.
 * You can visualize the contents of a two-key multimap either as a map from keys
 * to multimaps as values:
 *
 * <ul>
 *   <li>k1 -> { k2 -> [v1, v2], k3 -> [v3] }
 *   <li>k2 -> { k4 -> [v1, v4] }
 * </ul>
 * <p>
 * ... or as a single "flattened" collection of key1-key2-value triples:
 * <ul>
 *   <li>k1, k2 -> v1
 *   <li>k1, k2 -> v2
 *   <li>k1, k3 -> v3
 *   <li>k2, k4 -> v1
 *   <li>k2, k4 -> v4
 * </ul>
 * <p>
 * Note that both {@code null} keys and values are <i>not</i> permitted in this map.
 *
 * @param <K1> type of first keys in this map
 * @param <K2> type of second keys in this map
 * @param <V>  type of values in this map
 */
public interface TwoKeyMultiMap<K1, K2, V> {

    /**
     * @return {@code true} if given key1-key2-value mapping is contained
     * in this two-key multimap.
     */
    boolean contains(K1 key1, K2 key2, V value);

    /**
     * @return {@code true} if this two-key multimap contains the mapping
     * with {@code key1} as the first key and {@code key2} as the second key.
     */
    boolean containsKey(K1 key1, K2 key2);

    /**
     * @return {@code true} if this two-key multimap contains at least
     * one mapping with {@code key1} as the first key.
     */
    boolean containsKey(K1 key1);

    /**
     * @return {@code true} if this two-key multimap contains at least
     * one mapping with {@code value} as the value. Note that this operation
     * may be slow compared to {@link #containsKey(Object)}.
     */
    boolean containsValue(V value);

    /**
     * @return an unmodifiable view of the values associated with given keys
     * in this multimap, if the two-key pair is absent; otherwise,
     * returns an empty set.
     */
    Set<V> get(K1 key1, K2 key2);

    /**
     * @return an unmodifiable view of the values associated with given key
     * in this two-key multimap, if the key is absent; otherwise,
     * returns an empty multimap.
     */
    MultiMap<K2, V> get(K1 key1);

    /**
     * Stores a key1-key2-value triple in this two-key multimap.
     *
     * @return {@code true} if this two-key multimap changed.
     */
    boolean put(@Nonnull K1 key1, @Nonnull K2 key2, @Nonnull V value);

    /**
     * Removes a single key1-key2-value triple from this two-key multimap,
     * if it exists.
     *
     * @return {@code true} if the two-key multimap changed
     */
    boolean remove(K1 key1, K2 key2, V value);

    /**
     * Removes all values associated with the two-key pair.
     *
     * @return {@code true} if the multimap changed.
     */
    boolean removeAll(K1 key1, K2 key2);

    /**
     * @return an unmodifiable view of all <i>distinct</i> two-key pairs
     * contained in this two-key multimap. Note that the result contains
     * a two-key pair if and only if this map maps that key pair to
     * at least one non-{@code null} value.
     */
    Set<Pair<K1, K2>> twoKeySet();

    /**
     * @return an unmodifiable view of all key1-key2-value triples
     * contained in this two-key multimap, as {@link TwoKeyMap.Entry} instances.
     */
    Set<TwoKeyMap.Entry<K1, K2, V>> entrySet();

    /**
     * Performs the given action for all key1-key2-value triples
     * contained in this map.
     */
    default void forEach(@Nonnull TriConsumer<K1, K2, V> action) {
        Objects.requireNonNull(action);
        entrySet().forEach(entry -> action.accept(
                entry.key1(), entry.key2(), entry.value()));
    }

    /**
     * Removes all the mappings from this map.
     * The map will be empty after this call returns.
     */
    void clear();

    /**
     * @return {@code true} if this map contains no key1-key2-value mappings.
     */
    boolean isEmpty();

    /**
     * @return the number of key1-key2-value mappings in this map.
     */
    int size();
}
