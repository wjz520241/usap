

package keeno.usap.language.generics;

/**
 * In <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-BaseType">
 * JVM Spec. 4.3.2 Base Types</a>,
 * a <i>base type</i> is one of the following primitive types:
 * <ul>
 *     <li>byte</li>
 *     <li>char</li>
 *     <li>double</li>
 *     <li>float</li>
 *     <li>int</li>
 *     <li>long</li>
 *     <li>short</li>
 *     <li>boolean</li>
 * </ul>
 *
 * 可以看看《深入理解java虚拟机》227页字段表集合
 */
public enum BaseType implements TypeGSignature {

    BYTE('B', "byte"),
    CHAR('C', "char"),
    DOUBLE('D', "double"),
    FLOAT('F', "float"),
    INT('I', "int"),
    LONG('J', "long"),
    SHORT('S', "short"),
    BOOLEAN('Z', "boolean");

    /**
     * Descriptor of this type.
     */
    private final char descriptor;

    /**
     * Name of this type.
     */
    private final String name;

    BaseType(char descriptor, String name) {
        this.descriptor = descriptor;
        this.name = name;
    }

    /**
     * @return the primitive type specified by specific name.
     * @throws IllegalArgumentException if given name is irrelevant to any primitive type.
     */
    public static BaseType of(char descriptor) {
        for (BaseType t : values()) {
            if (t.descriptor == descriptor) {
                return t;
            }
        }
        throw new IllegalArgumentException(descriptor + " is not base type");
    }

    @Override
    public String toString() {
        return name;
    }

}
