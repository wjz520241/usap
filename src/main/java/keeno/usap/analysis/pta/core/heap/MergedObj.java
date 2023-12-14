

package keeno.usap.analysis.pta.core.heap;

import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;

import java.util.Optional;
import java.util.Set;

import static keeno.usap.util.collection.Sets.newSet;

/**
 * 表示一组合并的对象。
 */
public class MergedObj extends Obj {

    private final String name;

    private final Type type;

    /**
     * 该合并对象表示的对象集。
     */
    private final Set<Obj> representedObjs = newSet();

    /**
     * 此合并对象的代表对象。它是添加的第一个对象。
     */
    private Obj representative;

    public MergedObj(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public void addRepresentedObj(Obj obj) {
        setRepresentative(obj);
        representedObjs.add(obj);
    }

    private void setRepresentative(Obj obj) {
        if (representative == null) {
            representative = obj;
        }
    }

    @Override
    public Set<Obj> getAllocation() {
        return representedObjs;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Optional<JMethod> getContainerMethod() {
        return representative != null ?
                representative.getContainerMethod() :
                Optional.empty();
    }

    @Override
    public Type getContainerType() {
        return representative != null ?
                representative.getContainerType() : type;
    }

    @Override
    public String toString() {
        return "MergedObj{" + name + "}";
    }
}
