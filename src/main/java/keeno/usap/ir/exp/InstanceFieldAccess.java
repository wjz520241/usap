

package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.FieldRef;

import java.util.Set;

/**
 * Representation of instance field access expression, e.g., o.f.
 */
public class InstanceFieldAccess extends FieldAccess {

    private final Var base;

    public InstanceFieldAccess(FieldRef fieldRef, Var base) {
        super(fieldRef);
        this.base = base;
    }

    public Var getBase() {
        return base;
    }

    @Override
    public Set<RValue> getUses() {
        return Set.of(base);
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return base + "." + fieldRef;
    }
}
