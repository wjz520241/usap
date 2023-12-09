

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;
import keeno.usap.util.collection.ArraySet;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

/**
 * Representation of assign statements.
 *
 * @param <L> type of lvalue.
 * @param <R> type of rvalue.
 */
public abstract class AssignStmt<L extends LValue, R extends RValue>
        extends DefinitionStmt<L, R> {

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
    public Set<RValue> getUses() {
        Set<RValue> lUses = lvalue.getUses();
        Set<RValue> rUses = rvalue.getUses();
        Set<RValue> uses = new ArraySet<>(lUses.size() + rUses.size() + 1);
        uses.addAll(lUses);
        uses.addAll(rUses);
        uses.add(rvalue);
        return uses;
    }

    @Override
    public String toString() {
        return lvalue + " = " + rvalue;
    }
}
