

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

    // 以下三种方法为三个实现的API（在Stmt中声明）提供默认行为。
    // 如果这些API的行为与默认行为不同，则此类的子类应覆盖这些API

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
