

package keeno.usap.analysis.pta.plugin.reflection;

import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.ir.stmt.Stmt;

/**
 * Dummy inference model that does nothing.
 */
class DummyModel extends InferenceModel {

    DummyModel(Solver solver) {
        super(solver, null, null);
    }

    @Override
    protected void handleNewNonInvokeStmt(Stmt stmt) {
    }
}
