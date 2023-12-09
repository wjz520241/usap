

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;

/**
 * 注释同{@link IntLiteral}
 */
public class LongLiteral implements IntegerLiteral {

    /**
     * Cache frequently used literals for saving space.
     */
    private static final LongLiteral[] cache = new LongLiteral[-(-128) + 127 + 1];

    static {
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new LongLiteral(i - 128);
        }
    }

    /**
     * The value of the literal.
     */
    private final long value;

    private LongLiteral(long value) {
        this.value = value;
    }

    public static LongLiteral get(long value) {
        final int offset = 128;
        if (value >= -128 && value <= 127) { // will cache
            return cache[(int) value + offset];
        }
        return new LongLiteral(value);
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.LONG;
    }

    /**
     * @return the value of the literal as a long.
     */
    public long getValue() {
        return value;
    }

    @Override
    public Long getNumber() {
        return value;
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LongLiteral) {
            return value == ((LongLiteral) o).getValue();
        }
        return false;
    }

    /**
     * @see DoubleLiteral#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    @Override
    public String toString() {
        return value + "L";
    }
}
