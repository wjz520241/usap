

package keeno.usap.analysis.dataflow.solver;

import keeno.usap.analysis.dataflow.analysis.DataflowAnalysis;
import keeno.usap.analysis.dataflow.fact.DataflowResult;
import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.analysis.graph.cfg.CFGNodeIndexer;
import keeno.usap.util.collection.IndexMap;

/**
 * Provides common functionalities for {@link Solver}.
 *
 * @param <Node> type of CFG nodes
 * @param <Fact> type of data-flow facts
 */
abstract class AbstractSolver<Node, Fact> implements Solver<Node, Fact> {

    @Override
    public DataflowResult<Node, Fact> solve(DataflowAnalysis<Node, Fact> analysis) {
        DataflowResult<Node, Fact> result = initialize(analysis);
        doSolve(analysis, result);
        return result;
    }

    /**
     * Creates and initializes a new data-flow result for given CFG.
     *
     * @return the initialized data-flow result
     */
    private DataflowResult<Node, Fact> initialize(DataflowAnalysis<Node, Fact> analysis) {
        CFG<Node> cfg = analysis.getCFG();
        var indexer = new CFGNodeIndexer<>(cfg);
        DataflowResult<Node, Fact> result = new DataflowResult<>(
                new IndexMap<>(indexer, cfg.getNumberOfNodes()),
                new IndexMap<>(indexer, cfg.getNumberOfNodes()));
        if (analysis.isForward()) {
            initializeForward(analysis, result);
        } else {
            initializeBackward(analysis, result);
        }
        return result;
    }

    protected void initializeForward(DataflowAnalysis<Node, Fact> analysis,
                                     DataflowResult<Node, Fact> result) {
        CFG<Node> cfg = analysis.getCFG();
        // initialize entry
        Node entry = cfg.getEntry();
        result.setInFact(entry, analysis.newBoundaryFact());
        result.setOutFact(entry, analysis.newBoundaryFact());
        cfg.forEach(node -> {
            // skip entry which has been initialized
            if (cfg.isEntry(node)) {
                return;
            }
            // initialize in & out fact
            result.setInFact(node, analysis.newInitialFact());
            result.setOutFact(node, analysis.newInitialFact());
        });
    }

    protected void initializeBackward(DataflowAnalysis<Node, Fact> analysis,
                                      DataflowResult<Node, Fact> result) {
        CFG<Node> cfg = analysis.getCFG();
        // initialize exit
        Node exit = cfg.getExit();
        result.setInFact(exit, analysis.newBoundaryFact());
        result.setOutFact(exit, analysis.newBoundaryFact());
        cfg.forEach(node -> {
            // skip exit which has been initialized
            if (cfg.isExit(node)) {
                return;
            }
            // initialize in fact
            result.setInFact(node, analysis.newInitialFact());
            result.setOutFact(node, analysis.newInitialFact());
        });
    }

    /**
     * Solves the data-flow problem for given analysis.
     */
    private void doSolve(DataflowAnalysis<Node, Fact> analysis,
                         DataflowResult<Node, Fact> result) {
        if (analysis.isForward()) {
            doSolveForward(analysis, result);
        } else {
            doSolveBackward(analysis, result);
        }
    }

    protected abstract void doSolveForward(DataflowAnalysis<Node, Fact> analysis,
                                           DataflowResult<Node, Fact> result);

    protected abstract void doSolveBackward(DataflowAnalysis<Node, Fact> analysis,
                                            DataflowResult<Node, Fact> result);
}
