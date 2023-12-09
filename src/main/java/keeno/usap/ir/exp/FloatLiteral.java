

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;

public class FloatLiteral implements FloatingPointLiteral {

    /**
     * Cache frequently used literals for saving space.
     */
    private static final FloatLiteral ZERO = new FloatLiteral(0);

    private final float value;

    private FloatLiteral(float value) {
        this.value = value;
    }

    public static FloatLiteral get(float value) {
        return value == 0 ? ZERO : new FloatLiteral(value);
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.FLOAT;
    }

    public float getValue() {
        return value;
    }

    @Override
    public Float getNumber() {
        return value;
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FloatLiteral that = (FloatLiteral) o;
        return Float.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return (value != 0.0f ? Float.floatToIntBits(value) : 0);
    }

    @Override
    public String toString() {
        return value + "F";
    }
}
