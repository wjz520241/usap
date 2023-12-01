package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;
import soot.util.ArraySet;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

/**
 * 表示Assign statement
 */
public abstract class AssignStmt<L extends LValue, R extends RValue> extends DefinitionStmt<L, R> {

    private final L lvalue;
    private final R rvalue;

    public AssignStmt(L lvalue, R rvalue) {
        this.lvalue = lvalue;
        this.rvalue = rvalue;
    }

    @Override
    @Nonnull
    public L getLValue() {
        return lvalue;
    }

    @Override
    public R getRValue() {
        return rvalue;
    }

    @Override
    public Optional<LValue> getDef() {
        return Optional.of(lvalue);
    }

    @Override
    public Set<RValue> getUse() {
        Set<RValue> lvalueUse = lvalue.getUses();
        Set<RValue> rvalueUse = rvalue.getUses();
        Set<RValue> arraySet = new ArraySet<>(lvalueUse.size() + rvalueUse.size() + 1);
        arraySet.addAll(lvalueUse);
        arraySet.addAll(rvalueUse);
        arraySet.add(rvalue);
        return arraySet;
    }

    @Override
    public String toString() {
        return lvalue + " = " + rvalue;
    }
}
