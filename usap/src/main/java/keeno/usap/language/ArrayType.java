package keeno.usap.language;

/**
 * A[] a = ...;
 * B继承于A
 * B b = new B();
 * a[i] = b;
 *
 * @param baseType    A
 * @param dimensions  a.length
 * @param elementType B
 */
public record ArrayType(Type baseType, int dimensions, Type elementType) implements ReferenceType {
    @Override
    public String getName() {
        return elementType + "[]";
    }

    @Override
    public String toString() {
        return getName();
    }
}
