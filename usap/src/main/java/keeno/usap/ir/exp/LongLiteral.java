package keeno.usap.ir.exp;

import keeno.usap.language.PrimitiveType;

/**
 * 注释同{@link IntLiteral}
 */
public class LongLiteral implements IntegerLiteral{
    private static final LongLiteral[] cache = new LongLiteral[-(-128) + 127 + 1];

    static {
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new LongLiteral(i - 128);
        }
    }

    private final long value;

    private LongLiteral(long value) {
        this.value = value;
    }

    public static LongLiteral get(long value) {
        final int offset = 128;
        if (value >= -128 && value <= 127) {
            return cache[(int) value + offset];
        }
        return new LongLiteral(value);
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.LONG;
    }

    public long getValue() {
        return value;
    }

    @Override
    public Long getNumber() {
        return value;
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
