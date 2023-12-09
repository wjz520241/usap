

package keeno.usap.ir.exp;

import keeno.usap.language.type.ReferenceType;

/**
 * Literal of reference type.
 */
public interface ReferenceLiteral extends Literal {

    @Override
    ReferenceType getType();
}
