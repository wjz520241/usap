package keeno.usap.language.annotation;

public record EnumElement(
        // 枚举类型的字符串表示
        String type,
        // 枚举常量的名称
        String name)
        implements Element {

    @Override
    public String toString() {
        return type + "." + name;
    }
}
