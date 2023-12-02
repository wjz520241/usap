package keeno.usap.language.annotation;

public record IntElement(int value) implements Element {

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
