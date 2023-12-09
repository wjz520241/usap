

package keeno.usap.analysis.pta.core.cs.selector;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.language.classes.JMethod;

/**
 * Represents context sensitivity variants.
 */
public interface ContextSelector {

    /**
     * @return the empty context that does not contain any context elements.
     */
    Context getEmptyContext();

    /**
     * Selects contexts for static methods.
     *
     * @param callSite the (context-sensitive) call site.
     * @param callee   the callee.
     * @return the context for the callee.
     */
    Context selectContext(CSCallSite callSite, JMethod callee);

    /**
     * Selects contexts for instance methods.
     *
     * @param callSite the (context-sensitive) call site.
     * @param recv     the (context-sensitive) receiver object for the callee.
     * @param callee   the callee.
     * @return the context for the callee.
     */
    Context selectContext(CSCallSite callSite, CSObj recv, JMethod callee);

    /**
     * Selects heap contexts for new-created abstract objects.
     *
     * @param method the (context-sensitive) method that contains the
     *               allocation site of the new-created object.
     * @param obj    the new-created object.
     * @return the heap context for the object.
     */
    Context selectHeapContext(CSMethod method, Obj obj);
}
