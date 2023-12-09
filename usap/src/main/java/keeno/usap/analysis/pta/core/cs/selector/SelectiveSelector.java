

package keeno.usap.analysis.pta.core.cs.selector;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.language.classes.JMethod;

import java.util.function.Predicate;

/**
 * Selective context selector which applies context sensitivity
 * for part of methods and objects.
 */
class SelectiveSelector implements ContextSelector {

    /**
     * Delegate context selector.
     */
    private final ContextSelector delegate;

    /**
     * Predicate for whether a method should be analyzed with context sensitivity.
     */
    private final Predicate<JMethod> isCSMethod;

    /**
     * Predicate for whether an object should be analyzed with context sensitivity.
     */
    private final Predicate<Obj> isCSObj;

    SelectiveSelector(ContextSelector delegate,
                      Predicate<JMethod> isCSMethod, Predicate<Obj> isCSObj) {
        this.delegate = delegate;
        this.isCSMethod = isCSMethod;
        this.isCSObj = isCSObj;
    }

    @Override
    public Context getEmptyContext() {
        return delegate.getEmptyContext();
    }

    @Override
    public Context selectContext(CSCallSite callSite, JMethod callee) {
        return isCSMethod.test(callee) ?
                delegate.selectContext(callSite, callee) :
                delegate.getEmptyContext();
    }

    @Override
    public Context selectContext(CSCallSite callSite, CSObj recv, JMethod callee) {
        return isCSMethod.test(callee) ?
                delegate.selectContext(callSite, recv, callee) :
                delegate.getEmptyContext();
    }

    @Override
    public Context selectHeapContext(CSMethod method, Obj obj) {
        return isCSObj.test(obj) ?
                delegate.selectHeapContext(method, obj) :
                delegate.getEmptyContext();
    }
}
