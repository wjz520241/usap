

package keeno.usap.analysis.pta.core.heap;

/**
 * Descriptor for {@link MockObj}.
 * It also acts as a distinguishable part of different kinds of {@link MockObj}.
 */
@FunctionalInterface
public interface Descriptor {

    /**
     * Descriptor for entry objects.
     */
    Descriptor ENTRY_DESC = () -> "EntryPointObj";

    /**
     * @return string content of this descriptor.
     */
    String string();
}
