

package keeno.usap.analysis.pta.core.solver;

import keeno.usap.ir.exp.Exp;
import keeno.usap.language.type.NullType;
import keeno.usap.language.type.PrimitiveType;
import keeno.usap.language.type.ReferenceType;
import keeno.usap.language.type.Type;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Checks whether propagation of objects is allowed.
 * The decision is made based on the type of the relevant expression and
 * the allowed types given in the constructor.
 */
public class PropagateTypes {

    private final boolean allowReference;

    private final boolean allowNull;

    private final Set<PrimitiveType> allowedPrimitives;

    /**
     * Elements of {@code types} can be:
     * <ul>
     *     <li>"reference": allow reference types</li>
     *     <li>"null" (or null): allow null type</li>
     *     <li>various primitive types: allow the corresponding primitive types</li>
     * </ul>
     */
    public PropagateTypes(List<String> types) {
        allowReference = types.contains("reference");
        allowNull = types.contains("null") || types.contains(null);
        List<PrimitiveType> primitiveTypes = types.stream()
                .filter(PrimitiveType::isPrimitiveType)
                .map(PrimitiveType::get)
                .toList();
        allowedPrimitives = primitiveTypes.isEmpty()
                ? Set.of()
                : EnumSet.copyOf(primitiveTypes);
    }

    public boolean isAllowed(Type type) {
        if (type instanceof ReferenceType) {
            return (type instanceof NullType) ? allowNull : allowReference;
        } else if (type instanceof PrimitiveType primitiveType) {
            return allowedPrimitives.contains(primitiveType);
        }
        return false;
    }

    public boolean isAllowed(Exp exp) {
        return isAllowed(exp.getType());
    }

    @Override
    public String toString() {
        return "PropagateTypes{" +
                "allowReference=" + allowReference +
                ", allowNull=" + allowNull +
                ", allowedPrimitives=" + allowedPrimitives +
                '}';
    }
}
