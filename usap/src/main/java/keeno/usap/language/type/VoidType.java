

package keeno.usap.language.type;

public enum VoidType implements Type {

    VOID;

    @Override
    public String getName() {
        return "void";
    }

    @Override
    public String toString() {
        return getName();
    }
}
