

package keeno.usap.analysis.pta.core.cs.selector;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.heap.NewObj;

/**
 * K-limiting context selector.
 *
 * @param <T> type of context elements.
 */
abstract class KLimitingSelector<T> extends AbstractContextSelector<T> {

    /**
     * Limit of context length.
     */
    protected final int limit;

    /**
     * Limit of heap context length.
     */
    protected final int hLimit;

    /**
     * @param k  k-limit for method contexts.
     * @param hk k-limit for heap contexts.
     */
    KLimitingSelector(int k, int hk) {
        this.limit = k;
        this.hLimit = hk;
    }

    @Override
    protected Context selectNewObjContext(CSMethod method, NewObj obj) {
        return factory.makeLastK(method.getContext(), hLimit);
    }
}
