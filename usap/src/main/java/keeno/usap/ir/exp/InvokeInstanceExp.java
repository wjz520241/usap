package keeno.usap.ir.exp;


import keeno.usap.ir.proginfo.MethodRef;
import soot.util.ArraySet;

import java.util.List;
import java.util.Set;

/**
 * （虚拟、接口和特殊）表达式的表示
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

    /**
     * @return 这个格式可以看出InvokeInstanceExp的整体的层次结构和设计思路
     */
    @Override
    public String toString() {
        return String.format("%s %s.%s%s", getInvokeString(),
                base.getName(), methodRef.getName(), getArgsString());
    }
}
