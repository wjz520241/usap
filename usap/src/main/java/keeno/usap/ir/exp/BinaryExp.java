

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;

/**
 * Representation of binary expression.
 */
public interface BinaryExp extends RValue {

    /**
     * Representation of binary operators.
     */
    interface Op {
    }

    /**
     * @return the operator.
     */
    Op getOperator();

    /**
     * @return the first operand.
     */
    Var getOperand1();

    /**
     * @return the second operand.
     */
    Var getOperand2();

    @Override
    PrimitiveType getType();
}
