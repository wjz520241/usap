

package keeno.usap.config;

import keeno.usap.util.graph.Graph;
import keeno.usap.util.graph.SimpleGraph;

import java.util.List;
import java.util.Set;

/**
 * Contains information about analysis execution plan.
 *
 * @param analyses        list of analyses to be executed.
 * @param dependenceGraph graph that describes dependencies among analyses.
 *                        This graph is used to clear unused analysis results.
 * @param keepResult      set of IDs for the analyses whose results are kept.
 */
public record Plan(
        List<AnalysisConfig> analyses,
        Graph<AnalysisConfig> dependenceGraph,
        Set<String> keepResult) {

    /**
     * Special element for {@link #keepResult}, which means
     * to keep results of all analyses.
     */
    public static final String KEEP_ALL = "$KEEP-ALL";

    private static final Plan EMPTY = new Plan(List.of(), new SimpleGraph<>(), Set.of());

    /**
     * @return an empty plan.
     */
    public static Plan emptyPlan() {
        return EMPTY;
    }
}
