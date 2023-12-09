

package keeno.usap.analysis.pta.client;

import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;

public class PolymorphicCallSite extends Collector {

    public static final String ID = "poly-call";

    public PolymorphicCallSite(AnalysisConfig config) {
        super(config);
    }

    @Override
    boolean isRelevant(Stmt stmt) {
        return stmt instanceof Invoke invoke &&
                (invoke.isVirtual() || invoke.isInterface());
    }

    @Override
    boolean isWanted(Stmt stmt, PointerAnalysisResult result) {
        Invoke invoke = (Invoke) stmt;
        return result.getCallGraph().getCalleesOf(invoke).size() > 1;
    }

    @Override
    String getDescription() {
        return ID;
    }
}
