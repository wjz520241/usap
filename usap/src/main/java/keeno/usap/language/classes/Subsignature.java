

package keeno.usap.language.classes;

import keeno.usap.World;
import keeno.usap.language.type.Type;
import keeno.usap.util.InternalCanonicalized;
import keeno.usap.util.collection.Maps;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Method name and descriptor.
 */
@InternalCanonicalized
public class Subsignature implements Serializable {

    // Subsignatures of special methods
    public static final String CLINIT = "void <clinit>()";

    public static final String NO_ARG_INIT = "void <init>()";

    private static final ConcurrentMap<String, Subsignature> map
            = Maps.newConcurrentMap();

    static {
        World.registerResetCallback(map::clear);
    }

    private final String subsig;

    public static Subsignature get(
            String name, List<Type> parameterTypes, Type returnType) {
        return get(StringReps.toSubsignature(name, parameterTypes, returnType));
    }

    public static Subsignature get(String subsig) {
        return map.computeIfAbsent(subsig, Subsignature::new);
    }

    /**
     * @return subsignature of no-arg constructor.
     */
    public static Subsignature getNoArgInit() {
        return get(NO_ARG_INIT);
    }

    /**
     * @return subsignature of static initializer (clinit).
     */
    public static Subsignature getClinit() {
        return get(CLINIT);
    }

    private Subsignature(String subsig) {
        this.subsig = subsig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subsignature that)) {
            return false;
        }

        return subsig.equals(that.subsig);
    }

    @Override
    public int hashCode() {
        return subsig.hashCode();
    }

    @Override
    public String toString() {
        return subsig;
    }
}
