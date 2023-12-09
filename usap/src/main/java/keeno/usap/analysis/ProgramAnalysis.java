

package keeno.usap.analysis;

import keeno.usap.config.AnalysisConfig;

/**
 * Abstract base class for all whole-program analyses.
 *
 * @param <R> result type
 */
public abstract class ProgramAnalysis<R> extends Analysis {

    protected ProgramAnalysis(AnalysisConfig config) {
        super(config);
    }

    /**
     * Runs this analysis for the whole program.
     * If the result is not used by following analyses, then this method
     * should return {@code null}.
     *
     * @return the analysis result for the whole program.
     * The result will be stored in {@link keeno.usap.World}.
     */
    public abstract R analyze();
}
