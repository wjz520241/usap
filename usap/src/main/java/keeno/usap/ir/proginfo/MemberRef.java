package keeno.usap.ir.proginfo;

import java.io.Serializable;

public abstract class MemberRef implements Serializable {

    private final String name;

    private final boolean isStatic;

    public MemberRef(String name, boolean isStatic) {
        this.name = name;
        this.isStatic = isStatic;
    }

    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return isStatic;
    }

}
