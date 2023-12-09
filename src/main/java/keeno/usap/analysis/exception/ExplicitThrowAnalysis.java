

package keeno.usap.analysis.exception;

import keeno.usap.ir.IR;

interface ExplicitThrowAnalysis {

    /**
     * Analyzes explicit exceptions (of Throw and Invoke) in given ir
     * and store result.
     */
    void analyze(IR ir, ThrowResult result);
}
