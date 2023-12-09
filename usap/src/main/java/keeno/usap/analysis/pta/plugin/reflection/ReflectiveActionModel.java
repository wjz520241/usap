

package keeno.usap.analysis.pta.plugin.reflection;

import keeno.usap.analysis.graph.flowgraph.FlowKind;
import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.cs.element.InstanceField;
import keeno.usap.analysis.pta.core.cs.element.StaticField;
import keeno.usap.analysis.pta.core.heap.Descriptor;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.util.AbstractModel;
import keeno.usap.analysis.pta.plugin.util.CSObjs;
import keeno.usap.analysis.pta.plugin.util.InvokeHandler;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.classes.Subsignature;
import keeno.usap.language.type.ArrayType;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.ReferenceType;
import keeno.usap.language.type.Type;
import keeno.usap.language.type.VoidType;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static keeno.usap.analysis.graph.flowgraph.FlowKind.INSTANCE_STORE;
import static keeno.usap.analysis.graph.flowgraph.FlowKind.STATIC_STORE;
import static keeno.usap.analysis.pta.plugin.util.InvokeUtils.BASE;

/**
 * Models reflective-action methods, currently supports
 * <ul>
 *     <li>Class.newInstance()
 *     <li>Constructor.newInstance(Object[])
 *     <li>Method.invoke(Object,Object[])
 *     <li>Field.get(Object)
 *     <li>Field.set(Object,Object)
 *     <li>Array.newInstance(Class,int)
 * </ul>
 * TODO: check accessibility
 */
public class ReflectiveActionModel extends AbstractModel {

    /**
     * Descriptor for objects created by reflective newInstance() calls.
     */
    private static final Descriptor REF_OBJ_DESC = () -> "ReflectiveObj";

    private final Subsignature initNoArg;

    private final MetaObjHelper helper;

    private final TypeMatcher typeMatcher;

    /**
     * Set of invocations that are annotated by the reflection log.
     */
    private final Set<Invoke> invokesWithLog;

    /**
     * Records all reflective targets resolved by reflection analysis.
     */
    private final MultiMap<Invoke, Object> allTargets = Maps.newMultiMap();

    ReflectiveActionModel(Solver solver, MetaObjHelper helper,
                          TypeMatcher typeMatcher, Set<Invoke> invokesWithLog) {
        super(solver);
        initNoArg = Subsignature.getNoArgInit();
        this.helper = helper;
        this.typeMatcher = typeMatcher;
        this.invokesWithLog = invokesWithLog;
    }

    @InvokeHandler(signature = "<java.lang.Class: java.lang.Object newInstance()>", argIndexes = {BASE})
    public void classNewInstance(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Context context = csVar.getContext();
        pts.forEach(obj -> {
            if (isInvalidTarget(invoke, obj)) {
                return;
            }
            JClass clazz = CSObjs.toClass(obj);
            if (clazz != null) {
                JMethod init = clazz.getDeclaredMethod(initNoArg);
                if (init != null && !typeMatcher.isUnmatched(invoke, init)) {
                    ClassType type = clazz.getType();
                    CSObj csNewObj = newReflectiveObj(context, invoke, type);
                    addReflectiveCallEdge(context, invoke, csNewObj, init, null);
                }
            }
        });
    }

    @InvokeHandler(signature = "<java.lang.reflect.Constructor: java.lang.Object newInstance(java.lang.Object[])>", argIndexes = {BASE})
    public void constructorNewInstance(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Context context = csVar.getContext();
        pts.forEach(obj -> {
            if (isInvalidTarget(invoke, obj)) {
                return;
            }
            JMethod constructor = CSObjs.toConstructor(obj);
            if (constructor != null && !typeMatcher.isUnmatched(invoke, constructor)) {
                ClassType type = constructor.getDeclaringClass().getType();
                CSObj csNewObj = newReflectiveObj(context, invoke, type);
                addReflectiveCallEdge(context, invoke, csNewObj,
                        constructor, invoke.getInvokeExp().getArg(0));
            }
        });
    }

    private CSObj newReflectiveObj(Context context, Invoke invoke, ReferenceType type) {
        Obj newObj = heapModel.getMockObj(REF_OBJ_DESC,
                invoke, type, invoke.getContainer());
        // TODO: double-check if the heap context is proper
        CSObj csNewObj = csManager.getCSObj(context, newObj);
        Var result = invoke.getResult();
        if (result != null) {
            solver.addVarPointsTo(context, result, csNewObj);
        }
        return csNewObj;
    }

    @InvokeHandler(signature = "<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>", argIndexes = {BASE, 0})
    public void methodInvoke(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Context context = csVar.getContext();
        List<PointsToSet> args = getArgs(csVar, pts, invoke, BASE, 0);
        PointsToSet mtdObjs = args.get(0);
        PointsToSet recvObjs = args.get(1);
        Var argsVar = invoke.getInvokeExp().getArg(1);
        mtdObjs.forEach(mtdObj -> {
            if (isInvalidTarget(invoke, mtdObj)) {
                return;
            }
            JMethod target = CSObjs.toMethod(mtdObj);
            if (target != null && !typeMatcher.isUnmatched(invoke, target)) {
                if (target.isStatic()) {
                    addReflectiveCallEdge(context, invoke, null, target, argsVar);
                } else {
                    recvObjs.forEach(recvObj ->
                            addReflectiveCallEdge(context, invoke, recvObj, target, argsVar));
                }
            }
        });
    }

