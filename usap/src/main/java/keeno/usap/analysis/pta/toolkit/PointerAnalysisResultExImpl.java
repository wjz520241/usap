

package keeno.usap.analysis.pta.toolkit;

import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.ir.exp.Var;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

import java.util.Set;

public class PointerAnalysisResultExImpl implements PointerAnalysisResultEx {

    private final PointerAnalysisResult base;

    /**
     * Constructs an extended pointer analysis result.
     *
     * @param base      base pointer analysis result
     * @param eagerInit whether initialize all fields eagerly; if this result
     *                  will be accessed in concurrent setting, then the caller
     *                  should give {@code true}.
     */
    public PointerAnalysisResultExImpl(
            PointerAnalysisResult base, boolean eagerInit) {
        this.base = base;
        if (eagerInit) {
            initMethodReceiverObjects();
            initAllocatedObjects();
            initType2Objects();
        }
    }

    @Override
    public PointerAnalysisResult getBase() {
        return base;
    }

    /**
     * Map from each receiver object to the methods invoked on it.
     */
    private MultiMap<Obj, JMethod> recv2Methods;

    @Override
    public Set<JMethod> getMethodsInvokedOn(Obj obj) {
        MultiMap<Obj, JMethod> map = recv2Methods;
        if (map == null) {
            initMethodReceiverObjects();
            map = recv2Methods;
        }
        return map.get(obj);
    }

    /**
     * Map from each method to its receiver objects.
     */
    private MultiMap<JMethod, Obj> method2Recvs;

    @Override
    public Set<Obj> getReceiverObjectsOf(JMethod method) {
        MultiMap<JMethod, Obj> map = method2Recvs;
        if (map == null) {
            initMethodReceiverObjects();
            map = method2Recvs;
        }
        return map.get(method);
    }

    private void initMethodReceiverObjects() {
        MultiMap<Obj, JMethod> r2m = Maps.newMultiMap();
        MultiMap<JMethod, Obj> m2r = Maps.newMultiMap();
        for (JMethod method : base.getCallGraph()) {
            if (!method.isStatic()) {
                Var thisVar = method.getIR().getThis();
                for (Obj recv : base.getPointsToSet(thisVar)) {
                    r2m.put(recv, method);
                    m2r.put(method, recv);
                }
            }
        }
        recv2Methods = r2m;
        method2Recvs = m2r;
    }

    /**
     * Map from each method to the objects allocated in it.
     */
    private MultiMap<JMethod, Obj> method2Objs;

    @Override
    public Set<Obj> getObjectsAllocatedIn(JMethod method) {
        MultiMap<JMethod, Obj> map = method2Objs;
        if (map == null) {
            initAllocatedObjects();
            map = method2Objs;
        }
        return map.get(method);
    }

    private void initAllocatedObjects() {
        MultiMap<JMethod, Obj> map = Maps.newMultiMap();
        for (Obj obj : base.getObjects()) {
            obj.getContainerMethod().ifPresent(m -> map.put(m, obj));
        }
        method2Objs = map;
    }

    /**
     * Map from each type to the objects of the type.
     */
    private MultiMap<Type, Obj> type2Objs;

    @Override
    public Set<Obj> getObjectsOf(Type type) {
        MultiMap<Type, Obj> map = type2Objs;
        if (map == null) {
            initType2Objects();
            map = type2Objs;
        }
        return map.get(type);
    }

    @Override
    public Set<Type> getObjectTypes() {
        MultiMap<Type, Obj> map = type2Objs;
        if (map == null) {
            initType2Objects();
            map = type2Objs;
        }
        return map.keySet();
    }

    private void initType2Objects() {
        MultiMap<Type, Obj> map = Maps.newMultiMap();
        for (Obj obj : base.getObjects()) {
            map.put(obj.getType(), obj);
        }
        type2Objs = map;
    }
}
