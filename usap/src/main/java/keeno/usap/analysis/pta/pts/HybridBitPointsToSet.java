

package keeno.usap.analysis.pta.pts;

import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.util.Indexer;
import keeno.usap.util.collection.HybridBitSet;
import keeno.usap.util.collection.SetEx;

class HybridBitPointsToSet extends DelegatePointsToSet {

    public HybridBitPointsToSet(Indexer<CSObj> indexer, boolean isSparse) {
        this(new HybridBitSet<>(indexer, isSparse));
    }

    private HybridBitPointsToSet(SetEx<CSObj> set) {
        super(set);
    }

    @Override
    protected PointsToSet newSet(SetEx<CSObj> set) {
        return new HybridBitPointsToSet(set);
    }
}
