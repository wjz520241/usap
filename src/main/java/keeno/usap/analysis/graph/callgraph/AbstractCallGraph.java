

package keeno.usap.analysis.graph.callgraph;

import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.Sets;
import keeno.usap.util.collection.Views;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common functionality for {@link CallGraph} implementations.
 * This class contains the data structures and methods for storing and
 * accessing information of a call graph. The logic of modifying
 * (adding new call edges and methods) is left to its subclasses.
 *
 * @param <CallSite> type of call sites
 * @param <Method>   type of methods
 */
public abstract class AbstractCallGraph<CallSite, Method>
        implements CallGraph<CallSite, Method> {

    protected final MultiMap<CallSite, Edge<CallSite, Method>> callSiteToEdges = Maps.newMultiMap();
    protected final MultiMap<Method, Edge<CallSite, Method>> calleeToEdges = Maps.newMultiMap();
    protected final Map<CallSite, Method> callSiteToContainer = Maps.newMap();
    protected final MultiMap<Method, CallSite> callSitesIn = Maps.newMultiMap(Sets::newHybridOrderedSet);
    protected final Set<Method> entryMethods = Sets.newSet();

    /**
     * 一组可到达的方法。该字段不是最终字段，因此它允许子类选择更有效的数据结构。
     */
    protected Set<Method> reachableMethods = Sets.newSet();

    @Override
    public Set<CallSite> getCallersOf(Method callee) {
        return Views.toMappedSet(calleeToEdges.get(callee), Edge::getCallSite);
    }

    @Override
    public Set<Method> getCalleesOf(CallSite callSite) {
        return Views.toMappedSet(callSiteToEdges.get(callSite), Edge::getCallee);
    }

    @Override
    public Set<Method> getCalleesOfM(Method caller) {
        return callSitesIn(caller)
                .flatMap(cs -> getCalleesOf(cs).stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Method getContainerOf(CallSite callSite) {
        return callSiteToContainer.get(callSite);
    }

    @Override
    public Set<CallSite> getCallSitesIn(Method method) {
        return callSitesIn.get(method);
    }

    @Override
    public Stream<Edge<CallSite, Method>> edgesOutOf(CallSite callSite) {
        return callSiteToEdges.get(callSite).stream();
    }

    @Override
    public Stream<Edge<CallSite, Method>> edgesInTo(Method method) {
        return calleeToEdges.get(method).stream();
    }

    @Override
    public Stream<Edge<CallSite, Method>> edges() {
        return callSiteToEdges.values().stream();
    }

    @Override
    public int getNumberOfEdges() {
        return callSiteToEdges.size();
    }

    @Override
    public Stream<Method> entryMethods() {
        return entryMethods.stream();
    }

    @Override
    public Stream<Method> reachableMethods() {
        return reachableMethods.stream();
    }

    @Override
    public int getNumberOfMethods() {
        return reachableMethods.size();
    }

    @Override
    public boolean contains(Method method) {
        return hasNode(method);
    }

    // Implementation for Graph interface.

    @Override
    public Set<MethodEdge<CallSite, Method>> getInEdgesOf(Method method) {
        return getCallersOf(method)
                .stream()
                .map(cs -> new MethodEdge<>(getContainerOf(cs), method, cs))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<MethodEdge<CallSite, Method>> getOutEdgesOf(Method method) {
        return callSitesIn(method)
                .flatMap(cs -> getCalleesOf(cs)
                        .stream()
                        .map(callee -> new MethodEdge<>(method, callee, cs)))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Method> getPredsOf(Method node) {
        return getCallersOf(node)
                .stream()
                .map(this::getContainerOf)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Method> getSuccsOf(Method node) {
        return callSitesIn(node)
                .flatMap(cs -> getCalleesOf(cs).stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Method> getNodes() {
        return Collections.unmodifiableSet(reachableMethods);
    }
}
