package keeno.usap.ir.exp;

import keeno.usap.language.PrimitiveType;

/**
 * 负数表达式  -x
 */
public class NegExp implements UnaryExp {

    private final Var value;

    public NegExp(Var value) {
        this.value = value;
        assert value.getType() instanceof PrimitiveType;
    }

    @Override
    public Var getOperand() {
        return value;
    }

    @Override
    public PrimitiveType getType() {
        PrimitiveType type = (PrimitiveType) value.getType();
        return type.asInt() ? PrimitiveType.INT : type;
    }

    @Override
    public String toString() {
        return "-" + value;
    }
}
