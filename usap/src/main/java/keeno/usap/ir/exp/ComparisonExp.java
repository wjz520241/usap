

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;
import keeno.usap.language.type.Type;

/**
 * Representation of comparison expression, e.g., cmp.
 */
public class ComparisonExp extends AbstractBinaryExp {

    public enum Op implements BinaryExp.Op {

        CMP("cmp"),
        CMPL("cmpl"),
        CMPG("cmpg"),
        ;

        private final String instruction;

        Op(String instruction) {
            this.instruction = instruction;
        }

        @Override
        public String toString() {
            return instruction;
        }
    }

    private final Op op;

    public ComparisonExp(Op op, Var value1, Var value2) {
        super(value1, value2);
        this.op = op;
    }

    @Override
    protected void validate() {
        Type v1type = operand1.getType();
        assert v1type.equals(operand2.getType());
        assert v1type.equals(PrimitiveType.LONG) ||
                v1type.equals(PrimitiveType.FLOAT) ||
                v1type.equals(PrimitiveType.DOUBLE);
    }

    @Override
    public Op getOperator() {
        return op;
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.INT;
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
