package keeno.usap.ir.exp;

import keeno.usap.language.ClassType;
import keeno.usap.language.Type;

public class ClassLiteral implements ReferenceLiteral{
    private final Type value;

    private ClassLiteral(Type value) {
        this.value = value;
    }

    public static ClassLiteral get(Type value) {
        return new ClassLiteral(value);
    }

    @Override
    public ClassType getType() {
        return null;
    }

    public Type getTypeValue() {
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
        ClassLiteral that = (ClassLiteral) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.getName() + ".class";
    }
}
