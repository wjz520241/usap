

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;

public class DoubleLiteral implements FloatingPointLiteral {

    /**
     * 缓存常用字面量以节省空间。
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

    /**
     * @return 这种实现方式的目的是尽可能地降低哈希冲突的概率。由于double类型的值具有较大的范围和精度，
     * 直接使用该值作为哈希码可能导致较高的冲突率。通过将double类型的位表示形式转换为long类型，
     * 并使用位运算操作，可以更好地保持哈希码的均匀分布性。
     */
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
