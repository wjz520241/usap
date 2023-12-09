

package keeno.usap.analysis.pta.plugin.util;

import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;

/**
 * Dummy model which does nothing.
 */
public enum DummyModel implements Model {

    INSTANCE;

    public static Model get() {
        return INSTANCE;
    }

    @Override
    public void handleNewInvoke(Invoke invoke) {
    }

    @Override
    public boolean isRelevantVar(Var var) {
        return false;
    }

    @Override
    public void handleNewPointsToSet(CSVar csVar, PointsToSet pts) {
    }
}
