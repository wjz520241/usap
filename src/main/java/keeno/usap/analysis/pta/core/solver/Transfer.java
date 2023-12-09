

package keeno.usap.analysis.pta.core.solver;

import keeno.usap.analysis.pta.pts.PointsToSet;

/**
 * Transfer function on pointer flow edges.
 * For a given pointer flow edge "source" -> "target", the function defines
 * how the points-to facts of "source" node are propagated to the "target" node.
 */
@FunctionalInterface
public interface Transfer {

    /**
     * Transfer function on a pointer flow edge.
     *
     * @param edge  the pointer flow edge being transferred.
     * @param input set of objects pointed to by the "source" node.
     * @return set of objects that are propagated to the "target" node.
     */
    PointsToSet apply(PointerFlowEdge edge, PointsToSet input);
}
