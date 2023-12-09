

package keeno.usap.ir.exp;

import keeno.usap.language.type.ClassType;

/**
 * Representation of new instance expression, e.g., new T.
 */
public class NewInstance implements NewExp {

    private final ClassType type;

    public NewInstance(ClassType type) {
        this.type = type;
    }

    @Override
    public ClassType getType() {
        return type;
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "new " + type;
    }
}
