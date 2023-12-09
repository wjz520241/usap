

package keeno.usap.analysis.graph.icfg;

import keeno.usap.util.graph.AbstractEdge;

/**
 * Abstract class for ICFG edges.
 *
 * @param <Node> type of ICFG nodes
 * @see NormalEdge
 * @see CallToReturnEdge
 * @see CallEdge
 * @see ReturnEdge
 */
public abstract class ICFGEdge<Node> extends AbstractEdge<Node> {

    ICFGEdge(Node source, Node target) {
        super(source, target);
    }
}
