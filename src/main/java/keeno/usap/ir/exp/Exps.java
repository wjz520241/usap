

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;
import keeno.usap.language.type.ReferenceType;

/**
 * Provides static utility methods for {@link Exp}.
 */
public final class Exps {

    private Exps() {
    }

    /**
     * @return {@code true} if {@code exp} can hold primitive values.
     */
    public static boolean holdsPrimitive(Exp exp) {
        return exp.getType() instanceof PrimitiveType;
    }

    /**
     * @return {@code true} if {@code exp} can hold int values.
     * Note that expressions of some primitive types other than int,
     * whose computational type is int, can also hold int values.
     * @see PrimitiveType#asInt()
     */
    public static boolean holdsInt(Exp exp) {
        return exp.getType() instanceof PrimitiveType t && t.asInt();
    }

    /**
     * @return {@code true} if {@code exp} can hold long values.
     */
    public static boolean holdsLong(Exp exp) {
        return exp.getType().equals(PrimitiveType.LONG);
    }

    /**
     * @return {@code true} if {@code exp} can hold int or long values.
     */
    public static boolean holdsInteger(Exp exp) {
        return holdsInt(exp) || holdsLong(exp);
    }

    /**
     * @return {@code true} if {@code exp} can hold reference values.
     */
    public static boolean holdsReference(Exp exp) {
        // TODO: exclude null type?
        return exp.getType() instanceof ReferenceType;
    }
}
