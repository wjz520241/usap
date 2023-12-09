

package keeno.usap.language.classes;

/**
 * Provides names of special methods.
 */
@StringProvider
public final class MethodNames {

    public static final String INIT = "<init>";

    public static final String CLINIT = "<clinit>";

    // Suppresses default constructor, ensuring non-instantiability.
    private MethodNames() {
    }
}
