

package keeno.usap.util.graph;

import keeno.usap.util.collection.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Represents a node in {@link MergedSCCGraph}, where each node
 * corresponds to a SCC.
 *
 * @param <N> type of nodes
 */
public class MergedNode<N> {

    private final List<N> nodes;

    private final Set<MergedNode<N>> preds = Sets.newHybridSet();

    private final Set<MergedNode<N>> succs = Sets.newHybridSet();

    MergedNode(Collection<N> nodes) {
        assert !nodes.isEmpty();
        this.nodes = List.copyOf(nodes);
    }

    void addPred(MergedNode<N> pred) {
        preds.add(pred);
    }

    Set<MergedNode<N>> getPreds() {
        return preds;
    }

    void addSucc(MergedNode<N> succ) {
        succs.add(succ);
    }

    Set<MergedNode<N>> getSuccs() {
        return succs;
    }

    public List<N> getNodes() {
        return nodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MergedNode<?> that = (MergedNode<?>) o;
        return nodes.equals(that.nodes);
    }

    @Override
    public int hashCode() {
        return nodes.hashCode();
    }

    @Override
    public String toString() {
        return "MergedNode" + nodes;
    }
}
