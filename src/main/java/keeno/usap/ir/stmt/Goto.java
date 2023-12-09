

package keeno.usap.ir.stmt;

import java.util.List;

/**
 * Representation of goto statement, e.g., goto L.
 */
public class Goto extends JumpStmt {

    private Stmt target;

    public Stmt getTarget() {
        return target;
    }

    public void setTarget(Stmt target) {
        this.target = target;
    }

    @Override
    public boolean canFallThrough() {
        return false;
    }

    @Override
    public List<Stmt> getTargets() {
        return List.of(target);
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "goto " + toString(target);
    }
}
