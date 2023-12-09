

package keeno.usap.analysis.pta.core.cs.selector;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.context.ContextFactory;
import keeno.usap.analysis.pta.core.cs.context.TrieContext;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.heap.NewObj;
import keeno.usap.analysis.pta.core.heap.Obj;

abstract class AbstractContextSelector<T> implements ContextSelector {

    protected final ContextFactory<T> factory = new TrieContext.Factory<>();

    @Override
    public Context getEmptyContext() {
        return factory.getEmptyContext();
    }

    @Override
    public Context selectHeapContext(CSMethod method, Obj obj) {
        // Uses different strategies to select heap contexts
        // for different kinds of objects.
        if (obj instanceof NewObj) {
            return selectNewObjContext(method, (NewObj) obj);
        } else {
            return getEmptyContext();
        }
    }

    /**
     * Defines the real heap context selector for NewObj.
     */
    protected abstract Context selectNewObjContext(CSMethod method, NewObj obj);
}
