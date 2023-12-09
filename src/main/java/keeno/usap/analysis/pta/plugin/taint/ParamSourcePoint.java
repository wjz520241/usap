

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.language.classes.JMethod;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * A {@code ParamSourcePoint} is a parameter of a method.
 */
public record ParamSourcePoint(JMethod sourceMethod, int index) implements SourcePoint {

    private static final Comparator<ParamSourcePoint> COMPARATOR =
            Comparator.comparing((ParamSourcePoint psp) -> psp.sourceMethod.toString())
                    .thenComparingInt(ParamSourcePoint::index);

    @Override
    public int compareTo(@Nonnull SourcePoint sp) {
        if (sp instanceof ParamSourcePoint psp) {
            return COMPARATOR.compare(this, psp);
        }
        return SourcePoint.compare(this, sp);
    }

    @Override
    public JMethod getContainer() {
        return sourceMethod;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String toString() {
        return sourceMethod + "/" + index;
    }
}
