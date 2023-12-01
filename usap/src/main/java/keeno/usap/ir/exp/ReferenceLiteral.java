package keeno.usap.ir.exp;

import keeno.usap.language.ReferenceType;

public interface ReferenceLiteral extends Literal{
    @Override
    ReferenceType getType();
}
