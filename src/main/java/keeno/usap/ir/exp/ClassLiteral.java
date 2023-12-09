

package keeno.usap.ir.exp;

import keeno.usap.World;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.Type;

import static keeno.usap.language.classes.ClassNames.CLASS;

public class ClassLiteral implements ReferenceLiteral {

    /**
     * The type represented by this class object.
     */
    private final Type value;

    private ClassLiteral(Type value) {
        this.value = value;
    }

    public static ClassLiteral get(Type value) {
        return new ClassLiteral(value);
    }

    @Override
    public ClassType getType() {
        // TODO: cache Class type in a static field? Doing so
        //  requires to reset the field when resetting World.
        return World.get().getTypeSystem().getClassType(CLASS);
    }

    public Type getTypeValue() {
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
