

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.AbstractResultHolder;
import keeno.usap.util.Indexable;
import keeno.usap.util.ResultHolder;
import keeno.usap.util.collection.ArraySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 上下文敏感方法
 */
public class CSMethod extends AbstractCSElement implements Indexable {

    private final JMethod method;

    private final int index;

    /**
     * Call edges to this CS method.
     */
    private final ArrayList<Edge<CSCallSite, CSMethod>> edges = new ArrayList<>(4);

    private final ResultHolder resultHolder = new AbstractResultHolder() {};

    CSMethod(JMethod method, Context context, int index) {
        super(context);
        this.method = method;
        this.index = index;
    }

    /**
     * @return the method (without context).
     */
    public JMethod getMethod() {
        return method;
    }

    public void addEdge(Edge<CSCallSite, CSMethod> edge) {
        // 调用方已确保添加到CSMethod的每条边都是唯一的
        edges.add(edge);
    }

    public Set<Edge<CSCallSite, CSMethod>> getEdges() {
        return Collections.unmodifiableSet(new ArraySet<>(edges, true));
    }

    public <R> R getResult(String id, Supplier<R> supplier) {
        return resultHolder.getResult(id, supplier);
    }

    public <R> Optional<R> getResult(String id) {
        return Optional.ofNullable(resultHolder.getResult(id));
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return context + ":" + method;
    }
}
