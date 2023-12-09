

package keeno.usap.ir.stmt;

import java.util.List;

public abstract class JumpStmt extends AbstractStmt {

    /**
     * @return possible jump targets of this statement.
     */
    public abstract List<Stmt> getTargets();

    /**
     * Convert a target statement to its String representation.
     */
    public String toString(Stmt target) {
        return target == null ?
                "[unknown]" : Integer.toString(target.getIndex());
    }
}
