package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;

import java.util.Optional;
import java.util.Set;

public abstract class AbstractStmt implements Stmt {

    protected int index = -1;
    protected int lineNumber = -1;

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        if (index != -1) {
            throw new IllegalStateException("索引已设置");
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

    @Override
    public Optional<LValue> getDef() {
        return Optional.empty();
    }

    @Override
    public Set<RValue> getUse() {
        return Set.of();
    }
}
