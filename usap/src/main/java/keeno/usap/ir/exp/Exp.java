

package keeno.usap.ir.exp;

import keeno.usap.language.type.Type;

import java.io.Serializable;
import java.util.Set;

/**
 * Representation of expressions in Tai-e IR.
 */
public interface Exp extends Serializable {

    /**
     * @return type of this expression.
     */
    Type getType();

    /**
     * @return a list of expressions which are used by (contained in) this Exp.
     */
    default Set<RValue> getUses() {
        return Set.of();
    }

    <T> T accept(ExpVisitor<T> visitor);
}
