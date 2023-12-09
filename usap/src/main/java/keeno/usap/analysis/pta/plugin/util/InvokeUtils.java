

package keeno.usap.analysis.pta.plugin.util;

import keeno.usap.ir.exp.InvokeExp;
import keeno.usap.ir.exp.InvokeInstanceExp;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;

/**
 * Provides utility methods to conveniently handle {@link Invoke}.
 */
public final class InvokeUtils {

    /**
     * Special number representing the base variable of an invocation.
     */
    public static final int BASE = -1;

    /**
     * String representation of base variable.
     */
    private static final String BASE_STR = "base";

    /**
     * Special number representing the variable that receivers
     * the result of the invocation.
     */
    public static final int RESULT = -2;

    /**
     * String representation of result variable.
     */
    private static final String RESULT_STR = "result";

    private InvokeUtils() {
    }

    /**
     * Coverts string to index.
     */
    public static int toInt(String s) {
        return switch (s.toLowerCase()) {
            case BASE_STR -> BASE;
            case RESULT_STR -> RESULT;
            default -> Integer.parseInt(s);
        };
    }

    /**
     * Converts index to string.
     */
    public static String toString(int index) {
        return switch (index) {
            case BASE -> BASE_STR;
            case RESULT -> RESULT_STR;
            default -> Integer.toString(index);
        };
    }

    /**
     * Retrieves variable from a call site and index.
     */
    public static Var getVar(Invoke callSite, int index) {
        InvokeExp invokeExp = callSite.getInvokeExp();
        return switch (index) {
            case BASE -> ((InvokeInstanceExp) invokeExp).getBase();
            case RESULT -> callSite.getResult();
            default -> invokeExp.getArg(index);
        };
    }
}
