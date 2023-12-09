

package keeno.usap.analysis.graph.flowgraph;

import keeno.usap.util.graph.Edge;

/**
 * Represents edges in flow graph.
 */
public interface FlowEdge extends Edge<Node> {

    FlowKind kind();
}
