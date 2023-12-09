

package keeno.usap.analysis.dataflow.solver;

import keeno.usap.analysis.dataflow.analysis.DataflowAnalysis;
import keeno.usap.analysis.dataflow.fact.DataflowResult;

/**
 * Interface of data-flow analysis solver.
 *
 * @param <Node> type of control-flow graph nodes
 * @param <Fact> type of data facts
 */
public interface Solver<Node, Fact> {

    /**
     * The default solver.
     */
    @SuppressWarnings("rawtypes")
    Solver SOLVER = new WorkListSolver<>();

    /**
     * Static factory method for obtaining a solver.
     */
    @SuppressWarnings("unchecked")
    static <Node, Fact> Solver<Node, Fact> getSolver() {
        return (Solver<Node, Fact>) SOLVER;
    }

    /**
     * Solves the given analysis problem.
     *
     * @return the data-flow analysis result
     */
    DataflowResult<Node, Fact> solve(DataflowAnalysis<Node, Fact> analysis);
}
