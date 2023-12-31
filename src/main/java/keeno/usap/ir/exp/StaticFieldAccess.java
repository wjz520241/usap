

package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.FieldRef;

/**
 * 静态字段访问, T.f.
 */
public class StaticFieldAccess extends FieldAccess {

    public StaticFieldAccess(FieldRef fieldRef) {
        super(fieldRef);
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return fieldRef.toString();
    }
}
