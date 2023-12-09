

package keeno.usap.analysis.dataflow.analysis;

import keeno.usap.analysis.MethodAnalysis;
import keeno.usap.analysis.dataflow.fact.DataflowResult;
import keeno.usap.analysis.dataflow.solver.Solver;
import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.analysis.graph.cfg.CFGBuilder;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.IR;

/**
 * Driver for performing a specific kind of data-flow analysis for a method.
 */
public abstract class AnalysisDriver<Node, Fact>
        extends MethodAnalysis<DataflowResult<Node, Fact>> {

    protected AnalysisDriver(AnalysisConfig config) {
        super(config);
    }

    @Override
    public DataflowResult<Node, Fact> analyze(IR ir) {
        CFG<Node> cfg = ir.getResult(CFGBuilder.ID);
        DataflowAnalysis<Node, Fact> analysis = makeAnalysis(cfg);
        Solver<Node, Fact> solver = Solver.getSolver();
        return solver.solve(analysis);
    }

    /**
     * Creates an analysis object for given cfg.
     */
    protected abstract DataflowAnalysis<Node, Fact> makeAnalysis(CFG<Node> cfg);
}
