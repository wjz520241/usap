package keeno.usap.ir.exp;

import keeno.usap.language.ArrayType;

import java.util.Set;

/**
 * new 数组表达式  new T[...]
 */
public class NewArray implements NewExp {
    private final ArrayType type;

    private final Var length;

    public NewArray(ArrayType type, Var length) {
        this.type = type;
        this.length = length;
    }

    @Override
    public ArrayType getType() {
        return type;
    }

    public Var getLength() {
        return length;
    }

    @Override
    public Set<RValue> getUses() {
        return Set.of(length);
    }

    @Override
    public String toString() {
        return String.format("newarray %s[%s]", type.elementType(), length);
    }
}
