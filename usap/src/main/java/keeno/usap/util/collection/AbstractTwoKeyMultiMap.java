

package keeno.usap.util.collection;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.StringJoiner;

public abstract class AbstractTwoKeyMultiMap<K1, K2, V> implements
        TwoKeyMultiMap<K1, K2, V>, Serializable {

    protected static final String NULL_KEY = "TwoKeyMultiMap does not permit null keys";

    protected static final String NULL_VALUE = "TwoKeyMultiMap does not permit null values";

    private transient Set<TwoKeyMap.Entry<K1, K2, V>> entrySet;

    @Override
    public Set<TwoKeyMap.Entry<K1, K2, V>> entrySet() {
        var es = entrySet;
        if (es == null) {
            es = Collections.unmodifiableSet(new EntrySet());
            entrySet = es;
        }
        return es;
    }

    private final class EntrySet extends AbstractSet<TwoKeyMap.Entry<K1, K2, V>> {

        @Override
        public boolean contains(Object o) {
            if (o instanceof TwoKeyMap.Entry<?, ?, ?> entry) {
                //noinspection unchecked
                return AbstractTwoKeyMultiMap.this.contains(
                        (K1) entry.key1(), (K2) entry.key2(), (V) entry.value());
            }
            return false;
        }

        @Override
        @Nonnull
        public Iterator<TwoKeyMap.Entry<K1, K2, V>> iterator() {
            return entryIterator();
        }

        @Override
        public int size() {
            return AbstractTwoKeyMultiMap.this.size();
        }
    }

    protected abstract Iterator<TwoKeyMap.Entry<K1, K2, V>> entryIterator();

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TwoKeyMultiMap)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        TwoKeyMultiMap<K1, K2, V> that = (TwoKeyMultiMap<K1, K2, V>) obj;
        if (size() != that.size()) {
            return false;
        }
        try {
            for (Pair<K1, K2> twoKey : twoKeySet()) {
                K1 key1 = twoKey.first();
                K2 key2 = twoKey.second();
                if (!get(key1, key2).equals(that.get(key1, key2))) {
                    return false;
                }
            }
        } catch (ClassCastException | NullPointerException ignored) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (Pair<K1, K2> twoKey : twoKeySet()) {
            K1 key1 = twoKey.first();
            K2 key2 = twoKey.second();
            joiner.add(key1 + "," + key2 + "=" + get(key1, key2));
        }
        return joiner.toString();
    }
}
