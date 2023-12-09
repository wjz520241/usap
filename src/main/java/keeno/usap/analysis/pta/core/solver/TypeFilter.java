

package keeno.usap.analysis.pta.core.solver;

import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.language.type.NullType;
import keeno.usap.language.type.ReferenceType;
import keeno.usap.language.type.Type;
import keeno.usap.language.type.TypeSystem;

import java.util.function.Supplier;

/**
 * Transfer function that filters out the objects whose types are NOT
 * subtypes of specific type.
 */
public class TypeFilter implements Transfer {

    /**
     * The guard type.
     */
    private final Type type;

    private final TypeSystem typeSystem;

    private final Supplier<PointsToSet> ptsFactory;

    public TypeFilter(Type type, Solver solver) {
        this.type = type;
        this.typeSystem = solver.getTypeSystem();
        this.ptsFactory = solver::makePointsToSet;
    }

    @Override
    public PointsToSet apply(PointerFlowEdge edge, PointsToSet input) {
        PointsToSet result = ptsFactory.get();
        input.objects()
                .filter(o -> isAssignable(o.getObject().getType(), type))
                .forEach(result::addObject);
        return result;
    }

    private boolean isAssignable(Type from, Type to) {
        return (from instanceof NullType)
                ? to instanceof ReferenceType
                : typeSystem.isSubtype(to, from);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypeFilter that)) {
            return false;
        }
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
