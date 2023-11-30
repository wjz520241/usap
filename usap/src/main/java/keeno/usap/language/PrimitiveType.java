package keeno.usap.language;

public enum PrimitiveType implements Type {
    INT("int"),
    CHAR("char"),
    BOOLEAN("boolean"),
    BYTE("byte"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    SHORT("short");

    private final String name;

    PrimitiveType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     *JVM规范（2.11.1）：对实际类型boolean、byte、char和short的值的大多数操作都是由对计算类型int的值进行操作的指令正确执行的
     */
    public boolean asInt() {
        return switch (this) {
            case INT, CHAR, BOOLEAN, BYTE, SHORT -> true;
            default -> false;
        };
    }


}
