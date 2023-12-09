

package keeno.usap.analysis.pta.pts;

import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.util.Indexer;

import java.util.function.Supplier;

/**
 * Provides static factory methods for {@link PointsToSet}.
 */
public class PointsToSetFactory {

    private final Supplier<PointsToSet> factory;

    public PointsToSetFactory(Indexer<CSObj> objIndexer) {
        factory = () -> new HybridBitPointsToSet(objIndexer, true);
    }

    public PointsToSet make() {
        return factory.get();
    }

    /**
     * Convenient method for making one-element points-to set.
     */
    public PointsToSet make(CSObj obj) {
        PointsToSet set = make();
        set.addObject(obj);
        return set;
    }
}
