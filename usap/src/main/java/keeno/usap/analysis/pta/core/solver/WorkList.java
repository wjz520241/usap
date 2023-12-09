

package keeno.usap.analysis.pta.core.solver;

import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.Pointer;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.util.collection.Maps;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Represents work list in pointer analysis.
 */
final class WorkList {

    /**
     * Pointer entries to be processed.
     */
    private final Map<Pointer, PointsToSet> pointerEntries = Maps.newLinkedHashMap();

    /**
     * Call edges to be processed.
     */
    private final Queue<Edge<CSCallSite, CSMethod>> callEdges = new ArrayDeque<>();

    void addEntry(Pointer pointer, PointsToSet pointsToSet) {
        PointsToSet set = pointerEntries.get(pointer);
        if (set != null) {
            set.addAll(pointsToSet);
        } else {
            pointerEntries.put(pointer, pointsToSet.copy());
        }
    }

    void addEntry(Edge<CSCallSite, CSMethod> edge) {
        callEdges.add(edge);
    }

    Entry pollEntry() {
        if (!callEdges.isEmpty()) {
            // for correctness, we need to ensure that any call edges in
            // the work list must be processed prior to the pointer entries
            return new CallEdgeEntry(callEdges.poll());
        } else if (!pointerEntries.isEmpty()) {
            var it = pointerEntries.entrySet().iterator();
            var e = it.next();
            it.remove();
            return new PointerEntry(e.getKey(), e.getValue());
        } else {
            throw new NoSuchElementException();
        }
    }

    boolean isEmpty() {
        return pointerEntries.isEmpty() && callEdges.isEmpty();
    }

    interface Entry {
    }

    record PointerEntry(Pointer pointer, PointsToSet pointsToSet)
            implements Entry {
    }

    record CallEdgeEntry(Edge<CSCallSite, CSMethod> edge)
            implements Entry {
    }
}
