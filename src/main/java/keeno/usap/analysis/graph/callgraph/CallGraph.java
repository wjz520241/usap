

package keeno.usap.analysis.graph.callgraph;

import keeno.usap.analysis.StmtResult;
import keeno.usap.util.graph.Graph;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Representation of call graph.
 *
 * @param <CallSite> type of call sites
 * @param <Method>   type of methods
 */
public interface CallGraph<CallSite, Method>
        extends Graph<Method>, StmtResult<Set<Method>> {

    /**
     * @return 调用给定方法的调用点。
     */
    Set<CallSite> getCallersOf(Method callee);

    /**
     * @return 调用点调用的方法
     */
    Set<Method> getCalleesOf(CallSite callSite);

    /**
     * @return 由给定方法中的所有调用点调用的方法。
     */
    Set<Method> getCalleesOfM(Method caller);

    /**
     * @return 包含给定调用点的方法。
     */
    Method getContainerOf(CallSite callSite);

    /**
     * @return 给定方法中的调用点。
     */
    Set<CallSite> getCallSitesIn(Method method);

    /**
     * @return the call sites within the given method.
     */
    default Stream<CallSite> callSitesIn(Method method) {
        return getCallSitesIn(method).stream();
    }

    /**
     * @return 给定调用点的出度边
     */
    Stream<Edge<CallSite, Method>> edgesOutOf(CallSite callSite);

    /**
     * @return 给定方法的入度边
     * edgesOutOf和edgesInTo方法这样设计是因为图中的结构CallSite-》Method-》CallSite-》....
     */
    Stream<Edge<CallSite, Method>> edgesInTo(Method method);

    /**
     * @return all call edges in this call graph.
     */
    Stream<Edge<CallSite, Method>> edges();

    /**
     * @return 此调用图中的调用图边数。
     */
    int getNumberOfEdges();

    /**
     * @return 此调用图的入口方法。
     */
    Stream<Method> entryMethods();

    /**
     * @return 此调用图中所有可到达的方法。
     */
    Stream<Method> reachableMethods();

    /**
     * @return 此调用图中可到达方法的数量。
     */
    int getNumberOfMethods();

    /**
     * @return 如果此调用图包含给定的方法，则为true，否则为false。
     */
    boolean contains(Method method);
}
