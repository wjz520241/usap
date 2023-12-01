package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.FieldRef;

import java.util.Set;

/**
 * 实例字段访问, o.f.
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
    public String toString() {
        return base + "." + fieldRef;
    }
}
