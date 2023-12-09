

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.ir.exp.Var;
import keeno.usap.language.type.Type;

/**
 * Represents context-sensitive variables.
 */
public class CSVar extends AbstractPointer implements CSElement {

    private final Var var;

    private final Context context;

    CSVar(Var var, Context context, int index) {
        super(index);
        this.var = var;
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    /**
     * @return the variable (without context).
     */
    public Var getVar() {
        return var;
    }

    @Override
    public Type getType() {
        return var.getType();
    }

    @Override
    public String toString() {
        return context + ":" + var.getMethod() + "/" + var.getName();
    }
}
