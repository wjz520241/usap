

package keeno.usap.util.graph;

import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.Sets;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents a merged graph of a directed graph G.
 * Each SCC of G is represented by a merged node of this graph.
 *
 * @see MergedNode
 */
public class MergedSCCGraph<N> implements Graph<MergedNode<N>> {

    private Set<MergedNode<N>> nodes;

    public MergedSCCGraph(Graph<N> graph) {
        init(graph);
    }

    private void init(Graph<N> graph) {
        nodes = Sets.newSet();
        // Map from original node to the corresponding merged node.
        Map<N, MergedNode<N>> nodeMap = Maps.newMap(graph.getNumberOfNodes());
        SCC<N> scc = new SCC<>(graph);
        scc.getComponents().forEach(component -> {
            MergedNode<N> node = new MergedNode<>(component);
            component.forEach(n -> nodeMap.put(n, node));
            nodes.add(node);
        });
        nodes.forEach(node -> node.getNodes()
                .stream()
                .flatMap(n -> graph.getSuccsOf(n).stream())
                .map(nodeMap::get)
                .filter(succ -> succ != node) // exclude self-loop
                .forEach(succ -> {
                    node.addSucc(succ);
                    succ.addPred(node);
                }));
    }

    @Override
    public Set<MergedNode<N>> getPredsOf(MergedNode<N> node) {
        return Collections.unmodifiableSet(node.getPreds());
    }

    @Override
    public Set<MergedNode<N>> getSuccsOf(MergedNode<N> node) {
        return Collections.unmodifiableSet(node.getSuccs());
    }

    @Override
    public Set<MergedNode<N>> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }
}
