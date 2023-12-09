

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;
import keeno.usap.language.type.Type;

/**
 * Representation of comparison expression, e.g., cmp.
 */
public class ComparisonExp extends AbstractBinaryExp {

    public enum Op implements BinaryExp.Op {

        /**
         * 该指令用于比较两个整数的大小，并将结果保存在操作数栈中。具体而言，它会从操作数栈中弹出两个整数值，然后将比较结果（等于、大于或小于）放入操作数栈中。
         */
        CMP("cmp"),
        /**
         * 该指令用于比较两个浮点数的大小，并将结果保存在操作数栈中。类似于CMP指令，它从操作数栈中弹出两个浮点数值，并将比较结果放入操作数栈中。
         */
        CMPL("cmpl"),
        /**
         * 与CMPL指令类似，CMPG也用于比较两个浮点数的大小，并将结果保存在操作数栈中。不同之处在于，CMPG指令对于NaN（Not-a-Number）值的处理略有不同。
         */
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
