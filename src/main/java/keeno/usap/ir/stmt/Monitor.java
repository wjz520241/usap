

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.RValue;
import keeno.usap.ir.exp.Var;

import java.util.Set;

/**
 * Representation of monitorenter/monitorexit instruction.
 */
public class Monitor extends AbstractStmt {

    public enum Op {
        ENTER("enter"), EXIT("exit");

        private final String name;

        Op(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Op op;

    /**
     * Reference of the object to be locked/unlocked.
     */
    private final Var objectRef;

    public Monitor(Op op, Var objectRef) {
        this.op = op;
        this.objectRef = objectRef;
    }

    public boolean isEnter() {
        return op == Op.ENTER;
    }

    public boolean isExit() {
        return op == Op.EXIT;
    }

    public Var getObjectRef() {
        return objectRef;
    }

    @Override
    public Set<RValue> getUses() {
        return Set.of(objectRef);
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "monitor" + op + " " + objectRef;
    }
}
