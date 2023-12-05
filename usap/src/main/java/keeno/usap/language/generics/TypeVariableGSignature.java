package keeno.usap.language.generics;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-TypeVariableSignature">
 * JVM Spec. 4.7.9.1 TypeVariableSignature</a>
 */
public final class TypeVariableGSignature implements ReferenceTypeGSignature {

    private static final ConcurrentMap<String, TypeVariableGSignature> map = new ConcurrentHashMap<>(48);

    private final String typeName;

    private TypeVariableGSignature(String typeName) {
        this.typeName = typeName;
    }

    public static TypeVariableGSignature of(String typeName) {
        return map.computeIfAbsent(typeName, TypeVariableGSignature::new);
    }

    public String getTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }

}
