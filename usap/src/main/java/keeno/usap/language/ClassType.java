package keeno.usap.language;

public class ClassType implements ReferenceType{

    private final String name;

    public ClassType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }



    @Override
    public String toString() {
        return name;
    }
}
