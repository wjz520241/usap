

package keeno.usap.analysis.pta.plugin.util;

import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.collection.Maps;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Provides common functionalities for implementing IR-based API models.
 */
public abstract class AbstractIRModel extends SolverHolder implements IRModel {

    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    protected final Map<JMethod, Function<Invoke, Collection<Stmt>>> handlers
            = Maps.newMap();

    protected final Map<JMethod, Collection<Stmt>> method2GenStmts
            = Maps.newHybridMap();

    protected AbstractIRModel(Solver solver) {
        super(solver);
        registerHandlersByAnnotation();
        registerHandlers();
    }

    protected void registerHandlersByAnnotation() {
        Class<?> clazz = getClass();
        for (Method method : clazz.getMethods()) {
            InvokeHandler[] invokeHandlers = method.getAnnotationsByType(InvokeHandler.class);
            if (invokeHandlers != null) {
                for (InvokeHandler invokeHandler : invokeHandlers) {
                    String signature = invokeHandler.signature();
                    JMethod api = hierarchy.getMethod(signature);
                    if (api != null) {
                        registerHandler(api, createHandler(method));
                    }
                }
            }
        }
    }

    /**
     * Creates a handler function (of type {@link Function}) for given method.
     * @param method the actual handler method
     * @return the resulting {@link Function}.
     */
    private Function<Invoke, Collection<Stmt>> createHandler(Method method) {
        try {
            MethodHandle handler = lookup.unreflect(method);
            MethodType handlerType = MethodType.methodType(
                    method.getReturnType(), method.getParameterTypes());
            CallSite callSite = LambdaMetafactory.metafactory(lookup,
                    "apply",
                    MethodType.methodType(Function.class, this.getClass()),
                    handlerType.erase(), handler, handlerType);
            MethodHandle factory = callSite.getTarget().bindTo(this);
            @SuppressWarnings ("unchecked")
            var handlerFunction = (Function<Invoke, Collection<Stmt>>) factory.invoke();
            return handlerFunction;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access " + method +
                    ", please make sure that the IRModel class and the handler method" +
                    " are public", e);
        } catch (LambdaConversionException e) {
            throw new RuntimeException("Failed to create lambda function for " + method +
                    ", please make sure that the type of handler method" +
                    " is (Invoke)Collection<Stmt>", e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected void registerHandlers() {
    }

    protected void registerHandler(JMethod api, Function<Invoke, Collection<Stmt>> handler) {
        if (handlers.containsKey(api)) {
            throw new RuntimeException(this + " registers multiple handlers for " +
                    api + " (in an IRModel, at most one handler can be registered for a method)");
        }
        handlers.put(api, handler);
    }

    @Override
    public Set<JMethod> getModeledAPIs() {
        return handlers.keySet();
    }

    @Override
    public void handleNewMethod(JMethod method) {
        List<Stmt> stmts = new ArrayList<>();
        method.getIR().invokes(false).forEach(invoke -> {
            JMethod target = invoke.getMethodRef().resolveNullable();
            if (target != null) {
                var handler = handlers.get(target);
                if (handler != null) {
                    stmts.addAll(handler.apply(invoke));
                }
            }
        });
        if (!stmts.isEmpty()) {
            method2GenStmts.put(method, List.copyOf(stmts));
        }
    }

    @Override
    public void handleNewCSMethod(CSMethod csMethod) {
        Collection<Stmt> genStmts = method2GenStmts.get(csMethod.getMethod());
        if (genStmts != null) {
            solver.addStmts(csMethod, genStmts);
        }
    }
}
