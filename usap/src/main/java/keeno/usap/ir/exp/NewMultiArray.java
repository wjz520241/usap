package keeno.usap.ir.exp;

import keeno.usap.language.ArrayType;
import soot.util.ArraySet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 多维数组 new T[..][..][..]..
 */
public class NewMultiArray implements NewExp {

    private final ArrayType type;

    private final List<Var> lengths;

    public NewMultiArray(ArrayType type, List<Var> lengths) {
        this.type = type;
        this.lengths = List.copyOf(lengths);
    }

    @Override
    public ArrayType getType() {
        return type;
    }

    public int getLengthCount() {
        return lengths.size();
    }

    public Var getLength(int i) {
        return lengths.get(i);
    }

    public List<Var> getLengths() {
        return lengths;
    }

    @Override
    public Set<RValue> getUses() {
        Set<RValue> arraySet = new ArraySet<>(lengths.size());
        arraySet.addAll(lengths);
        return arraySet;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("newmultiarray ");
        sb.append(type.baseType());
        lengths.forEach(length ->
                sb.append('[').append(length).append(']'));
        sb.append("[]".repeat(
                Math.max(0, type.dimensions() - lengths.size())));
        return sb.toString();
    }
}
