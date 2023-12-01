package keeno.usap.ir.proginfo;


import keeno.usap.language.Type;


public class FieldRef extends MemberRef {

    private final Type type;

    private FieldRef(Type type, String name, boolean isStatic) {
        super(name, isStatic);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

}
