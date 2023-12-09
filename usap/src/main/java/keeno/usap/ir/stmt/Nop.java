

package keeno.usap.ir.stmt;

/**
 * Representation of nop statement which does nothing.
 */
public class Nop extends AbstractStmt {

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "nop";
    }
}
