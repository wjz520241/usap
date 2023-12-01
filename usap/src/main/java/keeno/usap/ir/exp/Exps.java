package keeno.usap.ir.exp;

import keeno.usap.language.PrimitiveType;
import keeno.usap.language.ReferenceType;

/**
 * 为{@link Exp}类提供静态工具方法
 */
public final class Exps {
    private Exps() {
    }

    public static boolean holdsInt(Exp exp) {
        return exp.getType() instanceof PrimitiveType t && t.asInt();
    }

    public static boolean holdsInteger(Exp exp) {
        return holdsInt(exp) || holdsLong(exp);
    }

    public static boolean holdsLong(Exp exp) {
        return exp.getType().equals(PrimitiveType.LONG);
    }

    public static boolean holdsReference(Exp exp) {
        return exp.getType() instanceof ReferenceType;
    }
}
