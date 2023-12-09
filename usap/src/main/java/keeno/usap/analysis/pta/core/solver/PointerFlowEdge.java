

package keeno.usap.analysis.pta.core.solver;

import keeno.usap.analysis.graph.flowgraph.FlowKind;
import keeno.usap.analysis.pta.core.cs.element.Pointer;
import keeno.usap.util.Hashes;
import keeno.usap.util.collection.Sets;
import keeno.usap.util.graph.Edge;

import java.util.Set;

public class PointerFlowEdge implements Edge<Pointer> {

    private final FlowKind kind;

    private final Pointer source;

    private final Pointer target;

    private final Set<Transfer> transfers = Sets.newHybridSet();

    public PointerFlowEdge(FlowKind kind, Pointer source, Pointer target) {
        this.kind = kind;
        this.source = source;
        this.target = target;
    }

    public FlowKind kind() {
        return kind;
    }

    public Pointer source() {
        return source;
    }

    public Pointer target() {
        return target;
    }

    public boolean addTransfer(Transfer transfer) {
        return transfers.add(transfer);
    }

    public Set<Transfer> getTransfers() {
        return transfers;
    }

    @Override
    public int hashCode() {
        return Hashes.hash(source, target);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PointerFlowEdge that = (PointerFlowEdge) o;
        return source.equals(that.source) && target.equals(that.target);
    }

    @Override
    public String toString() {
        return "[" + kind + "]" + source + " -> " + target;
    }
}
