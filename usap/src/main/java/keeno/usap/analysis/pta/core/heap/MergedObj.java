

package keeno.usap.analysis.pta.core.heap;

import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;

import java.util.Optional;
import java.util.Set;

import static keeno.usap.util.collection.Sets.newSet;

/**
 * Represents a set of merged objects.
 */
public class MergedObj extends Obj {

    private final String name;

    private final Type type;

    /**
     * Set of objects represented by this merged object.
     */
    private final Set<Obj> representedObjs = newSet();

    /**
     * The representative object of this merged object. It is the first
     * object added.
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
