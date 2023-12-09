

package keeno.usap.analysis.graph.flowgraph;

import keeno.usap.analysis.graph.callgraph.CallGraph;
import keeno.usap.analysis.graph.callgraph.CallKind;
import keeno.usap.analysis.pta.core.cs.element.ArrayIndex;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.cs.element.InstanceField;
import keeno.usap.analysis.pta.core.cs.element.Pointer;
import keeno.usap.analysis.pta.core.cs.element.StaticField;
import keeno.usap.analysis.pta.core.solver.PointerFlowGraph;
import keeno.usap.ir.exp.InvokeInstanceExp;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.Indexer;
import keeno.usap.util.collection.IndexMap;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.Views;
import keeno.usap.util.graph.Graph;

import java.util.Set;

public class ObjectFlowGraph extends NodeManager
        implements Graph<Node>, Indexer<Node> {

    private final MultiMap<Node, FlowEdge> inEdges = Maps.newMultiMap(
            new IndexMap<>(this, 4096));

    private final MultiMap<Node, FlowEdge> outEdges = Maps.newMultiMap(
            new IndexMap<>(this, 4096));

    public ObjectFlowGraph(PointerFlowGraph pfg,
                           CallGraph<Invoke, JMethod> callGraph) {
        pfg.pointers().forEach(pointer -> {
            toNode(pointer); // ensure every pointer has a corresponding node
            pfg.getOutEdgesOf(pointer).forEach(e ->
                    addEdge(e.kind(), toNode(e.source()), toNode(e.target())));
        });
        // This-passing edges are absent on PFG, so we iterate call graph edges
        // to complement this kind of edges.
        callGraph.edges()
                .forEach(e -> {
                    // Currently ignore OTHER (e.g., reflective) call edges
                    if (e.getKind() != CallKind.OTHER &&
                            e.getCallSite().getInvokeExp() instanceof
                                    InvokeInstanceExp invokeExp) {
                        Var base = invokeExp.getBase();
                        Var thisVar = e.getCallee().getIR().getThis();
                        addEdge(FlowKind.THIS_PASSING,
                                getOrCreateVarNode(base),
                                getOrCreateVarNode(thisVar));
                    }
                });
    }

    private void addEdge(FlowKind kind, Node source, Node target) {
        BasicFlowEdge edge = new BasicFlowEdge(kind, source, target);
        outEdges.put(source, edge);
        inEdges.put(target, edge);
    }

    /**
     * Converts given pointer to a node in this OFG.
     */
    private Node toNode(Pointer pointer) {
        if (pointer instanceof CSVar csVar) {
            return getOrCreateVarNode(csVar.getVar());
        } else if (pointer instanceof InstanceField iField) {
            return getOrCreateInstanceFieldNode(
                    iField.getBase().getObject(), iField.getField());
        } else if (pointer instanceof ArrayIndex arrayIndex) {
            return getOrCreateArrayIndexNode(
                    arrayIndex.getArray().getObject());
        } else {
            return getOrCreateStaticFieldNode(
                    ((StaticField) pointer).getField());
        }
    }

    @Override
    public Set<Node> getPredsOf(Node node) {
        return Views.toMappedSet(getInEdgesOf(node), FlowEdge::source);
    }

    @Override
    public Set<FlowEdge> getInEdgesOf(Node node) {
        return inEdges.get(node);
    }

    @Override
    public Set<Node> getSuccsOf(Node node) {
        return Views.toMappedSet(getOutEdgesOf(node), FlowEdge::target);
    }

    @Override
    public Set<FlowEdge> getOutEdgesOf(Node node) {
        return outEdges.get(node);
    }
}
