

package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.MethodRef;

import java.util.List;

/**
 * 参阅doc目录中的《方法调用图》, super.m(...).
 */
public class InvokeSpecial extends InvokeInstanceExp {

    public InvokeSpecial(MethodRef methodRef, Var base, List<Var> args) {
        super(methodRef, base, args);
    }

    @Override
    public String getInvokeString() {
        return "invokespecial";
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
