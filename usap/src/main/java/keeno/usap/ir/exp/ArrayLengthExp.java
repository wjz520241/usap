package keeno.usap.ir.exp;

import keeno.usap.language.ArrayType;
import keeno.usap.language.PrimitiveType;

public class ArrayLengthExp implements UnaryExp {

    private final Var base;

    public ArrayLengthExp(Var base) {
        this.base = base;
        assert base.getType() instanceof ArrayType;
    }

    @Override
    public Var getOperand() {
        return base;
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.INT;
    }

    @Override
    public String toString() {
        return base + ".length";
    }
}
