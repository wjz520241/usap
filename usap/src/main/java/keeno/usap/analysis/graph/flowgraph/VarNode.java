

package keeno.usap.analysis.graph.flowgraph;

import keeno.usap.ir.exp.Var;

public class VarNode extends Node {

    private final Var var;

    VarNode(Var var, int index) {
        super(index);
        this.var = var;
    }

    public Var getVar() {
        return var;
    }

    @Override
    public String toString() {
        return "VarNode{" + var.getMethod() + "/" + var.getName() + "}";
    }
}
