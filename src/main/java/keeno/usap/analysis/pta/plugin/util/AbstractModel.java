

package keeno.usap.analysis.pta.plugin.util;

import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.TriConsumer;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides common functionalities for implementing API models.
 */
public abstract class AbstractModel extends SolverHolder implements Model {

    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    protected final Map<JMethod, int[]> relevantVarIndexes = Maps.newHybridMap();

    protected final MultiMap<Var, Invoke> relevantVars
            = Maps.newMultiMap(Maps.newHybridMap());

    protected final Map<JMethod, TriConsumer<CSVar, PointsToSet, Invoke>> handlers
            = Maps.newMap();

    protected AbstractModel(Solver solver) {
        super(solver);
        registerVarAndHandlersByAnnotation();
        registerVarAndHandlers();
    }

    private void registerVarAndHandlersByAnnotation() {
        Class<?> clazz = getClass();
        for (Method method : clazz.getMethods()) {
            InvokeHandler[] invokeHandlers = method.getAnnotationsByType(InvokeHandler.class);
            if (invokeHandlers != null) {
                for (InvokeHandler invokeHandler : invokeHandlers) {
                    String signature = invokeHandler.signature();
                    JMethod api = hierarchy.getMethod(signature);
                    if (api != null) {
                        registerRelevantVarIndexes(api, invokeHandler.argIndexes());
                        registerAPIHandler(api, createHandler(method));
                    }
                }
            }
        }
    }

    /**
     * Creates a handler function (of type {@link TriConsumer}) for given method.
     * @param method the actual handler method
     * @return the resulting {@link TriConsumer}.
     */
    private TriConsumer<CSVar, PointsToSet, Invoke> createHandler(Method method) {
        try {
            MethodHandle handler = lookup.unreflect(method);
            MethodType handlerType = MethodType.methodType(
                    method.getReturnType(), method.getParameterTypes());
            CallSite callSite = LambdaMetafactory.metafactory(lookup,
                    "accept",
                    MethodType.methodType(TriConsumer.class, this.getClass()),
                    handlerType.erase(), handler, handlerType);
            MethodHandle factory = callSite.getTarget().bindTo(this);
            @SuppressWarnings ("unchecked")
            var handlerConsumer = (TriConsumer<CSVar, PointsToSet, Invoke>) factory.invoke();
            return handlerConsumer;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access " + method +
                    ", please make sure that the Model class and the handler method" +
                    " are public", e);
        } catch (LambdaConversionException e) {
            throw new RuntimeException("Failed to create lambda function for " + method +
                    ", please make sure that the parameter types of handler method" +
                    " is (CSVar, PointsToSet, Invoke)", e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected void registerVarAndHandlers() {
    }

    protected void registerRelevantVarIndexes(JMethod api, int... indexes) {
        relevantVarIndexes.put(api, indexes);
    }

    protected void registerAPIHandler(
            JMethod api, TriConsumer<CSVar, PointsToSet, Invoke> handler) {
        if (handlers.containsKey(api)) {
            throw new RuntimeException(this + " registers multiple handlers for " +
                    api + " (in a Model, at most one handler can be registered for a method)");
        }
        handlers.put(api, handler);
    }

    @Override
    public void handleNewInvoke(Invoke invoke) {
        JMethod target = invoke.getMethodRef().resolveNullable();
        if (target != null) {
            int[] indexes = relevantVarIndexes.get(target);
            if (indexes != null) {
                for (int i : indexes) {
                    relevantVars.put(InvokeUtils.getVar(invoke, i), invoke);
                }
            }
        }
    }

    @Override
    public boolean isRelevantVar(Var var) {
        return relevantVars.containsKey(var);
    }

    @Override
    public void handleNewPointsToSet(CSVar csVar, PointsToSet pts) {
        relevantVars.get(csVar.getVar()).forEach(invoke -> {
            JMethod target = invoke.getMethodRef().resolve();
            var handler = handlers.get(target);
            if (handler != null) {
                handler.accept(csVar, pts, invoke);
            }
        });
    }

    /**
     * For invocation r = v.foo(a0, a1, ..., an);
     * when points-to set of v or any ai (0 &le; i &le; n) changes,
     * this convenient method returns points-to sets relevant arguments.
     * For case v/ai == csVar.getVar(), this method returns pts,
     * otherwise, it just returns current points-to set of v/ai.
     *
     * @param csVar   may be v or any ai.
     * @param pts     changed part of csVar
     * @param invoke  the call site which contain csVar
     * @param indexes indexes of the relevant arguments
     */
    protected List<PointsToSet> getArgs(
            CSVar csVar, PointsToSet pts, Invoke invoke, int... indexes) {
        List<PointsToSet> args = new ArrayList<>(indexes.length);
        for (int i : indexes) {
            Var arg = InvokeUtils.getVar(invoke, i);
            if (arg.equals(csVar.getVar())) {
                args.add(pts);
            } else {
                CSVar csArg = csManager.getCSVar(csVar.getContext(), arg);
                args.add(solver.getPointsToSetOf(csArg));
            }
        }
        return args;
    }
}
