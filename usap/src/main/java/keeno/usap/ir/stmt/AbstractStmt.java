

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;

import java.util.Optional;
import java.util.Set;

abstract class AbstractStmt implements Stmt {

    protected int index = -1;

    protected int lineNumber = -1;

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        if (this.index != -1) {
            throw new IllegalStateException("index already set");
        }
        this.index = index;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    // Following three methods provide default behaviors for the three
    // implemented APIs (declared in Stmt). The subclasses of this class
    // should override these APIs iff their behaviors are different from
    // the default ones.

    @Override
    public Optional<LValue> getDef() {
        return Optional.empty();
    }

    @Override
    public Set<RValue> getUses() {
        return Set.of();
    }

    @Override
    public boolean canFallThrough() {
        return true;
    }
}
