

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.language.type.ArrayType;
import keeno.usap.language.type.Type;

/**
 * 表示数组索引指针.
 * 由于模拟数组的索引模型是一件十分复杂且收益很低的事情，所以无论我们往数组的那个索引下赋值，都认为该数组已经被污染
 */
public class ArrayIndex extends AbstractPointer {

    private final CSObj array;

    ArrayIndex(CSObj array, int index) {
        super(index);
        this.array = array;
    }

    /**
     * @return the array object.
     */
    public CSObj getArray() {
        return array;
    }

    @Override
    public Type getType() {
        return ((ArrayType) array.getObject().getType())
                .elementType();
    }

    @Override
    public String toString() {
        return array + "[*]";
    }
}
