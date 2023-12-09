

package keeno.usap.analysis.pta.plugin.natives;

import keeno.usap.analysis.graph.callgraph.CallKind;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.util.AbstractIRModel;
import keeno.usap.analysis.pta.plugin.util.InvokeHandler;
import keeno.usap.ir.exp.InvokeInterface;
import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.util.collection.Maps;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DoPriviledgedModel extends AbstractIRModel {

    private final MethodRef privilegedActionRun;

    private final MethodRef privilegedExceptionActionRun;

    /**
     * Map from artificial invocation of run() to corresponding
     * invocation doPriviledged(...).
     */
    private final Map<Invoke, Invoke> run2DoPriv = Maps.newMap();

    DoPriviledgedModel(Solver solver) {
        super(solver);
        privilegedActionRun = Objects.requireNonNull(
                        hierarchy.getJREMethod("<java.security.PrivilegedAction: java.lang.Object run()>")).getRef();
        privilegedExceptionActionRun = Objects.requireNonNull(
                        hierarchy.getJREMethod("<java.security.PrivilegedExceptionAction: java.lang.Object run()>")).getRef();
    }

    @InvokeHandler(signature = "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedAction)>")
    @InvokeHandler(signature = "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedAction,java.security.AccessControlContext)>")
    public List<Stmt> doPrivilegedPA(Invoke invoke) {
        return doPrivileged(invoke, privilegedActionRun);
    }

    @InvokeHandler(signature = "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction)>")
    @InvokeHandler(signature = "<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction,java.security.AccessControlContext)>")
    public List<Stmt> doPrivilegedPEA(Invoke invoke) {
        return doPrivileged(invoke, privilegedExceptionActionRun);
    }

    private List<Stmt> doPrivileged(Invoke invoke, MethodRef run) {
        Invoke invokeRun = new Invoke(invoke.getContainer(),
                new InvokeInterface(run, invoke.getInvokeExp().getArg(0), List.of()),
                invoke.getResult());
        run2DoPriv.put(invokeRun, invoke);
        return List.of(invokeRun);
    }

    /**
     * Connects doPrivileged(...) invocation to the corresponding run() method
     * which is the callee of the corresponding run().
     */
    void handleNewCallEdge(Edge<CSCallSite, CSMethod> edge) {
        Invoke invoke = edge.getCallSite().getCallSite();
        Invoke doPrivilegedInvoke = run2DoPriv.get(invoke);
        if (doPrivilegedInvoke != null) {
            CSCallSite csCallSite = csManager.getCSCallSite(
                    edge.getCallSite().getContext(), doPrivilegedInvoke);
            solver.addCallEdge(new DoPrivilegedCallEdge(csCallSite, edge.getCallee()));
        }
    }

    /**
     * Represents call edge from AccessController.doPrivileged(...)
     * to the privileged action.
     */
    private static class DoPrivilegedCallEdge extends Edge<CSCallSite, CSMethod> {

        DoPrivilegedCallEdge(CSCallSite csCallSite, CSMethod callee) {
            super(CallKind.OTHER, csCallSite, callee);
        }
    }
}
