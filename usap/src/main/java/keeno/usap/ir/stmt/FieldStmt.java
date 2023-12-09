

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.FieldAccess;
import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;
import keeno.usap.ir.proginfo.FieldRef;

/**
 * Load/Store field statements.
 */
public abstract class FieldStmt<L extends LValue, R extends RValue> extends AssignStmt<L, R> {

    FieldStmt(L lvalue, R rvalue) {
        super(lvalue, rvalue);
    }

    public abstract FieldAccess getFieldAccess();

    public FieldRef getFieldRef() {
        return getFieldAccess().getFieldRef();
    }

    public boolean isStatic() {
        return getFieldRef().isStatic();
    }
}
