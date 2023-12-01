package keeno.usap.ir.exp;

import keeno.usap.language.PrimitiveType;

/**
 * 算数表达式 a + b
 */
public class ArithmeticExp extends AbstractBinaryExp {

    public enum Op implements BinaryExp.Op {
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        REM("%");

        private final String symbol;

        Op(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    private final Op op;

    protected ArithmeticExp(Op op, Var operand1, Var operand2) {
        super(operand1, operand2);
        this.op = op;
    }

    @Override
    public Op getOperator() {
        return op;
    }

    /**
     * @return 计算机中同类型的才能相加，不是同类型也会转换成同类型再相加
     */
    @Override
    public PrimitiveType getType() {
        return (PrimitiveType) operand1.getType();
    }
}
