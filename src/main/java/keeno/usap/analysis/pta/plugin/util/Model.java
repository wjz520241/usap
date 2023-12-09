

package keeno.usap.analysis.pta.plugin.util;

import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;

/**
 * Model for special APIs.
 */
public interface Model {

    void handleNewInvoke(Invoke invoke);

    boolean isRelevantVar(Var var);

    void handleNewPointsToSet(CSVar csVar, PointsToSet pts);
}
