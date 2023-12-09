

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;
import keeno.usap.language.type.ReferenceType;

import java.util.Set;

/**
 * Representation of instanceof expression, e.g., o instanceof T.
 */
public class InstanceOfExp implements RValue {

    /**
     * The value to be checked.
     */
    private final Var value;

    private final ReferenceType checkedType;

    public InstanceOfExp(Var value, ReferenceType checkedType) {
        this.value = value;
        this.checkedType = checkedType;
    }

    public Var getValue() {
        return value;
    }

    public ReferenceType getCheckedType() {
        return checkedType;
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.BOOLEAN;
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
        return value + " instanceof " + checkedType;
    }
}
