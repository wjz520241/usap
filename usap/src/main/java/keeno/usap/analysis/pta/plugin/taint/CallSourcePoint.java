

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.plugin.util.InvokeUtils;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.classes.JMethod;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * A {@code CallSourcePoint} is variable at an invocation site.
 */
public record CallSourcePoint(Invoke sourceCall, int index) implements SourcePoint {

    private static final Comparator<CallSourcePoint> COMPARATOR =
            Comparator.comparing((CallSourcePoint csp) -> csp.sourceCall)
                    .thenComparingInt(CallSourcePoint::index);

    @Override
    public int compareTo(@Nonnull SourcePoint sp) {
        if (sp instanceof CallSourcePoint csp) {
            return COMPARATOR.compare(this, csp);
        }
        return SourcePoint.compare(this, sp);
    }

    @Override
    public JMethod getContainer() {
        return sourceCall.getContainer();
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public String toString() {
        return sourceCall.toString() + "/" + InvokeUtils.toString(index);
    }

}
