

package keeno.usap.analysis.dataflow.analysis;

import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.analysis.graph.cfg.CFGEdge;

/**
 * Template interface for defining data-flow analysis.
 *
 * @param <Node> type of CFG nodes
 * @param <Fact> type of data-flow facts
 */
public interface DataflowAnalysis<Node, Fact> {

    /**
     * @return true if this analysis is forward, otherwise false.
     */
    boolean isForward();

    /**
     * @return new fact in boundary conditions, i.e., the fact for
     * entry (exit) node in forward (backward) analysis.
     */
    Fact newBoundaryFact();

    /**
     * @return new initial fact for non-boundary nodes.
     */
    Fact newInitialFact();

    /**
     * Meets a fact into another (target) fact.
     * This function will be used to handle control-flow confluences.
     */
    void meetInto(Fact fact, Fact target);

    /**
     * Node Transfer function for the analysis.
     * The function transfers data-flow from in (out) fact to out (in) fact
     * for forward (backward) analysis.
     *
     * @return true if the transfer changed the out (in) fact, otherwise false.
     */
    boolean transferNode(Node node, Fact in, Fact out);

    /**
     * @return true if this analysis needs to perform transfer for given edge, otherwise false.
     */
    boolean needTransferEdge(CFGEdge<Node> edge);

    /**
     * Edge Transfer function for this analysis.
     * Note that this function should NOT modify {@code nodeFact}.
     *
     * @param edge     the edge that the transfer function is applied on
     * @param nodeFact the fact of the source node of the edge. Note that
     *                 which node is the source node of an edge depends on
     *                 the direction of the analysis.
     * @return the resulting edge fact
     */
    Fact transferEdge(CFGEdge<Node> edge, Fact nodeFact);

    /**
     * @return the control-flow graph that this analysis works on.
     */
    CFG<Node> getCFG();

}
