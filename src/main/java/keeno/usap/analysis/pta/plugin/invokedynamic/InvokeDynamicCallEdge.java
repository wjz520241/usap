

package keeno.usap.analysis.pta.plugin.invokedynamic;

import keeno.usap.analysis.graph.callgraph.CallKind;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;

/**
 * Represents call edge from invokedynamic to actual target method.
 */
class InvokeDynamicCallEdge extends Edge<CSCallSite, CSMethod> {

    InvokeDynamicCallEdge(CSCallSite csCallSite, CSMethod callee) {
        super(CallKind.OTHER, csCallSite, callee);
    }
}
