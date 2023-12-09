

package keeno.usap.util.collection;

class ImmutableMapEntry<K, V> extends MapEntry<K, V> {

    ImmutableMapEntry(K key, V value) {
        super(key, value);
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }
}
