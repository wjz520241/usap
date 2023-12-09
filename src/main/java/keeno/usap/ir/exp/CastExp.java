

package keeno.usap.ir.exp;

import keeno.usap.language.type.Type;

import java.util.Set;

/**
 * Representation of cast expression, e.g., (T) o.
 */
public class CastExp implements RValue {

    /**
     * The value to be casted.
     */
    private final Var value;

    private final Type castType;

    public CastExp(Var value, Type castType) {
        this.value = value;
        this.castType = castType;
    }

    public Var getValue() {
        return value;
    }

    public Type getCastType() {
        return castType;
    }

    @Override
    public Type getType() {
        return castType;
    }

    @Override
    public Set<RValue> getUses() {
        return Set.of(value);
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("(%s) %s", castType, value);
    }
}
