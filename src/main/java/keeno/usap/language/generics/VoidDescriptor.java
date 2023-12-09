


package keeno.usap.language.generics;

/**
 * In <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-VoidDescriptor">
 * JVM Spec. 4.3.3 VoidDescriptor</a>,
 * The <i>void</i> descriptor indicates that the method returns no value.
 */
public enum VoidDescriptor implements TypeGSignature {

    VOID('V', "void");

    /**
     * Descriptor of this type.
     */
    private final char descriptor;

    /**
     * Name of this type.
     */
    private final String name;

    VoidDescriptor(char descriptor, String name) {
        this.descriptor = descriptor;
        this.name = name;
    }

    public static boolean isVoid(char descriptor) {
        return descriptor == VOID.descriptor;
    }

    @Override
    public String toString() {
        return "void";
    }

}
