

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;

import java.util.Set;

/**
 * Representation of unary expression.
 */
public interface UnaryExp extends RValue {

    Var getOperand();

    @Override
    default Set<RValue> getUses() {
        return Set.of(getOperand());
    }

    @Override
    PrimitiveType getType();
}
