

package keeno.usap.util.graph;

import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.Sets;

import java.util.Collections;
import java.util.Set;

/**
 * A simple map-based implementation of {@link Graph}.
 *
 * @param <N> type of nodes
 */
public class SimpleGraph<N> implements Graph<N> {

    private final Set<N> nodes = Sets.newSet();

    private final MultiMap<N, N> preds = Maps.newMultiMap();

    private final MultiMap<N, N> succs = Maps.newMultiMap();

    /**
     * Constructs an empty graph.
     */
    public SimpleGraph() {
    }

    /**
     * Constructs a new graph containing the same node and edge sets
     * as the specified graph.
     */
    public SimpleGraph(Graph<N> graph) {
        for (N node : graph) {
            addNode(node);
            for (N succ : graph.getSuccsOf(node)) {
                addEdge(node, succ);
            }
        }
    }

    public void addNode(N node) {
        nodes.add(node);
    }

    public void addEdge(N source, N target) {
        nodes.add(source);
        nodes.add(target);
        preds.put(target, source);
        succs.put(source, target);
    }

    /**
     * Removes a node from this graph.
     * All edges from/to the node will also be removed.
     */
    public void removeNode(N node) {
        nodes.remove(node);
        preds.removeAll(node);
        succs.removeAll(node);
    }

    /**
     * Removes an edge from this graph.
     */
    public void removeEdge(N source, N target) {
        preds.remove(target, source);
        succs.remove(source, target);
    }

    @Override
    public Set<N> getPredsOf(N node) {
        return preds.get(node);
    }

    @Override
    public Set<N> getSuccsOf(N node) {
        return succs.get(node);
    }

    @Override
    public int getInDegreeOf(N node) {
        return preds.get(node).size();
    }

    @Override
    public Set<N> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }
}
