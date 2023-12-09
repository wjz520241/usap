

package keeno.usap.analysis.pta.plugin;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.heap.Descriptor;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.ir.exp.NullLiteral;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.AssignLiteral;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.NullType;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

/**
 * Handles {@link AssignLiteral} var = null.
 */
public class NullHandler implements Plugin {

    private static final Descriptor NULL_DESC = () -> "NullObj";

    private final MultiMap<JMethod, Var> nullVars = Maps.newMultiMap();

    private Solver solver;

    private Obj nullObj;

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        nullObj = solver.getHeapModel().getMockObj(
                NULL_DESC, NullLiteral.get(), NullType.NULL, false);
    }

    @Override
    public void onNewStmt(Stmt stmt, JMethod container) {
        if (stmt instanceof AssignLiteral assign &&
                assign.getRValue() instanceof NullLiteral) {
            nullVars.put(container, assign.getLValue());
        }
    }

    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        Context ctx = csMethod.getContext();
        nullVars.get(csMethod.getMethod()).forEach(var ->
                solver.addVarPointsTo(ctx, var, nullObj));
    }
}
