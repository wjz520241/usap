package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.language.Type;
import soot.util.ArraySet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class InvokeExp implements RValue {
    protected final MethodRef methodRef;

    protected final List<Var> args;

    /**
     * @param methodRef 方法的签名信息
     * @param args      实参
     */
    protected InvokeExp(MethodRef methodRef, List<Var> args) {
        this.methodRef = methodRef;
        this.args = List.copyOf(args);
    }

    @Override
    public Type getType() {
        return methodRef.getReturnType();
    }

    public MethodRef getMethodRef() {
        return methodRef;
    }

    public int getArgCount() {
        return args.size();
    }

    /**
     * @return 获取第i个参数，注意索引范围
     */
    public Var getArg(int i) {
        return args.get(i);
    }

    public List<Var> getArgs() {
        return args;
    }

    public abstract String getInvokeString();

    public String getArgsString() {
        return "(" + args.stream()
                .map(Var::toString)
                .collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public Set<RValue> getUses() {
        Set<RValue> arraySet = new ArraySet<>(args.size());
        arraySet.addAll(args);
        return arraySet;
    }
}
