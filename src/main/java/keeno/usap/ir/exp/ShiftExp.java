

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;

/**
 * Representation of shift expression, e.g., a >> b.
 */
public class ShiftExp extends AbstractBinaryExp {

    public enum Op implements BinaryExp.Op {

        SHL("<<"),
        SHR(">>"),
        USHR(">>>"),
        ;

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

    public ShiftExp(Op op, Var value1, Var value2) {
        super(value1, value2);
        this.op = op;
    }

    @Override
    protected void validate() {
        assert Exps.holdsInteger(operand1) && Exps.holdsInt(operand2);
    }

    @Override
    public Op getOperator() {
        return op;
    }

    @Override
    public PrimitiveType getType() {
        return (PrimitiveType) operand1.getType();
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
