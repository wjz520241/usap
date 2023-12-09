

package keeno.usap.analysis.graph.icfg;

import keeno.usap.analysis.graph.callgraph.CallGraph;

import java.util.Set;
import java.util.stream.Stream;

abstract class AbstractICFG<Method, Node> implements ICFG<Method, Node> {

    protected final CallGraph<Node, Method> callGraph;

    protected AbstractICFG(CallGraph<Node, Method> callGraph) {
        this.callGraph = callGraph;
    }

    @Override
    public Stream<Method> entryMethods() {
        return callGraph.entryMethods();
    }

    @Override
    public Set<Method> getCalleesOf(Node callSite) {
        return callGraph.getCalleesOf(callSite);
    }

    @Override
    public Set<Node> getCallersOf(Method method) {
        return callGraph.getCallersOf(method);
    }
}
