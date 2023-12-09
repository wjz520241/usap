

package keeno.usap.analysis.graph.icfg;

import keeno.usap.analysis.graph.cfg.CFGEdge;

/**
 * The edge connecting nodes in the same method.
 * Note that This kind of edges does not include the edges from call sites
 * to their return sites, which are represented by {@link CallToReturnEdge}.
 *
 * @param <Node> type of nodes
 */
public class NormalEdge<Node> extends ICFGEdge<Node> {

    /**
     * The corresponding CFG edge, which brings the information of edge type.
     */
    private final CFGEdge<Node> cfgEdge;

    NormalEdge(CFGEdge<Node> edge) {
        super(edge.source(), edge.target());
        this.cfgEdge = edge;
    }

    public CFGEdge<Node> getCFGEdge() {
        return cfgEdge;
    }
}
