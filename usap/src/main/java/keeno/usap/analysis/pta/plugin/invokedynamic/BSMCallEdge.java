

package keeno.usap.analysis.pta.plugin.invokedynamic;

import keeno.usap.analysis.graph.callgraph.CallKind;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;

/**
 * Represents call graph edge from invokedynamic to bootstrap method.
 */
class BSMCallEdge extends Edge<CSCallSite, CSMethod> {

    public BSMCallEdge(CSCallSite csCallSite, CSMethod callee) {
        super(CallKind.OTHER, csCallSite, callee);
    }
}
