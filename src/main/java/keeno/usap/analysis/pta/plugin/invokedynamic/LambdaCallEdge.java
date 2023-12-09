

package keeno.usap.analysis.pta.plugin.invokedynamic;

import keeno.usap.analysis.graph.callgraph.CallKind;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.ir.exp.InvokeDynamic;
import keeno.usap.ir.exp.Var;
import keeno.usap.util.Hashes;

import java.util.List;

/**
 * Represents call edge on lambda functional object.
 * The edge carries the information about invokedynamic invocation site
 * where the lambda functional object was created.
 */
class LambdaCallEdge extends Edge<CSCallSite, CSMethod> {

    private final InvokeDynamic lambdaIndy;

    private final Context lambdaContext;

    LambdaCallEdge(CSCallSite csCallSite, CSMethod callee,
                   InvokeDynamic lambdaIndy, Context lambdaContext) {
        super(CallKind.OTHER, csCallSite, callee);
        this.lambdaIndy = lambdaIndy;
        this.lambdaContext = lambdaContext;
    }

    List<Var> getCapturedArgs() {
        return lambdaIndy.getArgs();
    }

    Context getLambdaContext() {
        return lambdaContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        LambdaCallEdge that = (LambdaCallEdge) o;
        return lambdaIndy.equals(that.lambdaIndy) &&
                lambdaContext.equals(that.lambdaContext);
    }

    @Override
    public int hashCode() {
        return Hashes.hash(super.hashCode(), lambdaIndy, lambdaContext);
    }
}
