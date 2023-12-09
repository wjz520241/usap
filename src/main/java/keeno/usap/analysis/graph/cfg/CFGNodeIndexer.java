

package keeno.usap.analysis.graph.cfg;

import keeno.usap.util.Indexer;

/**
 * Indexer for nodes in a CFG.
 *
 * @param <Node> type of CFG nodes.
 */
public record CFGNodeIndexer<Node>(CFG<Node> cfg) implements Indexer<Node> {

    @Override
    public int getIndex(Node node) {
        return cfg.getIndex(node);
    }

    @Override
    public Node getObject(int index) {
        return cfg.getNode(index);
    }
}
