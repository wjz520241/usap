package keeno.usap.ir.exp;

import keeno.usap.language.ReferenceType;

public interface NewExp extends Exp {
    @Override
    ReferenceType getType();
}
