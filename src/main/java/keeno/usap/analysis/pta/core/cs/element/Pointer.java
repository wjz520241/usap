

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.analysis.graph.flowgraph.FlowKind;
import keeno.usap.analysis.pta.core.solver.PointerFlowEdge;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.language.type.Type;
import keeno.usap.util.Indexable;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 表示上下文相关指针分析（指针流图）中的所有指针（节点）。
 */
public interface Pointer extends Indexable {

    /**
     * 获取与该指针关联的指向集合。
     * 此方法可能返回 {@code null}.
     * 我们建议在指针分析完成后使用 {@link #getObjects()} 和 {@link #objects()}来访问此指针所指向的对象。
     *
     * @return the points-to set associated with this pointer.
     */
    @Nullable
    PointsToSet getPointsToSet();

    /**
     * 设置该指针的关联指向集合。
     */
    void setPointsToSet(PointsToSet pointsToSet);

    /**
     * 添加过滤器以过滤出此指针指向的对象。
     */
    void addFilter(Predicate<CSObj> filter);

    /**
     * @return all filters added to this pointer.
     */
    Set<Predicate<CSObj>> getFilters();

    /**
     * 安全地检索此指针指向的上下文相关对象。
     *
     * @return 如果{@code pointer}尚未与{@code PointsToSet}关联，则为空集；否则，返回{@code PointsToSet}中的一组对象。
     */
    Set<CSObj> getObjects();

    /**
     * 同上
     */
    Stream<CSObj> objects();

    /**
     * 添加指针流边 {@code source} -> {@code target}, 并返回边。如果边已经存在，并且 {@code kind}不是 {@link FlowKind#OTHER}, 则返回{@code null}.
     */
    PointerFlowEdge getOrAddEdge(FlowKind kind, Pointer source, Pointer target);

    /**
     * @return 指针流图中该指针的出度边。
     */
    Set<PointerFlowEdge> getOutEdges();

    /**
     * @return 指针流图中该指针的出度。
     */
    int getOutDegree();

    /**
     * @return the type of this pointer
     */
    Type getType();
}
