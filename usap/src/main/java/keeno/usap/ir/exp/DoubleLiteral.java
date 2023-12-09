

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;

public class DoubleLiteral implements FloatingPointLiteral {

    /**
     * Cache frequently used literals for saving space.
     */
    private static final DoubleLiteral ZERO = new DoubleLiteral(0);

    private final double value;

    private DoubleLiteral(double value) {
        this.value = value;
    }

    public static DoubleLiteral get(double value) {
        return value == 0 ? ZERO : new DoubleLiteral(value);
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.DOUBLE;
    }

    public double getValue() {
        return value;
    }

    @Override
    public Double getNumber() {
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
        DoubleLiteral that = (DoubleLiteral) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}
