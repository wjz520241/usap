

package keeno.usap.language.type;

public record ArrayType(Type baseType, int dimensions, Type elementType)
        implements ReferenceType {

    @Override
    public String getName() {
        return elementType + "[]";
    }

    @Override
    public String toString() {
        return getName();
    }
}
