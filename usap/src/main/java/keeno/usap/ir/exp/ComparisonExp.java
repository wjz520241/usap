package keeno.usap.ir.exp;

import keeno.usap.language.PrimitiveType;
import keeno.usap.language.Type;

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

    /**
     * @return 这里更多的是代表数字的意思，而非具体的类型
     */
    @Override
    public PrimitiveType getType() {
        return PrimitiveType.INT;
    }
}
