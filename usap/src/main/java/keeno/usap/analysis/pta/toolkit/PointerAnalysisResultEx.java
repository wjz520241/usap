

package keeno.usap.analysis.pta.toolkit;

import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;

import java.util.Set;

/**
 * Extended version {@link PointerAnalysisResult}.
 * <p>
 * Unlike {@link PointerAnalysisResult} which only provides results directly
 * computed from pointer analysis, this class provides more commonly-used results
 * that are indirectly derived from original pointer analysis result.
 */
public interface PointerAnalysisResultEx {

    /**
     * @return the base pointer analysis result.
     */
    PointerAnalysisResult getBase();

    /**
     * @return the methods whose receiver objects contain obj.
     */
    Set<JMethod> getMethodsInvokedOn(Obj obj);

    /**
     * @return the receiver objects of given method.
     */
    Set<Obj> getReceiverObjectsOf(JMethod method);

    /**
     * @return the objects that are allocated in given method.
     */
    Set<Obj> getObjectsAllocatedIn(JMethod method);

    /**
     * @return the objects of given type.
     */
    Set<Obj> getObjectsOf(Type type);

    /**
     * @return types of all reachable objects in pointer analysis.
     */
    Set<Type> getObjectTypes();
}
