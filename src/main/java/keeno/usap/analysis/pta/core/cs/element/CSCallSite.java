

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.util.collection.ArraySet;
import keeno.usap.util.collection.HybridIndexableSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * 上下文敏感的调用点
 */
public class CSCallSite extends AbstractCSElement {

    private final Invoke callSite;

    /**
     * Context-sensitive method which contains this CS call site.
     */
    private final CSMethod container;

    private final Set<CSMethod> callees = new HybridIndexableSet<>(true);

    /**
     * Call edges from this call site.
     */
    private final ArrayList<Edge<CSCallSite, CSMethod>> edges = new ArrayList<>(4);

    CSCallSite(Invoke callSite, Context context, CSMethod container) {
        super(context);
        this.callSite = callSite;
        this.container = container;
    }

    /**
     * @return the call site (without context).
     */
    public Invoke getCallSite() {
        return callSite;
    }

    public CSMethod getContainer() {
        return container;
    }

    public boolean addEdge(Edge<CSCallSite, CSMethod> edge) {
        if (callees.add(edge.getCallee())) {
            return edges.add(edge);
        }
        return false;
    }

    public Set<Edge<CSCallSite, CSMethod>> getEdges() {
        return Collections.unmodifiableSet(new ArraySet<>(edges, true));
    }

    @Override
    public String toString() {
        return context + ":" + callSite;
    }
}
