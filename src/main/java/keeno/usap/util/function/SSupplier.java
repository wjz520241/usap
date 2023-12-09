

package keeno.usap.util.function;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * The {@link Serializable} version of {@link Supplier}.
 * <br>
 * <strong>It is suggested
 * that the implementation of this interface should not have state</strong>,
 * as this may cause serialization failures.
 *
 * @see Supplier
 */
@FunctionalInterface
public interface SSupplier<T> extends Supplier<T>, Serializable {
}
