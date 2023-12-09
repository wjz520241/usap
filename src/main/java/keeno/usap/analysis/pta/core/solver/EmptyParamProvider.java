

package keeno.usap.analysis.pta.core.solver;

import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.language.classes.JField;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.TwoKeyMultiMap;

import java.util.Set;

/**
 * This {@link ParamProvider} ignored all parameters.
 */
public enum EmptyParamProvider implements ParamProvider {

    INSTANCE;

    public static EmptyParamProvider get() {
        return INSTANCE;
    }

    @Override
    public Set<Obj> getThisObjs() {
        return Set.of();
    }

    @Override
    public Set<Obj> getParamObjs(int i) {
        return Set.of();
    }

    @Override
    public TwoKeyMultiMap<Obj, JField, Obj> getFieldObjs() {
        return Maps.emptyTwoKeyMultiMap();
    }

    @Override
    public MultiMap<Obj, Obj> getArrayObjs() {
        return Maps.emptyMultiMap();
    }
}
