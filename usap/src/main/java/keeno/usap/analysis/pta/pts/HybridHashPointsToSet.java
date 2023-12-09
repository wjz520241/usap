

package keeno.usap.analysis.pta.pts;

import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.util.collection.HybridHashSet;
import keeno.usap.util.collection.SetEx;

class HybridHashPointsToSet extends DelegatePointsToSet {

    HybridHashPointsToSet() {
        this(new HybridHashSet<>());
    }

    private HybridHashPointsToSet(SetEx<CSObj> set) {
        super(set);
    }

    @Override
    protected PointsToSet newSet(SetEx<CSObj> set) {
        return new HybridHashPointsToSet(set);
    }
}
