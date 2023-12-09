

package keeno.usap.util.graph;

import java.util.Set;

/**
 * A reverse view of given graph.
 *
 * @param <N> type of nodes.
 */
public class ReverseGraph<N> implements Graph<N> {

    private final Graph<N> graph;

    public ReverseGraph(Graph<N> graph) {
        this.graph = graph;
    }

    @Override
    public boolean hasEdge(N source, N target) {
        return graph.hasEdge(target, source);
    }

    @Override
    public Set<N> getPredsOf(N node) {
        return graph.getSuccsOf(node);
    }

    @Override
    public Set<N> getSuccsOf(N node) {
        return graph.getPredsOf(node);
    }

    @Override
    public Set<N> getNodes() {
        return graph.getNodes();
    }
}
