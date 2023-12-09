

package keeno.usap.analysis.pta.core.solver;

import keeno.usap.analysis.graph.flowgraph.FlowKind;
import keeno.usap.analysis.pta.core.cs.element.CSManager;
import keeno.usap.analysis.pta.core.cs.element.Pointer;
import keeno.usap.util.collection.Views;
import keeno.usap.util.graph.Edge;
import keeno.usap.util.graph.Graph;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents pointer flow graph in context-sensitive pointer analysis.
 */
public class PointerFlowGraph implements Graph<Pointer> {

    private final CSManager csManager;

    PointerFlowGraph(CSManager csManager) {
        this.csManager = csManager;
    }

    /**
     * Adds a pointer flow edge {@code source} -> {@code target}, and
     * returns the edge. If the edge already exists and {@code kind}
     * is not {@link FlowKind#OTHER}, {@code null} is returned.
     */
    public PointerFlowEdge getOrAddEdge(FlowKind kind, Pointer source, Pointer target) {
        return source.getOrAddEdge(kind, source, target);
    }

    @Override
    public Set<? extends Edge<Pointer>> getInEdgesOf(Pointer node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<PointerFlowEdge> getOutEdgesOf(Pointer pointer) {
        return pointer.getOutEdges();
    }

    public Stream<Pointer> pointers() {
        return csManager.pointers();
    }

    @Override
    public Set<Pointer> getPredsOf(Pointer node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Pointer> getSuccsOf(Pointer node) {
        return Views.toMappedSet(node.getOutEdges(),
                PointerFlowEdge::target);
    }

    @Override
    public Set<Pointer> getNodes() {
        return pointers().collect(Collectors.toUnmodifiableSet());
    }
}
