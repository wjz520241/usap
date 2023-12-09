

package keeno.usap.analysis.graph.callgraph;

interface CGBuilder<CallSite, Method> {

    CallGraph<CallSite, Method> build();
}
