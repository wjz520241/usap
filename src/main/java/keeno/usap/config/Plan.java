

package keeno.usap.config;

import keeno.usap.util.graph.Graph;
import keeno.usap.util.graph.SimpleGraph;

import java.util.List;
import java.util.Set;

/**
 * 包含有关分析执行计划的信息。
 *
 * @param analyses        要执行的分析列表。
 * @param dependenceGraph 描述分析之间依赖关系的图。此图用于清除未使用的分析结果。
 * @param keepResult      保留其结果的分析的ID集。
 */
public record Plan(
        List<AnalysisConfig> analyses,
        Graph<AnalysisConfig> dependenceGraph,
        Set<String> keepResult) {

    /**
     * {@link #keepResult}的特殊元素，意思是保留所有分析的结果。
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
