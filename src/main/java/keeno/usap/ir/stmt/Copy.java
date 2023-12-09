

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.Var;

/**
 * Representation of copy statement, e.g., a = b.
 */
public class Copy extends AssignStmt<Var, Var> {

    public Copy(Var lvalue, Var rvalue) {
        super(lvalue, rvalue);
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
