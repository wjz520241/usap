package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.BinaryExp;
import keeno.usap.ir.exp.Var;

public class Binary extends AssignStmt<Var, BinaryExp> {
    public Binary(Var lvalue, BinaryExp rvalue) {
        super(lvalue, rvalue);
    }
}
