

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
 * 表示上下文敏感指针分析中的指针流图。
 */
public class PointerFlowGraph implements Graph<Pointer> {

    private final CSManager csManager;

    PointerFlowGraph(CSManager csManager) {
        this.csManager = csManager;
    }

    /**
     * 添加一个指针流图边 {@code source} -> {@code target}, 并返回这个边. 如果这个边已经存在且 {@code kind}
     * 不是 {@link FlowKind#OTHER}, 则返回 {@code null} .
     */
    public PointerFlowEdge getOrAddEdge(FlowKind kind, Pointer source, Pointer target) {
        return source.getOrAddEdge(kind, source, target);
    }

    /**
     * 如果你看过《软件分析》，你应该明白指针流图中的对象在算法中实际上是单向流动的
     */
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
