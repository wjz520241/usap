

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;

/**
 * Representation of condition expression, e.g., a == b.
 */
public class ConditionExp extends AbstractBinaryExp {

    public enum Op implements BinaryExp.Op {

        EQ("=="),
        NE("!="),
        LT("<"),
        GT(">"),
        LE("<="),
        GE(">="),
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

    public ConditionExp(Op op, Var value1, Var value2) {
        super(value1, value2);
        this.op = op;
    }

    @Override
    protected void validate() {
        assert (Exps.holdsInt(operand1) && Exps.holdsInt(operand2)) ||
                (Exps.holdsReference(operand1) && Exps.holdsReference(operand2));
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
