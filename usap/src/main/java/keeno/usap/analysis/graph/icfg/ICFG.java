

package keeno.usap.analysis.graph.icfg;

import keeno.usap.util.graph.Graph;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Represents an inter-procedural control-flow graph.
 */
public interface ICFG<Method, Node> extends Graph<Node> {

    /**
     * @return entry methods of the ICFG.
     */
    Stream<Method> entryMethods();

    /**
     * @return the incoming edges of the given node.
     */
    @Override
    Set<ICFGEdge<Node>> getInEdgesOf(Node node);

    /**
     * @return the outgoing edges of the given node.
     */
    @Override
    Set<ICFGEdge<Node>> getOutEdgesOf(Node node);

    /**
     * @return the methods that are called by the given call site.
     */
    Set<Method> getCalleesOf(Node callSite);

    /**
     * @return the return sites of the given call site.
     */
    Set<Node> getReturnSitesOf(Node callSite);

    /**
     * @return the entry node of the given method.
     */
    Node getEntryOf(Method method);

    /**
     * @return the exit node of the given method.
     */
    Node getExitOf(Method method);

    /**
     * @return the call sites that invoke the given method.
     */
    Set<Node> getCallersOf(Method method);

    /**
     * @return the method that contains the given node.
     */
    Method getContainingMethodOf(Node node);

    /**
     * @return true if the given node is a call site, otherwise false.
     */
    boolean isCallSite(Node node);
}
