

package keeno.usap.ir.exp;

import keeno.usap.language.type.ArrayType;
import keeno.usap.language.type.Type;

import java.util.Set;

/**
 * Representation of array access expression, e.g., a[i].
 */
public class ArrayAccess implements LValue, RValue {

    private final Var base;

    private final Var index;

    public ArrayAccess(Var base, Var index) {
        this.base = base;
        this.index = index;
        assert base.getType() instanceof ArrayType;
    }

    public Var getBase() {
        return base;
    }

    public Var getIndex() {
        return index;
    }

    @Override
    public Type getType() {
        if (base.getType() instanceof ArrayType) {
            return ((ArrayType) base.getType()).elementType();
        } else {
            throw new RuntimeException("Invalid base type: " + base.getType());
        }
    }

    @Override
    public Set<RValue> getUses() {
        return Set.of(base, index);
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", base, index);
    }
}
