

package keeno.usap.analysis.dataflow.fact;

import keeno.usap.analysis.StmtResult;
import keeno.usap.ir.stmt.Stmt;

/**
 * An interface for querying data-flow results.
 *
 * @param <Node> type of graph nodes
 * @param <Fact> type of data-flow facts
 */
public interface NodeResult<Node, Fact> extends StmtResult<Fact> {

    /**
     * @return the flowing-in fact of given node.
     */
    Fact getInFact(Node node);

    /**
     * @return the flowing-out fact of given node.
     */
    Fact getOutFact(Node node);

    /**
     * Typically, all {@code stmt}s are relevant in {@code NodeResult}.
     *
     * @return {@code true}.
     */
    @Override
    default boolean isRelevant(Stmt stmt) {
        return true;
    }

    /**
     * {@link NodeResult} is designed to be compatible with CFGs of both
     * stmt nodes and block nodes. When the node result instance represent
     * results of stmt nodes, it can be used as a {@link StmtResult}.
     *
     * @return out fact as the analysis result for given stmt.
     */
    @Override
    default Fact getResult(Stmt stmt) {
        return getOutFact((Node) stmt);
    }
}
