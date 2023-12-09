

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.core.heap.Descriptor;
import keeno.usap.analysis.pta.core.heap.HeapModel;
import keeno.usap.analysis.pta.core.heap.MockObj;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.language.type.Type;
import keeno.usap.util.AnalysisException;
import keeno.usap.util.collection.Sets;

import java.util.Collections;
import java.util.Set;

/**
 * Manages taint objects.
 */
class TaintManager {

    private static final Descriptor TAINT_DESC = () -> "TaintObj";

    private final HeapModel heapModel;

    private final Set<Obj> taintObjs = Sets.newHybridSet();

    TaintManager(HeapModel heapModel) {
        this.heapModel = heapModel;
    }

    /**
     * Makes a taint object for given source point and type.
     *
     * @param sourcePoint where the taint is generated
     * @param type        type of the taint object
     * @return the taint object for given source and type.
     */
    Obj makeTaint(SourcePoint sourcePoint, Type type) {
        Obj taint = heapModel.getMockObj(TAINT_DESC, sourcePoint, type, false);
        taintObjs.add(taint);
        return taint;
    }

    /**
     * @return true if given obj represents a taint object, otherwise false.
     */
    boolean isTaint(Obj obj) {
        return obj instanceof MockObj mockObj &&
                mockObj.getDescriptor().equals(TAINT_DESC);
    }

    /**
     * @return the source point of given taint object.
     * @throws AnalysisException if given object is not a taint object.
     */
    SourcePoint getSourcePoint(Obj obj) {
        if (isTaint(obj)) {
            return (SourcePoint) obj.getAllocation();
        }
        throw new AnalysisException(obj + " is not a taint object");
    }

    /**
     * @return all taint objects generated via this manager.
     */
    Set<Obj> getTaintObjs() {
        return Collections.unmodifiableSet(taintObjs);
    }
}
