

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.Literal;
import keeno.usap.ir.exp.Var;

/**
 * Representation of statement that assigns literals, e.g., a = 10.
 * TODO: give a better name (replace Assign)?
 */
public class AssignLiteral extends AssignStmt<Var, Literal> {

    public AssignLiteral(Var lvalue, Literal rvalue) {
        super(lvalue, rvalue);
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
