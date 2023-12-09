

package keeno.usap.analysis.pta.toolkit.zipper;

import keeno.usap.analysis.graph.flowgraph.FlowEdge;
import keeno.usap.analysis.graph.flowgraph.FlowKind;
import keeno.usap.analysis.graph.flowgraph.Node;

record UnwrappedFlowEdge(Node source, Node target) implements FlowEdge {

    @Override
    public FlowKind kind() {
        return FlowKind.OTHER;
    }
}
