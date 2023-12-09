

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.graph.callgraph.CallKind;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.plugin.util.InvokeUtils;
import keeno.usap.ir.IR;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.LoadField;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

import java.util.Map;
import java.util.Set;

/**
 * Handles sources in taint analysis.
 */
class SourceHandler extends OnFlyHandler {

    /**
     * Map from a source method to its result sources.
     */
    private final MultiMap<JMethod, CallSource> callSources = Maps.newMultiMap();

    /**
     * Map from a method to {@link Invoke} statements in the method
     * which matches any call source.
     * This map matters only when call-site mode is enabled.
     */
    private final MultiMap<JMethod, Invoke> callSiteSources = Maps.newMultiMap();

    /**
     * Map from a source method to its parameter sources.
     */
    private final MultiMap<JMethod, ParamSource> paramSources = Maps.newMultiMap();

    /**
     * Whether this handler needs to handle field sources.
     */
    private final boolean handleFieldSources;

    /**
     * Map from a source field taint objects generated from it.
     */
    private final Map<JField, Type> fieldSources = Maps.newMap();

    /**
     * Maps from a method to {@link LoadField} statements in the method
     * which loads a source field.
     */
    private final MultiMap<JMethod, LoadField> loadedFieldSources = Maps.newMultiMap();

    SourceHandler(HandlerContext context) {
        super(context);
        context.config().sources().forEach(src -> {
            if (src instanceof CallSource callSrc) {
                callSources.put(callSrc.method(), callSrc);
            } else if (src instanceof ParamSource paramSrc) {
                paramSources.put(paramSrc.method(), paramSrc);
            } else if (src instanceof FieldSource fieldSrc) {
                fieldSources.put(fieldSrc.field(), fieldSrc.type());
            }
        });
        handleFieldSources = !fieldSources.isEmpty();
    }

    /**
     * Generates taint objects from call sources.
     */
    private void processCallSource(Context context, Invoke callSite, CallSource source) {
        int index = source.index();
        if (InvokeUtils.RESULT == index && callSite.getLValue() == null) {
            return;
        }
        Var var = InvokeUtils.getVar(callSite, index);
        SourcePoint sourcePoint = new CallSourcePoint(callSite, index);
        Obj taint = manager.makeTaint(sourcePoint, source.type());
        solver.addVarPointsTo(context, var, taint);
    }

    /**
     * Handles call sources.
     */
    @Override
    public void onNewCallEdge(Edge<CSCallSite, CSMethod> edge) {
        if (edge.getKind() == CallKind.OTHER) {
            return;
        }
        Set<CallSource> sources = callSources.get(edge.getCallee().getMethod());
        if (!sources.isEmpty()) {
            Context context = edge.getCallSite().getContext();
            Invoke callSite = edge.getCallSite().getCallSite();
            sources.forEach(source -> processCallSource(context, callSite, source));
        }

    }

    @Override
    public void onNewStmt(Stmt stmt, JMethod container) {
        if (handleFieldSources && stmt instanceof LoadField loadField) {
            // Handle field sources.
            // If a {@link LoadField} loads any source fields,
            // then records the {@link LoadField} statements.
            JField field = loadField.getFieldRef().resolveNullable();
            if (fieldSources.containsKey(field)) {
                loadedFieldSources.put(container, loadField);
            }
        }
        if (callSiteMode &&
                stmt instanceof Invoke invoke &&
                !invoke.isDynamic()) {
            // Handles call sources for the case when call-site mode is enabled.
            // If method references of any {@link Invoke}s are resolved to
            // call source method, then records the {@link Invoke} statements.
            JMethod callee = invoke.getMethodRef().resolveNullable();
            if (callSources.containsKey(callee)) {
                callSiteSources.put(container, invoke);
            }
        }
    }

    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        handleParamSource(csMethod);
        if (handleFieldSources) {
            handleFieldSource(csMethod);
        }
        if (callSiteMode) {
            handleCallSource(csMethod);
        }
    }

    private void handleParamSource(CSMethod csMethod) {
        JMethod method = csMethod.getMethod();
        if (paramSources.containsKey(method)) {
            Context context = csMethod.getContext();
            IR ir = method.getIR();
            paramSources.get(method).forEach(source -> {
                int index = source.index();
                Var param = ir.getParam(index);
                SourcePoint sourcePoint = new ParamSourcePoint(method, index);
                Type type = source.type();
                Obj taint = manager.makeTaint(sourcePoint, type);
                solver.addVarPointsTo(context, param, taint);
            });
        }
    }

    /**
     * If given method contains pre-recorded {@link LoadField} statements,
     * adds corresponding taint object to LHS of the {@link LoadField}.
     */
    private void handleFieldSource(CSMethod csMethod) {
        JMethod method = csMethod.getMethod();
        Set<LoadField> loads = loadedFieldSources.get(method);
        if (!loads.isEmpty()) {
            Context context = csMethod.getContext();
            loads.forEach(load -> {
                Var lhs = load.getLValue();
                SourcePoint sourcePoint = new FieldSourcePoint(method, load);
                JField field = load.getFieldRef().resolve();
                Type type = fieldSources.get(field);
                Obj taint = manager.makeTaint(sourcePoint, type);
                solver.addVarPointsTo(context, lhs, taint);
            });
        }
    }

    /**
     * If given method contains pre-recorded {@link Invoke} statements,
     * call {@link #processCallSource} to generate taint objects.
     */
    private void handleCallSource(CSMethod csMethod) {
        JMethod method = csMethod.getMethod();
        Set<Invoke> callSites = callSiteSources.get(method);
        if (!callSites.isEmpty()) {
            Context context = csMethod.getContext();
            callSites.forEach(callSite -> {
                JMethod callee = callSite.getMethodRef().resolve();
                callSources.get(callee).forEach(source ->
                        processCallSource(context, callSite, source));
            });
        }
    }
}
