

package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.MethodRef;

import java.util.List;

/**
 * 参阅doc目录中的《方法调用图》。 o.m(...).
 */
public class InvokeInterface extends InvokeInstanceExp {

    public InvokeInterface(MethodRef methodRef, Var base, List<Var> args) {
        super(methodRef, base, args);
    }

    @Override
    public String getInvokeString() {
        return "invokeinterface";
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
