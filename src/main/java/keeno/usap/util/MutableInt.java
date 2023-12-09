

package keeno.usap.util;

/**
 * A mutable int wrapper.
 */
public class MutableInt extends Number implements Comparable<Number> {

    private int value;

    /**
     * Creates a new MutableInt with the given initial value.
     */
    public MutableInt(int initialValue) {
        this.value = initialValue;
    }

    /**
     * Sets the value to {@code newValue}.
     */
    public void set(int newValue) {
        this.value = newValue;
    }

    /**
     * Adds the given value to the current value.
     */
    public void add(int delta) {
        value += delta;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public int compareTo(Number number) {
        return value - number.intValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Number other)) {
            return false;
        }
        return value == other.intValue();
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
