


package keeno.usap.language.generics;

import keeno.usap.util.Experimental;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * In <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-ClassSignature">
 * JVM Spec. 4.7.9.1 ClassSignature</a>,
 * a <i>class signature</i> encodes type information about a (possibly generic) class
 * or interface declaration. It describes any type parameters of the class or interface,
 * and lists its (possibly parameterized) direct superclass and direct superinterfaces, if any.
 * A type parameter is described by its name, followed by any class bound and interface bounds.
 */
public final class ClassGSignature implements Serializable {

    /**
     * Whether this class signature describes an interface.
     * This field is only used for precise printing.
     *
     * @see #toString()
     */
    private final boolean isInterface;

    private final List<TypeParameter> typeParams;

    private final ClassTypeGSignature superClass;

    private final List<ClassTypeGSignature> superInterfaces;

    ClassGSignature(boolean isInterface,
                    List<TypeParameter> typeParams,
                    ClassTypeGSignature superClass,
                    List<ClassTypeGSignature> superInterfaces) {
        this.isInterface = isInterface;
        this.typeParams = List.copyOf(typeParams);
        this.superClass = superClass;
        this.superInterfaces = List.copyOf(superInterfaces);
    }

    @Experimental
    public List<TypeParameter> getTypeParams() {
        return typeParams;
    }

    @Experimental
    public ClassTypeGSignature getSuperClass() {
        return superClass;
    }

    @Experimental
    public List<ClassTypeGSignature> getSuperInterfaces() {
        return superInterfaces;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(" ");
        if (!typeParams.isEmpty()) {
            joiner.add(typeParams.stream().map(TypeParameter::toString)
                    .collect(Collectors.joining(", ", "<", ">")));
        }
        if (superClass != null && !superClass.isJavaLangObject()) {
            joiner.add("extends");
            joiner.add(superClass.toString());
        }
        if (!superInterfaces.isEmpty()) {
            joiner.add(isInterface ? "extends" : "implements");
            joiner.add(superInterfaces.stream().map(ClassTypeGSignature::toString)
                    .collect(Collectors.joining(", ")));
        }
        return joiner.toString();
    }

}
