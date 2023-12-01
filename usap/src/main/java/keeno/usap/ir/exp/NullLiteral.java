package keeno.usap.ir.exp;

import keeno.usap.language.NullType;

public enum NullLiteral implements ReferenceLiteral{
    INSTANCE;

    public static NullLiteral get() {
        return INSTANCE;
    }

    @Override
    public NullType getType() {
        return NullType.NULL;
    }

    @Override
    public String toString() {
        return "null";
    }
}
