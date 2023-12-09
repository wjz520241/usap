

package keeno.usap.util.graph;

import java.io.Serializable;

/**
 * General interface for graph edges.
 *
 * @param <N> type of nodes
 */
public interface Edge<N> extends Serializable {

    /**
     * @return the source node of the edge.
     */
    N source();

    /**
     * @return the target node of the edge.
     */
    N target();
}
