

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.plugin.util.InvokeUtils;
import keeno.usap.ir.stmt.Invoke;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * Represents a program location where taint objects flow to a sink.
 *
 * @param sinkCall call site of the sink method.
 * @param index    index of the sensitive argument at {@code sinkCall}.
 */
public record SinkPoint(Invoke sinkCall, int index) implements Comparable<SinkPoint> {

    private static final Comparator<SinkPoint> COMPARATOR =
            Comparator.comparing(SinkPoint::sinkCall)
                    .thenComparingInt(SinkPoint::index);

    @Override
    public int compareTo(@Nonnull SinkPoint other) {
        return COMPARATOR.compare(this, other);
    }

    @Override
    public String toString() {
        return sinkCall + "/" + InvokeUtils.toString(index);
    }
}
