

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.graph.callgraph.CallKind;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.analysis.pta.plugin.util.InvokeUtils;
import keeno.usap.ir.exp.Var;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.MultiMapCollector;
import keeno.usap.util.collection.Sets;

import java.util.List;
import java.util.Set;

/**
 * Handles sinks in taint analysis.
 */
class SinkHandler extends Handler {

    private final List<Sink> sinks;

    SinkHandler(HandlerContext context) {
        super(context);
        sinks = context.config().sinks();
    }

    Set<TaintFlow> collectTaintFlows() {
        PointerAnalysisResult result = solver.getResult();
        Set<TaintFlow> taintFlows = Sets.newOrderedSet();
        sinks.forEach(sink -> {
            int i = sink.index();
            result.getCallGraph()
                    .edgesInTo(sink.method())
                    // TODO: handle other call edges
                    .filter(e -> e.getKind() != CallKind.OTHER)
                    .map(Edge::getCallSite)
                    .forEach(sinkCall -> {
                        Var arg = InvokeUtils.getVar(sinkCall, i);
                        SinkPoint sinkPoint = new SinkPoint(sinkCall, i);
                        result.getPointsToSet(arg)
                                .stream()
                                .filter(manager::isTaint)
                                .map(manager::getSourcePoint)
                                .map(sourcePoint -> new TaintFlow(sourcePoint, sinkPoint))
                                .forEach(taintFlows::add);
                    });
        });
        if (callSiteMode) {
            MultiMap<JMethod, Sink> sinkMap = sinks.stream()
                    .collect(MultiMapCollector.get(Sink::method, s -> s));
            // scan all reachable call sites to search sink calls
            result.getCallGraph()
                    .reachableMethods()
                    .filter(m -> !m.isAbstract())
                    .flatMap(m -> m.getIR().invokes(false))
                    .forEach(callSite -> {
                        JMethod callee = callSite.getMethodRef().resolveNullable();
                        if (callee == null) {
                            return;
                        }
                        for (Sink sink : sinkMap.get(callee)) {
                            int i = sink.index();
                            Var arg = InvokeUtils.getVar(callSite, i);
                            SinkPoint sinkPoint = new SinkPoint(callSite, i);
                            result.getPointsToSet(arg)
                                    .stream()
                                    .filter(manager::isTaint)
                                    .map(manager::getSourcePoint)
                                    .map(sourcePoint -> new TaintFlow(sourcePoint, sinkPoint))
                                    .forEach(taintFlows::add);
                        }
                    });
        }
        return taintFlows;
    }
}
