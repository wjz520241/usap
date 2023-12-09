


package keeno.usap.language.generics;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-ClassTypeSignature">
 * JVM Spec. 4.7.9.1 ClassTypeSignature</a>,
 * a <i>class type signature</i> represents a (possibly parameterized) class or interface type.
 * For example, the bytecode signature and the corresponding Java generic are:
 * <ul>
 *     <li>{@code Ljava/lang/String;} and {@code String}</li>
 *     <li>{@code Ljava/util/HashMap<TK;TV;>;} and {@code java.util.HashMap<K, V>}</li>
 *     <li>
 *         {@code Lorg/example/Generic<TT1;>.Inner1<TT2;>.Inner2<TT3;>;}
 *         and {@code org.example.Generic<T1>.Inner1<T2>.Inner2<T3>}
 *     </li>
 * </ul>
 */
public final class ClassTypeGSignature
        implements ReferenceTypeGSignature {

    public static final String JAVA_LANG = "java.lang";

    public static final String OBJECT = "Object";

    public static final ClassTypeGSignature JAVA_LANG_OBJECT =
            new ClassTypeGSignature(JAVA_LANG, List.of(
                    new SimpleClassTypeGSignature(OBJECT, List.of())));

    /**
     * package name. For example, {@code org.example}
     */
    @Nullable
    private final String packageName;

    /**
     * class names and their type arguments. For example,
     * <ul>
     *     <li>{@code Generic} and {@code T1}</li>
     *     <li>{@code Inner1} and {@code T2}</li>
     *     <li>{@code Inner2} and {@code T3}</li>
     * </ul>
     */
    private final List<SimpleClassTypeGSignature> signatures;

    ClassTypeGSignature(@Nullable String packageName,
                        List<SimpleClassTypeGSignature> signatures) {
        this.packageName = packageName;
        this.signatures = signatures;
    }

    @Nullable
    public String getPackageName() {
        return packageName;
    }

    public List<SimpleClassTypeGSignature> getSignatures() {
        return signatures;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (packageName != null) {
            sb.append(packageName).append('.');
        }
        sb.append(signatures.stream().map(SimpleClassTypeGSignature::toString)
                .collect(Collectors.joining(".")));
        return sb.toString();
    }

    @Override
    public boolean isJavaLangObject() {
        return JAVA_LANG.equals(packageName)
                && signatures.size() == 1
                && OBJECT.equals(signatures.get(0).className());
    }

    public record SimpleClassTypeGSignature(String className,
                                            List<TypeArgument> typeArgs)
            implements Serializable {

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(className);
            if (!typeArgs.isEmpty()) {
                sb.append(typeArgs.stream()
                        .map(TypeArgument::toString)
                        .collect(Collectors.joining(", ", "<", ">")));
            }
            return sb.toString();
        }

    }

}
