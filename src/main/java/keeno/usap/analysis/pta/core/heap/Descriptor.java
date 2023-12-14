

package keeno.usap.analysis.pta.core.heap;

/**
 * Descriptor for {@link MockObj}.
 * It also acts as a distinguishable part of different kinds of {@link MockObj}.
 */
@FunctionalInterface
public interface Descriptor {

    /**
     * Descriptor for entry objects.
     * 该实例等价于下列形式
     * Descriptor ENTRY_DESC = new Descriptor() {
     *     @Override
     *     public String string() {
     *         return "EntryPointObj";
     *     }
     * };
     */
    Descriptor ENTRY_DESC = () -> "EntryPointObj";

    /**
     * @return string content of this descriptor.
     */
    String string();
}
