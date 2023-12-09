

package keeno.usap.analysis.pta.plugin.taint;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * Each instance represents a taint flow from source to sink.
 */
public record TaintFlow(SourcePoint sourcePoint, SinkPoint sinkPoint)
        implements Comparable<TaintFlow> {

    private static final Comparator<TaintFlow> COMPARATOR =
            Comparator.comparing(TaintFlow::sourcePoint)
                    .thenComparing(TaintFlow::sinkPoint);

    @Override
    public int compareTo(@Nonnull TaintFlow other) {
        return COMPARATOR.compare(this, other);
    }

    @Override
    public String toString() {
        return String.format("TaintFlow{%s -> %s}", sourcePoint, sinkPoint);
    }
}
