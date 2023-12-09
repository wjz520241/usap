

package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.MethodRef;

import java.util.List;

/**
 * Representation of invokevirtual expression, e.g., o.m(...).
 */
public class InvokeVirtual extends InvokeInstanceExp {

    public InvokeVirtual(MethodRef methodRef, Var base, List<Var> args) {
        super(methodRef, base, args);
    }

    @Override
    public String getInvokeString() {
        return "invokevirtual";
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
