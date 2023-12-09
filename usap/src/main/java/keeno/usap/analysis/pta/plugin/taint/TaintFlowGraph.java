

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.graph.flowgraph.FlowEdge;
import keeno.usap.analysis.graph.flowgraph.Node;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.Sets;
import keeno.usap.util.collection.Views;
import keeno.usap.util.graph.Graph;

import java.util.Set;

class TaintFlowGraph implements Graph<Node> {

    private final Set<Node> sourceNodes;

    private final Set<Node> sinkNodes;

    private final Set<Node> nodes = Sets.newHybridSet();

    private final MultiMap<Node, FlowEdge> inEdges = Maps.newMultiMap();

    private final MultiMap<Node, FlowEdge> outEdges = Maps.newMultiMap();

    TaintFlowGraph(Set<Node> sourceNodes, Set<Node> sinkNodes) {
        this.sourceNodes = Set.copyOf(sourceNodes);
        nodes.addAll(sourceNodes);
        this.sinkNodes = Set.copyOf(sinkNodes);
        nodes.addAll(sinkNodes);
    }

    Set<Node> getSourceNodes() {
        return sourceNodes;
    }

    Set<Node> getSinkNodes() {
        return sinkNodes;
    }

    void addEdge(FlowEdge edge) {
        nodes.add(edge.source());
        nodes.add(edge.target());
        inEdges.put(edge.target(), edge);
        outEdges.put(edge.source(), edge);
    }

    @Override
    public Set<Node> getPredsOf(Node node) {
        return Views.toMappedSet(getInEdgesOf(node), FlowEdge::source);
    }

    @Override
    public Set<FlowEdge> getInEdgesOf(Node node) {
        return inEdges.get(node);
    }

    @Override
    public Set<Node> getSuccsOf(Node node) {
        return Views.toMappedSet(getOutEdgesOf(node), FlowEdge::target);
    }

    @Override
    public Set<FlowEdge> getOutEdgesOf(Node node) {
        return outEdges.get(node);
    }

    @Override
    public Set<Node> getNodes() {
        return nodes;
    }
}
