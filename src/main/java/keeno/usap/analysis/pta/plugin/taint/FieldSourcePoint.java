

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.ir.IRPrinter;
import keeno.usap.ir.stmt.LoadField;
import keeno.usap.language.classes.JMethod;

import javax.annotation.Nonnull;
import java.util.Comparator;

public record FieldSourcePoint(JMethod container, LoadField loadField)
        implements SourcePoint {

    private static final Comparator<FieldSourcePoint> COMPARATOR =
            Comparator.comparing((FieldSourcePoint fsp) -> fsp.container.toString())
                    .thenComparingInt(fsp -> fsp.loadField().getIndex());

    @Override
    public int compareTo(@Nonnull SourcePoint sp) {
        if (sp instanceof FieldSourcePoint fsp) {
            return COMPARATOR.compare(this, fsp);
        }
        return SourcePoint.compare(this, sp);
    }

    @Override
    public JMethod getContainer() {
        return container;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public String toString() {
        return container + " " + IRPrinter.position(loadField) + " " + loadField;
    }
}
