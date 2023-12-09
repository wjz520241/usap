

package keeno.usap.analysis.pta.plugin.reflection;

import keeno.usap.analysis.graph.callgraph.CallKind;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.ir.exp.Var;

import javax.annotation.Nullable;

/**
 * Represents reflective call edges.
 */
class ReflectiveCallEdge extends Edge<CSCallSite, CSMethod> {

    /**
     * Variable pointing to the array argument of reflective call,
     * which contains the arguments for the reflective target method, i.e.,
     * args for constructor.newInstance(args)/method.invoke(o, args).
     * This field is null for call edges from Class.newInstance().
     */
    @Nullable
    private final Var args;

    ReflectiveCallEdge(CSCallSite csCallSite, CSMethod callee, @Nullable Var args) {
        super(CallKind.OTHER, csCallSite, callee);
        this.args = args;
    }

    @Nullable
    Var getArgs() {
        return args;
    }
}
