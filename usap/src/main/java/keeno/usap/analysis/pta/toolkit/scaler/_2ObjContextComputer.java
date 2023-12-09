

package keeno.usap.analysis.pta.toolkit.scaler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.toolkit.PointerAnalysisResultEx;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.graph.Graph;

class _2ObjContextComputer extends ContextComputer {

    private static final Logger logger = LogManager.getLogger(_2ObjContextComputer.class);

    private final Graph<Obj> oag;

    _2ObjContextComputer(PointerAnalysisResultEx pta, Graph<Obj> oag) {
        super(pta);
        this.oag = oag;
    }

    @Override
    String getVariantName() {
        return "2-obj";
    }

    @Override
    int computeContextNumberOf(JMethod method) {
        if (pta.getReceiverObjectsOf(method).isEmpty()) {
            logger.debug("Empty receiver: {}", method);
            return 1;
        }
        int count = 0;
        for (Obj recv : pta.getReceiverObjectsOf(method)) {
            int inDegree = oag.getInDegreeOf(recv);
            if (inDegree > 0) {
                count += inDegree;
            } else {
                // without allocator, back to 1-object
                ++count;
            }
        }
        return count;
    }
}
