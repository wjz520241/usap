

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.RValue;
import keeno.usap.ir.exp.Var;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Representation of return statement, e.g., return; or return x.
 */
public class Return extends AbstractStmt {

    @Nullable
    private final Var value;

    public Return(@Nullable Var value) {
        this.value = value;
    }

    public Return() {
        this(null);
    }

    @Nullable
    public Var getValue() {
        return value;
    }

    @Override
    public Set<RValue> getUses() {
        return value != null ? Set.of(value) : Set.of();
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
        return value != null ? "return " + value : "return";
    }
}
