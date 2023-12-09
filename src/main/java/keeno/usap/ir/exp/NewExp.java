

package keeno.usap.ir.exp;

import keeno.usap.language.type.ReferenceType;

/**
 * Representation of new expressions.
 */
public interface NewExp extends RValue {

    @Override
    ReferenceType getType();
}
