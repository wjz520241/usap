

package keeno.usap.util;

import keeno.usap.util.collection.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Map-based implementation for {@link ResultHolder}.
 */
public abstract class AbstractResultHolder implements ResultHolder {

    /**
     * Map from analysis ID to the corresponding analysis result.
     */
    private final transient Map<String, Object> results = Maps.newHybridMap();

    @Override
    public <R> void storeResult(String key, R result) {
        results.put(key, result);
    }

    @Override
    public boolean hasResult(String key) {
        return results.containsKey(key);
    }

    @Override
    public <R> R getResult(String key) {
        return (R) results.get(key);
    }

    @Override
    public <R> R getResult(String key, R defaultResult) {
        return (R) results.getOrDefault(key, defaultResult);
    }

    @Override
    public <R> R getResult(String key, Supplier<R> supplier) {
        return (R) results.computeIfAbsent(key, __ -> supplier.get());
    }

    @Override
    public Collection<String> getKeys() {
        return results.keySet();
    }

    @Override
    public void clearResult(String key) {
        results.remove(key);
    }

    @Override
    public void clearAll() {
        results.clear();
    }
}
