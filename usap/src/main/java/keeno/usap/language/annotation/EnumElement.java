

package keeno.usap.language.annotation;

public record EnumElement(
        // string representation of the enum type
        String type,
        // name of the enum constant
        String name)
        implements Element {

    @Override
    public String toString() {
        return type + "." + name;
    }
}
