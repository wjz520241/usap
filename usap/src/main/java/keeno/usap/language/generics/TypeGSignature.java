package keeno.usap.language.generics;


import java.io.Serializable;

/**
 * In <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-JavaTypeSignature">
 * JVM Spec. 4.7.9.1 JavaTypeSignature</a>,
 * A <i>Java type signature</i> represents either a reference type
 * or a primitive type of the Java programming language.
 * <br>
 * In our implementation, for convenience, {@link VoidDescriptor} is
 * also a {@link TypeGSignature}.
 */
public sealed interface TypeGSignature extends Serializable
        permits ReferenceTypeGSignature, BaseType, VoidDescriptor {
}
