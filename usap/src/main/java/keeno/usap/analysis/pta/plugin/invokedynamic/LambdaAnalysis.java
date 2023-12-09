

package keeno.usap.analysis.pta.plugin.invokedynamic;

import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.graph.flowgraph.FlowKind;
import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSManager;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.cs.selector.ContextSelector;
import keeno.usap.analysis.pta.core.heap.Descriptor;
import keeno.usap.analysis.pta.core.heap.HeapModel;
import keeno.usap.analysis.pta.core.heap.MockObj;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.Plugin;
import keeno.usap.analysis.pta.plugin.util.CSObjs;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.exp.InvokeDynamic;
import keeno.usap.ir.exp.MethodHandle;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.ClassHierarchy;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.classes.Signatures;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.Type;
import keeno.usap.util.AnalysisException;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

import javax.annotation.Nullable;
import java.util.List;

public class LambdaAnalysis implements Plugin {

    /**
     * Descriptor for lambda functional objects.
     */
    private static final Descriptor LAMBDA_DESC = () -> "LambdaObj";

    /**
     * Descriptor for objects created by lambda constructor.
     */
    private static final Descriptor LAMBDA_NEW_DESC = () -> "LambdaConstructedObj";

    private Solver solver;

    private HeapModel heapModel;

    private ContextSelector selector;

    private ClassHierarchy hierarchy;

    private CSManager csManager;

    /**
     * Map from method to the lambda functional objects created in the method.
     */
    private final MultiMap<JMethod, Obj> lambdaObjs = Maps.newMultiMap();

