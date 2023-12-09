

package keeno.usap.analysis.graph.icfg;

import keeno.usap.language.classes.JMethod;

/**
 * The edge connecting a call site to method entry of the callee.
 *
 * @param <Node> type of nodes
 */
public class CallEdge<Node> extends ICFGEdge<Node> {

    /**
     * Callee of the call edge.
     */
    private final JMethod callee;

    CallEdge(Node source, Node target, JMethod callee) {
        super(source, target);
        this.callee = callee;
    }

    /**
     * @return the callee of the call edge.
     */
    public JMethod getCallee() {
        return callee;
    }
}
