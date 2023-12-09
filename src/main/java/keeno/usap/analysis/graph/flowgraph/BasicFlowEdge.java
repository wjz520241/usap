

package keeno.usap.analysis.graph.flowgraph;

/**
 * Represents basic (non-OTHER) edges in flow graph.
 */
record BasicFlowEdge(FlowKind kind, Node source, Node target)
        implements FlowEdge {
}
