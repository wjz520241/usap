

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.language.classes.JField;
import keeno.usap.language.type.Type;

/**
 * Represents static field pointers.
 */
public class StaticField extends AbstractPointer {

    private final JField field;

    StaticField(JField field, int index) {
        super(index);
        this.field = field;
    }

    /**
     * @return the corresponding static field of the StaticField pointer.
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
        return field.toString();
    }
}
