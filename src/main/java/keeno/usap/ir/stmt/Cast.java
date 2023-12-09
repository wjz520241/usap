

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.CastExp;
import keeno.usap.ir.exp.Var;

/**
 * Representation of cast statement, e.g., a = (T) b.
 */
public class Cast extends AssignStmt<Var, CastExp> {

    public Cast(Var lvalue, CastExp rvalue) {
        super(lvalue, rvalue);
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
