

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;

/**
 * Representation of int literals.
 */
public class IntLiteral implements IntegerLiteral {

    /**
     * Cache frequently used literals for saving space.
     */
    private static final IntLiteral[] cache = new IntLiteral[-(-128) + 127 + 1];

    static {
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new IntLiteral(i - 128);
        }
    }

    /**
     * The value of the literal.
     */
    private final int value;

    private IntLiteral(int value) {
        this.value = value;
    }

    public static IntLiteral get(int value) {
        final int offset = 128;
        if (value >= -128 && value <= 127) { // will cache
            return cache[value + offset];
        }
        return new IntLiteral(value);
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.INT;
    }

    /**
     * @return the value of the literal as an int.
     */
    public int getValue() {
        return value;
    }

    @Override
    public Integer getNumber() {
        return value;
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IntLiteral) {
            return value == ((IntLiteral) o).getValue();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
