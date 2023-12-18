

package keeno.usap.analysis.pta.core.heap;

import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.util.Hashes;

import java.util.Optional;

/**
 * Represents the objects whose allocation sites are not explicitly written in the program.
 *
 * 表示分配点未显式写入程序的对象。
 * "allocation sites" 通常指的是程序中动态内存分配的位置。在程序执行过程中，当动态内存被分配给对象或数据结构时，该内存分配的位置就是一个 "allocation site"。
 * 例如java中的new表达式
 */
public class MockObj extends Obj {

    private final Descriptor desc;

    private final Object alloc;

    private final Type type;

    private final JMethod container;

    private final boolean isFunctional;

    public MockObj(Descriptor desc, Object alloc, Type type,
                   JMethod container, boolean isFunctional) {
        this.desc = desc;
        this.alloc = alloc;
        this.type = type;
        this.container = container;
        this.isFunctional = isFunctional;
    }

    public Descriptor getDescriptor() {
        return desc;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Object getAllocation() {
        return alloc;
    }

    @Override
    public Optional<JMethod> getContainerMethod() {
        return Optional.ofNullable(container);
    }

    @Override
    public Type getContainerType() {
        return container != null ?
                container.getDeclaringClass().getType() : type;
    }

    @Override
    public boolean isFunctional() {
        return isFunctional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MockObj that = (MockObj) o;
        return desc.equals(that.desc) &&
                alloc.equals(that.alloc) &&
                type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Hashes.safeHash(desc, alloc, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(desc.string()).append('{');
        sb.append("alloc=").append(alloc).append(",");
        sb.append("type=").append(type);
        if (container != null) {
            sb.append(" in ").append(container);
        }
        sb.append("}");
        return sb.toString();
    }
}
