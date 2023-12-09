


package keeno.usap.language.generics;

import keeno.usap.util.Experimental;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * In <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-TypeParameter">
 * JVM Spec. 4.7.9.1 TypeParameter</a>,
 * a type parameter is described by its name, followed by any class bound and interface bounds.
 */
public final class TypeParameter implements Serializable {

    /**
     * A commonly used type parameter {@code T}.
     */
    public static final TypeParameter T = new TypeParameter("T",
            ClassTypeGSignature.JAVA_LANG_OBJECT, List.of());

    /**
     * A commonly used type parameter {@code E}.
     */
    public static final TypeParameter E = new TypeParameter("E",
            ClassTypeGSignature.JAVA_LANG_OBJECT, List.of());

    private final String typeName;

    private final ReferenceTypeGSignature classBound;

    private final List<ReferenceTypeGSignature> interfaceBounds;

    TypeParameter(String typeName,
                  ReferenceTypeGSignature classBound,
                  List<ReferenceTypeGSignature> interfaceBounds) {
        this.typeName = typeName;
        this.classBound = classBound;
        this.interfaceBounds = interfaceBounds;
    }

    @Experimental
    public String getTypeName() {
        return typeName;
    }

    @Experimental
    public ReferenceTypeGSignature getClassBound() {
        return classBound;
    }

    @Experimental
    public List<ReferenceTypeGSignature> getInterfaceBounds() {
        return interfaceBounds;
    }

    /**
     * @return e.g., {@code T}, {@code T extends java.lang.Enum<T>},
     * {@code T extends java.lang.Enum<T> & java.io.Serializable}
     */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(typeName);
        if (classBound != null && !classBound.isJavaLangObject()) {
            joiner.add("extends");
            joiner.add(classBound.toString());
        }
        if (!interfaceBounds.isEmpty()) {
            joiner.add(classBound == null ? "extends" : "&");
            joiner.add(interfaceBounds.stream().map(ReferenceTypeGSignature::toString)
                    .collect(Collectors.joining(" & ")));
        }
        return joiner.toString();
    }

}
