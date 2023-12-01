package keeno.usap.language;

public enum NullType implements ReferenceType{
    NULL;

    @Override
    public String getName() {
        return "null-type";
    }

    @Override
    public String toString() {
        return getName();
    }
}
