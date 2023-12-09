

package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.MethodRef;

import java.util.List;

/**
 * Representation of invokespecial expression, e.g., super.m(...).
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
