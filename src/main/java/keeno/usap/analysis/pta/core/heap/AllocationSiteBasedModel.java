

package keeno.usap.analysis.pta.core.heap;

import keeno.usap.config.AnalysisOptions;
import keeno.usap.ir.stmt.New;

public class AllocationSiteBasedModel extends AbstractHeapModel {

    public AllocationSiteBasedModel(AnalysisOptions options) {
        super(options);
    }

    @Override
    protected Obj doGetObj(New allocSite) {
        return getNewObj(allocSite);
    }
}
