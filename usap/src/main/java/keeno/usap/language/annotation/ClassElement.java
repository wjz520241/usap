package keeno.usap.language.annotation;

/**
 * 表示类常量元素。我们使用{@code String}而不是{@code Type}来表示类元素的类型信息，原因与{@link Annotation}相同。
 */
public record ClassElement(String classDescriptor) implements Element {
    @Override
    public String toString() {
        return classDescriptor + ".class";
    }
}
