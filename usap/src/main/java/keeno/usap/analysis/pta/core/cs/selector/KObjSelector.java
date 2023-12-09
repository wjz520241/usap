

package keeno.usap.analysis.pta.core.cs.selector;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.language.classes.JMethod;

class KObjSelector extends KLimitingSelector<Obj> {

    public KObjSelector(int k, int hk) {
        super(k, hk);
    }

    @Override
    public Context selectContext(CSCallSite callSite, JMethod callee) {
        return callSite.getContext();
    }

    @Override
    public Context selectContext(CSCallSite callSite, CSObj recv, JMethod callee) {
        return factory.append(recv.getContext(), recv.getObject(), limit);
    }
}
