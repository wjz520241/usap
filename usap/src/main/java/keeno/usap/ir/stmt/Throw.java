

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.RValue;
import keeno.usap.ir.exp.Var;

import java.util.Set;

/**
 * Representation of throw exception statement, e.g., throw e.
 */
public class Throw extends AbstractStmt {

    /**
     * Reference of the exception object to be thrown.
     */
    private final Var exceptionRef;

    public Throw(Var exceptionRef) {
        this.exceptionRef = exceptionRef;
    }

    public Var getExceptionRef() {
        return exceptionRef;
    }

    @Override
    public Set<RValue> getUses() {
        return Set.of(exceptionRef);
    }

    @Override
    public boolean canFallThrough() {
        return false;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "throw " + exceptionRef;
    }
}
