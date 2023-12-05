package keeno.usap.language.generics;

/**
 * In <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-ReferenceTypeSignature">
 * JVM Spec. 4.7.9.1 ReferenceTypeSignature</a>,
 * A <i>reference type signature</i> represents a reference type of
 * the Java programming language, that is,
 * a class or interface type({@link ClassTypeGSignature}),
 * a type variable({@link TypeVariableGSignature}),
 * or an array type({@link ArrayTypeGSignature}).
 */
public sealed interface ReferenceTypeGSignature
        extends TypeGSignature
        permits ArrayTypeGSignature, ClassTypeGSignature, TypeVariableGSignature {

    default boolean isJavaLangObject() {
        return false;
    }

}
