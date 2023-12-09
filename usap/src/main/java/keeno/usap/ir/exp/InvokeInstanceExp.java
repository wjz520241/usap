

package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.util.collection.ArraySet;

import java.util.List;
import java.util.Set;

/**
 * Representation of instance invocation (virtual, interface,
 * and special) expression.
 */
public abstract class InvokeInstanceExp extends InvokeExp {

    protected final Var base;

    protected InvokeInstanceExp(MethodRef methodRef, Var base, List<Var> args) {
        super(methodRef, args);
        this.base = base;
    }

    public Var getBase() {
        return base;
    }

    @Override
    public Set<RValue> getUses() {
        Set<RValue> uses = new ArraySet<>(args.size() + 1);
        uses.add(base);
        uses.addAll(args);
        return uses;
    }

    @Override
    public String toString() {
        return String.format("%s %s.%s%s", getInvokeString(),
                base.getName(), methodRef.getName(), getArgsString());
    }
}