    /**
     * Map from receiver variable to the information about the related
     * instance invocation sites. When new objects reach the receiver variable,
     * this information will be used to build lambda call edges.
     */
    private final MultiMap<CSVar, InstanceInvoInfo> invoInfos = Maps.newMultiMap();

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        this.heapModel = solver.getHeapModel();
        this.selector = solver.getContextSelector();
        this.hierarchy = solver.getHierarchy();
        this.csManager = solver.getCSManager();
    }

    @Override
    public void onNewStmt(Stmt stmt, JMethod container) {
        if (stmt instanceof Invoke invoke &&
                invoke.isDynamic() &&
                isLambdaMetaFactory(invoke)) {
            InvokeDynamic indy = (InvokeDynamic) invoke.getInvokeExp();
            Type type = indy.getMethodType().getReturnType();
            // record lambda meta factories of new reachable methods
            lambdaObjs.put(container,
                    heapModel.getMockObj(LAMBDA_DESC, invoke, type, container));
        }
    }

    static boolean isLambdaMetaFactory(Invoke invoke) {
        // Declaring class of bootstrap method reference may be phantom
        // (looks like an issue of current (soot-based) front end),
        // thus we use resolveNullable() to avoid exception.
        // TODO: change to resolve() after using new front end
        JMethod bsm = ((InvokeDynamic) invoke.getInvokeExp())
                .getBootstrapMethodRef()
                .resolveNullable();
        if (bsm != null) {
            String bsmSig = bsm.getSignature();
            return bsmSig.equals(Signatures.LAMBDA_METAFACTORY) ||
                    bsmSig.equals(Signatures.LAMBDA_ALTMETAFACTORY);
        } else {
            return false;
        }
    }

    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        JMethod method = csMethod.getMethod();
        Context context = csMethod.getContext();
        lambdaObjs.get(method).forEach(lambdaObj -> {
            // propagate lambda functional objects
            Invoke invoke = (Invoke) lambdaObj.getAllocation();
            Var ret = invoke.getResult();
            assert ret != null;
            // here we use full method context as the heap context of
            // lambda object, so that it can be directly used to obtain
            // context-sensitive captured values later.
            solver.addVarPointsTo(context, ret, context, lambdaObj);
        });
    }

    @Override
    public void onUnresolvedCall(CSObj recv, Context context, Invoke invoke) {
        if (!CSObjs.hasDescriptor(recv, LAMBDA_DESC)) {
            return;
        }
        MockObj lambdaObj = (MockObj) recv.getObject();
        Invoke indyInvoke = (Invoke) lambdaObj.getAllocation();
        InvokeDynamic indy = (InvokeDynamic) indyInvoke.getInvokeExp();
        if (!indy.getMethodName().equals(invoke.getMethodRef().getName())) {
            // Use method name to filter out mismatched (caused by imprecision
            // of pointer analysis) lambda objects and actual invocation sites.
            // TODO: use more information to filter out mismatches?
            return;
        }
        Context indyCtx = recv.getContext();
        CSCallSite csCallSite = csManager.getCSCallSite(context, invoke);
        MethodHandle mh = getMethodHandle(indy);
        final MethodRef targetRef = mh.getMethodRef();

        switch (mh.getKind()) {
            case REF_newInvokeSpecial -> { // targetRef is constructor
                ClassType type = targetRef.getDeclaringClass().getType();
                // Create mock object (if absent) which represents
                // the newly-allocated object. Note that here we use the
                // *invokedynamic* to represent the *allocation site*,
                // instead of the actual invocation site of the constructor.
                Obj newObj = heapModel.getMockObj(LAMBDA_NEW_DESC,
                        indyInvoke, type, indyInvoke.getContainer());
                // pass the mock object to result variable (if present)
                // TODO: double-check if the heap context is proper
                CSObj csNewObj = csManager.getCSObj(context, newObj);
                Var result = invoke.getResult();
                if (result != null) {
                    solver.addVarPointsTo(context, result, csNewObj);
                }
                // add call edge to constructor
                addLambdaCallEdge(csCallSite, csNewObj, targetRef, indy, indyCtx);
            }
            case REF_invokeInterface, REF_invokeVirtual, REF_invokeSpecial -> {
                // targetRef is instance method
                List<Var> capturedArgs = indy.getArgs();
                List<Var> actualArgs = invoke.getInvokeExp().getArgs();
                // Obtain receiver variable and context
                Var recvVar;
                Context recvCtx;
                if (!capturedArgs.isEmpty()) {
                    // if captured arguments are not empty, then the first one
                    // must be the receiver object for targetRef
                    recvVar = capturedArgs.get(0);
                    recvCtx = indyCtx;
                } else {
                    // otherwise, the first actual argument is the receiver
                    recvVar = actualArgs.get(0);
                    recvCtx = context;
                }
                CSVar csRecvVar = csManager.getCSVar(recvCtx, recvVar);
                solver.getPointsToSetOf(csRecvVar).forEach(recvObj ->
                        addLambdaCallEdge(csCallSite, recvObj, targetRef,
                                indy, indyCtx));
                // New objects may reach csRecvVar later, thus we store it
                // together with information about the related Lambda invocation.
                invoInfos.put(csRecvVar,
                        new InstanceInvoInfo(csCallSite, indy, indyCtx));
            }
            case REF_invokeStatic -> // targetRef is static method
                    addLambdaCallEdge(csCallSite, null, targetRef, indy, indyCtx);
            default -> throw new AnalysisException(mh.getKind() + " is not supported");
        }
    }

    /**
     * @return the MethodHandle to the target method from invokedynamic.
     */
    private static MethodHandle getMethodHandle(InvokeDynamic indy) {
        return (MethodHandle) indy.getBootstrapArgs().get(1);
    }

    private void addLambdaCallEdge(
            CSCallSite csCallSite, @Nullable CSObj recvObj, MethodRef targetRef,
            InvokeDynamic indy, Context indyCtx) {
        JMethod callee;
        Context calleeCtx;
        if (recvObj != null) {
            // recvObj is not null, meaning that callee is instance method
            callee = hierarchy.dispatch(recvObj.getObject().getType(), targetRef);
            if (callee == null) {
                return;
            }
            calleeCtx = selector.selectContext(csCallSite, recvObj, callee);
            // pass receiver object to 'this' variable of callee
            solver.addVarPointsTo(calleeCtx, callee.getIR().getThis(), recvObj);
        } else { // otherwise, callee is static method
            callee = targetRef.resolveNullable();
            if (callee == null) {
                // this may happen in special cases, e.g.,
                // when the declaring class of targetRef is phantom class
                return;
            }
            calleeCtx = selector.selectContext(csCallSite, callee);
        }
        LambdaCallEdge callEdge = new LambdaCallEdge(csCallSite,
                csManager.getCSMethod(calleeCtx, callee), indy, indyCtx);
        solver.addCallEdge(callEdge);
    }

    @Override
    public void onNewCallEdge(Edge<CSCallSite, CSMethod> edge) {
        if (edge instanceof LambdaCallEdge lambdaCallEdge) {
            CSCallSite csCallSite = lambdaCallEdge.getCallSite();
            CSMethod csCallee = lambdaCallEdge.getCallee();
            Context calleeContext = csCallee.getContext();
            List<Var> capturedArgs = lambdaCallEdge.getCapturedArgs();
            Context lambdaContext = lambdaCallEdge.getLambdaContext();

            JMethod target = csCallee.getMethod();
            // pass arguments captured at invokedynamic
            int shiftC; // shift of captured arguments
            if (capturedArgs.isEmpty()) {
                shiftC = 0;
            } else if (target.isStatic() || target.isConstructor()) {
                shiftC = 0;
            } else { // target is instance method and there is at least
                // one capture argument, then it must be the receiver object,
                // which has been passed to target's this variable when
                // adding call edge. Thus, we skip it and don't pass it
                // to target's parameters.
                shiftC = 1;
            }
            List<Var> targetParams = target.getIR().getParams();
            int j = 0;
            for (int i = shiftC; i < capturedArgs.size(); ++i, ++j) {
                solver.addPFGEdge(
                        csManager.getCSVar(lambdaContext, capturedArgs.get(i)),
                        csManager.getCSVar(calleeContext, targetParams.get(j)),
                        // filter spurious objects caused by imprecise lambda objects
                        FlowKind.PARAMETER_PASSING,
                        targetParams.get(j).getType());
            }
            // pass arguments from actual invocation site
            int shiftA; // shift of actual arguments
            if (capturedArgs.isEmpty() &&
                    !target.isStatic() && !target.isConstructor()) {
                // target is instance method and there is no any captured arguments,
                // then the first argument at actual invocation site must be
                // the receiver object, which has been passed to target's
                // this variable when adding call edge. So we can also skip it.
                shiftA = 1;
            } else {
                shiftA = 0;
            }
            Invoke invoke = csCallSite.getCallSite();
            List<Var> actualArgs = invoke.getInvokeExp().getArgs();
            Context callerContext = csCallSite.getContext();
            for (int i = shiftA; i < actualArgs.size(); ++i, ++j) {
                solver.addPFGEdge(
                        csManager.getCSVar(callerContext, actualArgs.get(i)),
                        csManager.getCSVar(calleeContext, targetParams.get(j)),
                        // filter spurious objects caused by imprecise lambda objects
                        FlowKind.PARAMETER_PASSING, targetParams.get(j).getType()
                );
            }
            // pass return value
            Var result = invoke.getResult();
            if (result != null) {
                CSVar csResult = csManager.getCSVar(callerContext, result);
                target.getIR().getReturnVars().forEach(ret -> {
                    CSVar csRet = csManager.getCSVar(calleeContext, ret);
                    solver.addPFGEdge(csRet, csResult,
                            // filter spurious objects caused by imprecise lambda objects
                            FlowKind.RETURN, result.getType());
                });
            }
        }
    }

    @Override
    public void onNewPointsToSet(CSVar csVar, PointsToSet pts) {
        invoInfos.get(csVar).forEach(info -> {
            // handle the case of that new objects reach base variable
            // of lambda invocation
            InvokeDynamic indy = info.getLambdaIndy();
            MethodRef targetRef = getMethodHandle(indy).getMethodRef();
            pts.forEach(recvObj ->
                    addLambdaCallEdge(info.getCSCallSite(), recvObj,
                            targetRef, indy, info.getLambdaContext()));
        });
    }
}
