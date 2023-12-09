

package keeno.usap.language.classes;

import java.util.Set;

public enum Modifier {

    PUBLIC,
    PRIVATE,
    PROTECTED,
    STATIC,
    FINAL,
    SYNCHRONIZED,
    VOLATILE,
    TRANSIENT,
    NATIVE,
    INTERFACE,
    ABSTRACT,
    STRICTFP,

    BRIDGE,
    VARARGS,
    SYNTHETIC,
    ANNOTATION,
    ENUM,
    MANDATED;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static boolean hasPublic(Set<Modifier> modifiers) {
        return modifiers.contains(PUBLIC);
    }

    public static boolean hasProtected(Set<Modifier> modifiers) {
        return modifiers.contains(PROTECTED);
    }

    public static boolean hasPrivate(Set<Modifier> modifiers) {
        return modifiers.contains(PRIVATE);
    }

    public static boolean hasStatic(Set<Modifier> modifiers) {
        return modifiers.contains(STATIC);
    }

    public static boolean hasFinal(Set<Modifier> modifiers) {
        return modifiers.contains(FINAL);
    }

    public static boolean hasSynchronized(Set<Modifier> modifiers) {
        return modifiers.contains(SYNCHRONIZED);
    }

    public static boolean hasVolatile(Set<Modifier> modifiers) {
        return modifiers.contains(VOLATILE);
    }

    public static boolean hasTransient(Set<Modifier> modifiers) {
        return modifiers.contains(TRANSIENT);
    }

    public static boolean hasNative(Set<Modifier> modifiers) {
        return modifiers.contains(NATIVE);
    }

    public static boolean hasInterface(Set<Modifier> modifiers) {
        return modifiers.contains(INTERFACE);
    }

    public static boolean hasAbstract(Set<Modifier> modifiers) {
        return modifiers.contains(ABSTRACT);
    }

    public static boolean hasStrictFP(Set<Modifier> modifiers) {
        return modifiers.contains(STRICTFP);
    }

    public static boolean hasSynthetic(Set<Modifier> modifiers) {
        return modifiers.contains(SYNTHETIC);
    }

    public static boolean hasAnnotation(Set<Modifier> modifiers) {
        return modifiers.contains(ANNOTATION);
    }

    public static boolean hasEnum(Set<Modifier> modifiers) {
        return modifiers.contains(ENUM);
    }
}
