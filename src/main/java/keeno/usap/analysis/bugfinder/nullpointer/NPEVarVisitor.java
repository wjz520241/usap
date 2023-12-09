

package keeno.usap.analysis.bugfinder.nullpointer;

import keeno.usap.ir.exp.ArrayLengthExp;
import keeno.usap.ir.exp.InstanceFieldAccess;
import keeno.usap.ir.exp.InvokeInstanceExp;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.LoadArray;
import keeno.usap.ir.stmt.LoadField;
import keeno.usap.ir.stmt.Monitor;
import keeno.usap.ir.stmt.StmtVisitor;
import keeno.usap.ir.stmt.StoreArray;
import keeno.usap.ir.stmt.StoreField;
import keeno.usap.ir.stmt.Throw;
import keeno.usap.ir.stmt.Unary;

class NPEVarVisitor implements StmtVisitor<Var> {

    @Override
    public Var visit(LoadField stmt) {
        return stmt.isStatic() ?
                null : ((InstanceFieldAccess) stmt.getFieldAccess()).getBase();
    }

    @Override
    public Var visit(StoreField stmt) {
        return stmt.isStatic() ?
                null : ((InstanceFieldAccess) stmt.getFieldAccess()).getBase();
    }

    @Override
    public Var visit(Unary stmt) {
        return stmt.getRValue() instanceof ArrayLengthExp ?
                ((ArrayLengthExp) stmt.getRValue()).getBase() : null;
    }

    @Override
    public Var visit(Invoke stmt) {
        return stmt.isStatic() || stmt.isDynamic() ?
                null : ((InvokeInstanceExp) stmt.getInvokeExp()).getBase();
    }

    @Override
    public Var visit(Throw stmt) {
        return stmt.getExceptionRef();
    }

    @Override
    public Var visit(Monitor stmt) {
        return StmtVisitor.super.visit(stmt);
    }

    @Override
    public Var visit(LoadArray stmt) {
        return stmt.getArrayAccess().getBase();
    }

    @Override
    public Var visit(StoreArray stmt) {
        return stmt.getArrayAccess().getBase();
    }
}
