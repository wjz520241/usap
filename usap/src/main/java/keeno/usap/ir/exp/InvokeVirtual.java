package keeno.usap.ir.exp;


import keeno.usap.ir.proginfo.MethodRef;

import java.util.List;

/**
 * 参阅doc目录中的《方法调用图》, o.m(...).
 */
public class InvokeVirtual extends InvokeInstanceExp {

    public InvokeVirtual(MethodRef methodRef, Var base, List<Var> args) {
        super(methodRef, base, args);
    }

    @Override
    public String getInvokeString() {
        return "invokevirtual";
    }

}
