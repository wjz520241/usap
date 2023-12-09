

package keeno.usap.analysis.graph.flowgraph;

import keeno.usap.util.Indexable;

/**
 * Nodes in object flow graph.
 */
public abstract class Node implements Indexable {

    private final int index;

    Node(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
