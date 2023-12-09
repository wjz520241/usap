

package keeno.usap.analysis.pta.toolkit.zipper;

import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.toolkit.PointerAnalysisResultEx;
import keeno.usap.analysis.pta.toolkit.util.OAGs;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.util.Canonicalizer;
import keeno.usap.util.Indexer;
import keeno.usap.util.SimpleIndexer;
import keeno.usap.util.collection.IndexerBitSet;
import keeno.usap.util.collection.Maps;

import java.util.Map;
import java.util.Set;

/**
 * For each object type t, this class compute the set of methods
 * which objects of t could potentially be their context element.
 */
class PotentialContextElement {

    /**
     * Map from each type to PCE methods of the objects of the type.
     */
    private final Map<Type, Set<JMethod>> type2PCEMethods;

    PotentialContextElement(PointerAnalysisResultEx pta,
                            ObjectAllocationGraph oag) {
        Map<Obj, Set<JMethod>> invokedMethods = OAGs.computeInvokedMethods(pta);
        Canonicalizer<Set<JMethod>> canonicalizer = new Canonicalizer<>();
        Indexer<JMethod> methodIndexer = new SimpleIndexer<>(
                pta.getBase().getCallGraph().getNodes());
        Set<Type> types = pta.getObjectTypes();
        type2PCEMethods = Maps.newConcurrentMap(types.size());
        types.parallelStream().forEach(type -> {
            Set<JMethod> methods = new IndexerBitSet<>(methodIndexer, true);
            // add invoked methods on objects of type
            for (Obj obj : pta.getObjectsOf(type)) {
                methods.addAll(invokedMethods.get(obj));
            }
            // add invoked methods on allocated objects of type
            for (Obj allocatee : oag.getAllocateesOf(type)) {
                methods.addAll(invokedMethods.get(allocatee));
            }
            type2PCEMethods.put(type, canonicalizer.get(methods));
        });
    }

    Set<JMethod> pceMethodsOf(Type type) {
        return type2PCEMethods.get(type);
    }
}
