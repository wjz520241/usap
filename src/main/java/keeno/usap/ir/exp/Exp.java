

package keeno.usap.ir.exp;

import keeno.usap.language.type.Type;

import java.io.Serializable;
import java.util.Set;

/**
 * Representation of expressions in Tai-e IR.
 */
public interface Exp extends Serializable {

    /**
     * @return 此表达式的类型。
     */
    Type getType();

    /**
     * @return 此表达式使用（包含在）的表达式列表。
     */
    default Set<RValue> getUses() {
        return Set.of();
    }

    <T> T accept(ExpVisitor<T> visitor);
}
