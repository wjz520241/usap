package keeno.usap.ir.exp;

import keeno.usap.language.Type;
import keeno.usap.util.Indexable;

import java.io.Serializable;

/**
 * 方法构造函数参数、lambda参数、异常参数和局部变量的表示
 */
public class Var implements LValue, RValue, Indexable, Serializable {

    private final Type type;
    private final int index;

    /**
     *这个变量的名称
     */
    private final String name;

    public Var(Type type, int index, String name) {
        this.type = type;
        this.index = index;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return name;
    }

}
