package keeno.usap.language.annotation;

public record StringElement(String value) implements Element {

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
}
