

package keeno.usap.analysis.pta.toolkit.scaler;

import keeno.usap.analysis.pta.toolkit.PointerAnalysisResultEx;
import keeno.usap.language.classes.JMethod;

/**
 * Context-insensitive analysis can be seen as the analysis where
 * all contexts are merged as 1 context.
 */
class _InsensitiveContextComputer extends ContextComputer {

    _InsensitiveContextComputer(PointerAnalysisResultEx pta) {
        super(pta);
    }

    @Override
    String getVariantName() {
        return "ci";
    }

    @Override
    int computeContextNumberOf(JMethod method) {
        return 1;
    }
}
