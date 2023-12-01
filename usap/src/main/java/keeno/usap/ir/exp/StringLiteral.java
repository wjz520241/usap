package keeno.usap.ir.exp;

import keeno.usap.language.ClassType;

public class StringLiteral implements ReferenceLiteral {
    private final String value;

    private StringLiteral(String value) {
        this.value = value;
    }

    public static StringLiteral get(String value) {

        return new StringLiteral(value);
    }

    @Override
    public ClassType getType() {
        return null;
    }

    public String getString() {
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
        StringLiteral that = (StringLiteral) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

}
