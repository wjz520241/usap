package keeno.usap.ir.exp;

import keeno.usap.language.PrimitiveType;

public class FloatLiteral implements FloatingPointLiteral{

    //缓存常用文字以节省空间
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
