

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.ArrayAccess;
import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;

abstract class ArrayStmt<L extends LValue, R extends RValue> extends AssignStmt<L, R> {

    ArrayStmt(L lvalue, R rvalue) {
        super(lvalue, rvalue);
    }

    public abstract ArrayAccess getArrayAccess();
}
