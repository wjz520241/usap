

package keeno.usap.analysis.pta.core.heap;

import keeno.usap.config.AnalysisOptions;
import keeno.usap.ir.stmt.New;

/**
 * 对堆内存进行建模，AllocationSite是堆内存模型中最有用最流行的，参考自《软件分析》第八节
 */
public class AllocationSiteBasedModel extends AbstractHeapModel {

    public AllocationSiteBasedModel(AnalysisOptions options) {
        super(options);
    }

    @Override
    protected Obj doGetObj(New allocSite) {
        return getNewObj(allocSite);
    }
}
