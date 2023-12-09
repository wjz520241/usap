


package keeno.usap.language.generics;

import keeno.usap.util.Experimental;

/**
 * In <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-ArrayTypeSignature">
 * JVM Spec. 4.7.9.1 ArrayTypeSignature</a>,
 * an <i>array type signature</i> represents one dimension of an array type.
 * For example, the bytecode signature and the corresponding Java generic are:
 * <ul>
 *     <li>{@code [[B} and {@code byte[][]}</li>
 *     <li>{@code [[Ljava/lang/String;} and {@code String[][]}</li>
 *     <li>{@code [[Ljava/util/HashMap<TK;TV;>;} and {@code java.util.HashMap<K, V>[][]}</li>
 * </ul>
 * In our implementation, we use {@link ArrayTypeGSignature} to represent
 * an n-dimensions({@link #dimensions}) array for a specific type({@link #baseTypeGSig}).
 */
public final class ArrayTypeGSignature implements ReferenceTypeGSignature {

    private final int dimensions;

    private final TypeGSignature baseTypeGSig;

    ArrayTypeGSignature(int dimensions, TypeGSignature baseTypeGSig) {
        this.dimensions = dimensions;
        this.baseTypeGSig = baseTypeGSig;
    }

    public int getDimensions() {
        return dimensions;
    }

    @Experimental
    public TypeGSignature getBaseTypeGSignature() {
        return baseTypeGSig;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(baseTypeGSig.toString());
        for (int i = 0; i < dimensions; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }
}
