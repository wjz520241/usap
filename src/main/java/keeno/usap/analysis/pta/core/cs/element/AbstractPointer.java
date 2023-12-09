

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.analysis.graph.flowgraph.FlowKind;
import keeno.usap.analysis.pta.core.solver.PointerFlowEdge;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.util.collection.ArraySet;
import keeno.usap.util.collection.HybridIndexableSet;
import keeno.usap.util.collection.Sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

abstract class AbstractPointer implements Pointer {

    private PointsToSet pointsToSet;

    private final int index;

    private final Set<Pointer> successors = new HybridIndexableSet<>(true);

    private final ArrayList<PointerFlowEdge> outEdges = new ArrayList<>(4);

    private Set<Predicate<CSObj>> filters = Set.of();

    protected AbstractPointer(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public PointsToSet getPointsToSet() {
        return pointsToSet;
    }

    @Override
    public void setPointsToSet(PointsToSet pointsToSet) {
        this.pointsToSet = pointsToSet;
    }

    @Override
    public void addFilter(Predicate<CSObj> filter) {
        if (filters.isEmpty()) {
            filters = Sets.newHybridSet();
        }
        filters.add(filter);
    }

    @Override
    public Set<Predicate<CSObj>> getFilters() {
        return filters;
    }

    @Override
    public Set<CSObj> getObjects() {
        PointsToSet pts = getPointsToSet();
        return pts == null ? Set.of() : pts.getObjects();
    }

    @Override
    public Stream<CSObj> objects() {
        return getObjects().stream();
    }

    @Override
    public PointerFlowEdge getOrAddEdge(FlowKind kind, Pointer source, Pointer target) {
        if (successors.add(target)) {
            PointerFlowEdge edge = new PointerFlowEdge(kind, source, target);
            outEdges.add(edge);
            return edge;
        } else if (kind == FlowKind.OTHER) {
            for (PointerFlowEdge edge : outEdges) {
                if (edge.target().equals(target)) {
                    return edge;
                }
            }
        }
        return null;
    }

    @Override
    public Set<PointerFlowEdge> getOutEdges() {
        return Collections.unmodifiableSet(new ArraySet<>(outEdges, true));
    }

    @Override
    public int getOutDegree() {
        return outEdges.size();
    }
}
