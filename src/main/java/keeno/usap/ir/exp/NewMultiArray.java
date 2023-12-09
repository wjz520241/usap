

package keeno.usap.ir.exp;

import keeno.usap.language.type.ArrayType;
import keeno.usap.util.collection.ArraySet;

import java.util.List;
import java.util.Set;

/**
 * Representation of new multi-array expression, e.g., new T[..][..][..].
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
        return new ArraySet<>(lengths);
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
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
