

package keeno.usap.util.graph;

import keeno.usap.util.collection.Views;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * Representation of a directed graph.
 *
 * @param <N> type of nodes
 */
public interface Graph<N> extends Iterable<N>, Serializable {

    /**
     * @return {@code true} if this graph has given node, otherwise {@code false}.
     */
    default boolean hasNode(N node) {
        return getNodes().contains(node);
    }

    /**
     * @return {@code true} if this graph has an edge from given source to target,
     * otherwise {@code false}.
     */
    default boolean hasEdge(N source, N target) {
        return getSuccsOf(source).contains(target);
    }

    /**
     * @return true if this graph has the given edge, otherwise false.
     */
    default boolean hasEdge(Edge<N> edge) {
        return hasEdge(edge.source(), edge.target());
    }

    /**
     * @return the predecessors of given node in this graph.
     */
    Set<N> getPredsOf(N node);

    /**
     * @return the successors of given node in this graph.
     */
    Set<N> getSuccsOf(N node);

    /**
     * @return incoming edges of the given node.
     */
    default Set<? extends Edge<N>> getInEdgesOf(N node) {
        return Views.toMappedSet(getPredsOf(node),
                pred -> new SimpleEdge<>(pred, node));
    }

    /**
     * @return the number of in edges of the given node.
     */
    default int getInDegreeOf(N node) {
        return getInEdgesOf(node).size();
    }

    /**
     * @return outgoing edges of the given node.
     */
    default Set<? extends Edge<N>> getOutEdgesOf(N node) {
        return Views.toMappedSet(getSuccsOf(node),
                succ -> new SimpleEdge<>(node, succ));
    }

    /**
     * @return the number of out edges of the given node.
     */
    default int getOutDegreeOf(N node) {
        return getOutEdgesOf(node).size();
    }

    /**
     * @return all nodes of this graph.
     */
    Set<N> getNodes();

    /**
     * @return the number of the nodes in this graph.
     */
    default int getNumberOfNodes() {
        return getNodes().size();
    }

    @Override
    default Iterator<N> iterator() {
        return getNodes().iterator();
    }
}