    private void addReflectiveCallEdge(
            Context callerCtx, Invoke callSite,
            @Nullable CSObj recvObj, JMethod callee, Var args) {
        if (!callee.isConstructor() && !callee.isStatic()) {
            // dispatch for instance method (except constructor)
            assert recvObj != null : "recvObj is required for instance method";
            callee = hierarchy.dispatch(recvObj.getObject().getType(),
                    callee.getRef());
            if (callee == null) {
                return;
            }
        }
        CSCallSite csCallSite = csManager.getCSCallSite(callerCtx, callSite);
        Context calleeCtx;
        if (callee.isStatic()) {
            calleeCtx = selector.selectContext(csCallSite, callee);
        } else {
            calleeCtx = selector.selectContext(csCallSite, recvObj, callee);
            // pass receiver object to 'this' variable of callee
            solver.addVarPointsTo(calleeCtx, callee.getIR().getThis(), recvObj);
        }
        ReflectiveCallEdge callEdge = new ReflectiveCallEdge(csCallSite,
                csManager.getCSMethod(calleeCtx, callee), args);
        solver.addCallEdge(callEdge);
        allTargets.put(callSite, callee); // record target
    }

    @InvokeHandler(signature = "<java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>", argIndexes = {BASE, 0})
    public void fieldGet(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Var result = invoke.getResult();
        if (result == null) {
            return;
        }
        Context context = csVar.getContext();
        CSVar to = csManager.getCSVar(context, result);
        List<PointsToSet> args = getArgs(csVar, pts, invoke, BASE, 0);
        PointsToSet fldObjs = args.get(0);
        PointsToSet baseObjs = args.get(1);
        fldObjs.forEach(fldObj -> {
            if (isInvalidTarget(invoke, fldObj)) {
                return;
            }
            JField field = CSObjs.toField(fldObj);
            if (field != null) {
                if (field.isStatic()) {
                    StaticField sfield = csManager.getStaticField(field);
                    solver.addPFGEdge(sfield, to, FlowKind.STATIC_LOAD);
                } else {
                    Type declType = field.getDeclaringClass().getType();
                    baseObjs.forEach(baseObj -> {
                        Type objType = baseObj.getObject().getType();
                        if (typeSystem.isSubtype(declType, objType)) {
                            InstanceField ifield = csManager.getInstanceField(baseObj, field);
                            solver.addPFGEdge(ifield, to, FlowKind.INSTANCE_LOAD);
                            allTargets.put(invoke, field); // record target
                        }
                    });
                }
            }
        });
    }

    @InvokeHandler(signature = "<java.lang.reflect.Field: void set(java.lang.Object,java.lang.Object)>", argIndexes = {BASE, 0})
    public void fieldSet(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Context context = csVar.getContext();
        CSVar from = csManager.getCSVar(context, invoke.getInvokeExp().getArg(1));
        List<PointsToSet> args = getArgs(csVar, pts, invoke, BASE, 0);
        PointsToSet fldObjs = args.get(0);
        PointsToSet baseObjs = args.get(1);
        fldObjs.forEach(fldObj -> {
            if (isInvalidTarget(invoke, fldObj)) {
                return;
            }
            JField field = CSObjs.toField(fldObj);
            if (field != null) {
                if (field.isStatic()) {
                    StaticField sfield = csManager.getStaticField(field);
                    solver.addPFGEdge(from, sfield, STATIC_STORE, sfield.getType());
                } else {
                    Type declType = field.getDeclaringClass().getType();
                    baseObjs.forEach(baseObj -> {
                        Type objType = baseObj.getObject().getType();
                        if (typeSystem.isSubtype(declType, objType)) {
                            InstanceField ifield = csManager.getInstanceField(baseObj, field);
                            solver.addPFGEdge(from, ifield, INSTANCE_STORE, ifield.getType());
                            allTargets.put(invoke, field); // record target
                        }
                    });
                }
            }
        });
    }

    @InvokeHandler(signature = "<java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>", argIndexes = {0})
    public void arrayNewInstance(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Var result = invoke.getResult();
        if (result == null) {
            return;
        }
        Context context = csVar.getContext();
        pts.forEach(obj -> {
            if (isInvalidTarget(invoke, obj)) {
                return;
            }
            Type baseType = CSObjs.toType(obj);
            if (baseType != null && !(baseType instanceof VoidType)) {
                ArrayType arrayType = typeSystem.getArrayType(baseType, 1);
                CSObj csNewArray = newReflectiveObj(context, invoke, arrayType);
                solver.addVarPointsTo(context, result, csNewArray);
                allTargets.put(invoke, arrayType);
            }
        });
    }

    /**
     * If a reflective invocation {@code invoke} is annotated by the log,
     * and the given {@code metaObj} is not generated by the log, then we treat
     * {@code metaObj} as an invalid target for {@code invoke}.
     */
    private boolean isInvalidTarget(Invoke invoke, CSObj metaObj) {
        return invokesWithLog.contains(invoke) && !helper.isLogMetaObj(metaObj);
    }

    MultiMap<Invoke, Object> getAllTargets() {
        return allTargets;
    }
}
