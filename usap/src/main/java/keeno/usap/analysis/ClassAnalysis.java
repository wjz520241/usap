

package keeno.usap.analysis;

import keeno.usap.config.AnalysisConfig;
import keeno.usap.language.classes.JClass;

/**
 * Abstract base class for all class analyses, or say, intra-class analyses.
 *
 * @param <R> result type
 */
public abstract class ClassAnalysis<R> extends Analysis {

    protected ClassAnalysis(AnalysisConfig config) {
        super(config);
    }

    /**
     * Runs this analysis for the given {@link JClass}.
     * The result will be stored in {@link JClass}. If the result is not used
     * by following analyses, then this method should return {@code null}.
     *
     * @param jclass the class to be analyzed
     * @return the analysis result for given class.
     */
    public abstract R analyze(JClass jclass);
}
