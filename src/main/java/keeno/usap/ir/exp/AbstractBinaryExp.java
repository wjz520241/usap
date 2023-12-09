

package keeno.usap.ir.exp;

import keeno.usap.util.collection.ArraySet;

import java.util.Collections;
import java.util.Set;

/**
 * Provides common functionalities for {@link BinaryExp} implementations.
 */
abstract class AbstractBinaryExp implements BinaryExp {

    protected final Var operand1;

    protected final Var operand2;

    protected AbstractBinaryExp(Var operand1, Var operand2) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        validate();
    }

    /**
     * Validates type correctness of the two values of this expression.
     */
    protected void validate() {
    }

    @Override
    public Var getOperand1() {
        return operand1;
    }

    @Override
    public Var getOperand2() {
        return operand2;
    }

    @Override
    public Set<RValue> getUses() {
        Set<RValue> uses = new ArraySet<>(2);
        Collections.addAll(uses, operand1, operand2);
        return uses;
    }

    @Override
    public String toString() {
        return operand1 + " " + getOperator() + " " + operand2;
    }
}
