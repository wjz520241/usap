

package keeno.usap.analysis.pta.plugin;

import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;

/**
 * Analysis plugin interface.
 * <p>
 * This interface contains callbacks for pointer analysis events.
 * It is supposed to provide a mechanism for extending functionalities
 * of the analysis, so its implementations may have side effects
 * on pointer analysis.
 */
public interface Plugin {

    /**
     * Sets pointer analysis solver which will be used later by the plugin.
     */
    default void setSolver(Solver solver) {
    }

    /**
     * Invoked when pointer analysis starts.
     */
    default void onStart() {
    }

    /**
     * Invoked when pointer analysis finishes.
     * Pointer analysis is supposed to have been finished at this stage,
     * thus this call back should NOT modify pointer analysis results.
     */
    default void onFinish() {
    }

    /**
     * Invoked when set of new objects flow to a context-sensitive variable.
     *
     * @param csVar variable whose points-to set changes
     * @param pts   set of new objects
     */
    default void onNewPointsToSet(CSVar csVar, PointsToSet pts) {
    }

    /**
     * Invoked when a new call graph edge is discovered.
     *
     * @param edge new call graph edge
     */
    default void onNewCallEdge(Edge<CSCallSite, CSMethod> edge) {
    }

    /**
     * Invoked when a new reachable method is discovered.
     *
     * @param method new reachable method
     */
    default void onNewMethod(JMethod method) {
    }

    /**
     * Invoked when a new reachable stmt is discovered.
     *
     * @param stmt      new reachable stmt
     * @param container container method of {@code stmt}
     */
    default void onNewStmt(Stmt stmt, JMethod container) {
    }

    /**
     * Invoked when a new reachable context-sensitive method is discovered.
     *
     * @param csMethod new reachable context-sensitive method
     */
    default void onNewCSMethod(CSMethod csMethod) {
    }

    /**
     * Invoked when pointer analysis failed to resolve callee (i.e., resolve
     * to null) on a receiver object. Some plugins take over such cases to
     * do their analyses.
     *
     * @param recv    the receiver object
     * @param context the context of the invocation
     * @param invoke  the invocation site
     */
    default void onUnresolvedCall(CSObj recv, Context context, Invoke invoke) {
    }
}
