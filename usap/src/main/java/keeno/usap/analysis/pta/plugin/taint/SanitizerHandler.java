

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSManager;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.ir.IR;
import keeno.usap.ir.exp.Var;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

import java.util.function.Predicate;

/**
 * Handles sanitizers in taint analysis.
 */
class SanitizerHandler extends OnFlyHandler {

    private final MultiMap<JMethod, ParamSanitizer> paramSanitizers = Maps.newMultiMap();

    private final CSManager csManager;

    /**
     * Used to filter out taint objects from points-to set.
     */
    private final Predicate<CSObj> taintFilter;

    SanitizerHandler(HandlerContext context) {
        super(context);
        csManager = solver.getCSManager();
        taintFilter = o -> !context.manager().isTaint(o.getObject());
        context.config().paramSanitizers()
                .forEach(s -> this.paramSanitizers.put(s.method(), s));
    }

    /**
     *
     * Handles parameter sanitizers.
     */
    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        JMethod method = csMethod.getMethod();
        if (paramSanitizers.containsKey(method)) {
            Context context = csMethod.getContext();
            IR ir = method.getIR();
            paramSanitizers.get(method).forEach(sanitizer -> {
                int index = sanitizer.index();
                Var param = ir.getParam(index);
                CSVar csParam = csManager.getCSVar(context, param);
                solver.addPointerFilter(csParam, taintFilter);
            });
        }
    }
}
