

package keeno.usap.analysis.pta.toolkit.scaler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.toolkit.PointerAnalysisResultEx;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.util.collection.Sets;
import keeno.usap.util.graph.Graph;

import java.util.List;
import java.util.Set;

class _2TypeContextComputer extends ContextComputer {

    private static final Logger logger = LogManager.getLogger(_2TypeContextComputer.class);

    private final Graph<Obj> oag;

    _2TypeContextComputer(PointerAnalysisResultEx pta, Graph<Obj> oag) {
        super(pta);
        this.oag = oag;
    }

    @Override
    String getVariantName() {
        return "2-type";
    }

    @Override
    int computeContextNumberOf(JMethod method) {
        if (pta.getReceiverObjectsOf(method).isEmpty()) {
            logger.debug("Empty receiver: {}", method);
            return 1;
        }
        Set<List<Type>> contexts = Sets.newHybridSet();
        for (Obj recv : pta.getReceiverObjectsOf(method)) {
            int inDegree = oag.getInDegreeOf(recv);
            if (inDegree > 0) {
                oag.getPredsOf(recv).forEach(pred ->
                        contexts.add(List.of(pred.getContainerType(),
                                recv.getContainerType())));
            } else { // without allocator, back to 1-type
                contexts.add(List.of(recv.getContainerType()));
            }
        }
        return contexts.size();
    }
}
