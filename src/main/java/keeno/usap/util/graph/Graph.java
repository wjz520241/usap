

package keeno.usap.util.graph;

import keeno.usap.util.collection.Views;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * 有向图
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
     * @return 前驱结点
     */
    Set<N> getPredsOf(N node);

    /**
     * @return 后继结点
     */
    Set<N> getSuccsOf(N node);

    /**
     * @return 入度
     */
    default Set<? extends Edge<N>> getInEdgesOf(N node) {
        return Views.toMappedSet(getPredsOf(node),
                pred -> new SimpleEdge<>(pred, node));
    }

    /**
     * @return 入度的边数
     */
    default int getInDegreeOf(N node) {
        return getInEdgesOf(node).size();
    }

    /**
     * @return 出度
     */
    default Set<? extends Edge<N>> getOutEdgesOf(N node) {
        return Views.toMappedSet(getSuccsOf(node),
                succ -> new SimpleEdge<>(node, succ));
    }

    /**
     * @return 出度的边数
     */
    default int getOutDegreeOf(N node) {
        return getOutEdgesOf(node).size();
    }

    /**
     * @return 该图的所有节点。
     */
    Set<N> getNodes();

    /**
     * @return 此图中的节点数。
     */
    default int getNumberOfNodes() {
        return getNodes().size();
    }

    @Override
    default Iterator<N> iterator() {
        return getNodes().iterator();
    }
}
