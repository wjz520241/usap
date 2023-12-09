

package keeno.usap.analysis.pta.core.heap;

import keeno.usap.ir.stmt.New;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ReferenceType;
import keeno.usap.language.type.Type;

import java.util.Optional;

/**
 * Objects that are created by new statements.
 */
public class NewObj extends Obj {

    private final New allocSite;

    NewObj(New allocSite) {
        this.allocSite = allocSite;
    }

    @Override
    public ReferenceType getType() {
        return allocSite.getRValue().getType();
    }

    @Override
    public New getAllocation() {
        return allocSite;
    }

    @Override
    public Optional<JMethod> getContainerMethod() {
        return Optional.of(allocSite.getContainer());
    }

    @Override
    public Type getContainerType() {
        return allocSite.getContainer()
                .getDeclaringClass()
                .getType();
    }

    @Override
    public String toString() {
        return String.format("NewObj{%s[%d@L%d] %s}",
                allocSite.getContainer(), allocSite.getIndex(),
                allocSite.getLineNumber(), allocSite.getRValue());
    }
}
