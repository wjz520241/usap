

package keeno.usap.analysis.pta.core.cs.selector;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.heap.NewObj;
import keeno.usap.language.classes.JMethod;

/**
 * 用于上下文不敏感分析的选择器。上下文不敏感不使用任何上下文元素，因此上下文元素的类型无关紧要
 */
class ContextInsensitiveSelector extends AbstractContextSelector<Void> {

    @Override
    public Context selectContext(CSCallSite callSite, JMethod callee) {
        return getEmptyContext();
    }

    @Override
    public Context selectContext(CSCallSite callSite, CSObj recv, JMethod callee) {
        return getEmptyContext();
    }

    @Override
    protected Context selectNewObjContext(CSMethod method, NewObj obj) {
        return getEmptyContext();
    }
}
