

package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.FieldRef;
import keeno.usap.language.type.Type;

/**
 * Representation of field access expressions.
 */
public abstract class FieldAccess implements LValue, RValue {

    protected final FieldRef fieldRef;

    protected FieldAccess(FieldRef fieldRef) {
        this.fieldRef = fieldRef;
    }

    public FieldRef getFieldRef() {
        return fieldRef;
    }

    @Override
    public Type getType() {
        return fieldRef.getType();
    }
}
