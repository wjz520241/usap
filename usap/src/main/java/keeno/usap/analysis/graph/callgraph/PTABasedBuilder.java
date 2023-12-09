

package keeno.usap.analysis.graph.callgraph;

import keeno.usap.World;
import keeno.usap.analysis.pta.PointerAnalysis;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.classes.JMethod;

/**
 * Builds call graph based on pointer analysis results.
 * This builder assumes that pointer analysis has finished,
 * and it merely returns the (context-insensitive) call graph
 * obtained from pointer analysis result.
 */
class PTABasedBuilder implements CGBuilder<Invoke, JMethod> {

    @Override
    public CallGraph<Invoke, JMethod> build() {
        PointerAnalysisResult result = World.get().getResult(PointerAnalysis.ID);
        return result.getCallGraph();
    }
}
