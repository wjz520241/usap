

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.language.classes.JField;
import keeno.usap.language.type.Type;

/**
 * Represents instance field pointers.
 */
public class InstanceField extends AbstractPointer {

    private final CSObj base;

    private final JField field;

    InstanceField(CSObj base, JField field, int index) {
        super(index);
        this.base = base;
        this.field = field;
    }

    /**
     * @return the base object.
     */
    public CSObj getBase() {
        return base;
    }

    /**
     * @return the corresponding instance field of the InstanceField pointer.
     */
    public JField getField() {
        return field;
    }

    @Override
    public Type getType() {
        return field.getType();
    }

    @Override
    public String toString() {
        return base + "." + field.getName();
    }
}
