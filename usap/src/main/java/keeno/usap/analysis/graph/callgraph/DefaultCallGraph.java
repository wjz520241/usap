

package keeno.usap.analysis.graph.callgraph;

import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;

import java.util.Set;

/**
 * Default implementation of call graph.
 */
public class DefaultCallGraph extends AbstractCallGraph<Invoke, JMethod> {

    /**
     * Adds an entry method to this call graph.
     */
    public void addEntryMethod(JMethod entryMethod) {
        entryMethods.add(entryMethod);
    }

    /**
     * Adds a reachable method to this call graph.
     *
     * @return true if this call graph changed as a result of the call,
     * otherwise false.
     */
    public boolean addReachableMethod(JMethod method) {
        if (reachableMethods.add(method)) {
            if (!method.isAbstract()) {
                method.getIR().forEach(stmt -> {
                    if (stmt instanceof Invoke invoke) {
                        callSiteToContainer.put(invoke, method);
                        callSitesIn.put(method, invoke);
                    }
                });
            }
            return true;
        }
        return false;
    }

    /**
     * Adds a new call graph edge to this call graph.
     *
     * @param edge the call edge to be added
     * @return true if the call graph changed as a result of the call,
     * otherwise false.
     */
    public boolean addEdge(Edge<Invoke, JMethod> edge) {
        if (callSiteToEdges.put(edge.getCallSite(), edge)) {
            calleeToEdges.put(edge.getCallee(), edge);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public JMethod getContainerOf(Invoke invoke) {
        return invoke.getContainer();
    }

    @Override
    public boolean isRelevant(Stmt stmt) {
        return stmt instanceof Invoke;
    }

    @Override
    public Set<JMethod> getResult(Stmt stmt) {
        return getCalleesOf((Invoke) stmt);
    }
}
