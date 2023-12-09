

package keeno.usap.ir.exp;

import keeno.usap.World;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.Type;
import keeno.usap.util.Hashes;

import java.util.List;

import static keeno.usap.language.classes.ClassNames.METHOD_TYPE;
import static keeno.usap.language.classes.StringReps.toDescriptor;

/**
 * Representation of java.lang.invoke.MethodType instances.
 */
public class MethodType implements ReferenceLiteral {

    private final List<Type> paramTypes;

    private final Type returnType;

    private MethodType(List<Type> paramTypes, Type returnType) {
        this.paramTypes = List.copyOf(paramTypes);
        this.returnType = returnType;
    }

    public static MethodType get(List<Type> paramTypes, Type returnType) {
        return new MethodType(paramTypes, returnType);
    }

    public List<Type> getParamTypes() {
        return paramTypes;
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    public ClassType getType() {
        return World.get().getTypeSystem().getClassType(METHOD_TYPE);
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MethodType that = (MethodType) o;
        return paramTypes.equals(that.paramTypes) &&
                returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return Hashes.hash(paramTypes, returnType);
    }

    @Override
    public String toString() {
        return "MethodType: " + toDescriptor(paramTypes, returnType);
    }
}
