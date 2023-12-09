

package keeno.usap.analysis.sideeffect;

import keeno.usap.World;
import keeno.usap.analysis.ProgramAnalysis;
import keeno.usap.analysis.pta.PointerAnalysis;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.config.AnalysisConfig;

public class SideEffectAnalysis extends ProgramAnalysis<SideEffect> {

    public static final String ID = "side-effect";

    /**
     * Whether the analysis only tracks the modifications on the objects
     * created in application code.
     */
    private final boolean onlyApp;

    public SideEffectAnalysis(AnalysisConfig config) {
        super(config);
        onlyApp = getOptions().getBoolean("only-app");
    }

    @Override
    public SideEffect analyze() {
        PointerAnalysisResult pta = World.get().getResult(PointerAnalysis.ID);
        return new TopologicalSolver(onlyApp).solve(pta);
    }
}
