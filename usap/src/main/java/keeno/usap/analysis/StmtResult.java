

package keeno.usap.analysis;

import keeno.usap.ir.stmt.Stmt;

/**
 * An interface for querying analysis results of Stmt.
 *
 * @param <R> type of analysis results
 */
public interface StmtResult<R> {

    /**
     * @return if {@code stmt} is relevant in this result.
     */
    boolean isRelevant(Stmt stmt);

    /**
     * @return analysis result of given stmt.
     */
    R getResult(Stmt stmt);
}
