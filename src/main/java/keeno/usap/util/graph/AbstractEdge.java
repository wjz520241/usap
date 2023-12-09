

package keeno.usap.util.graph;

import keeno.usap.util.Hashes;

public abstract class AbstractEdge<N> implements Edge<N> {

    /**
     * The source node of the edge.
     */
    protected final N source;

    /**
     * The target node of the edge.
     */
    protected final N target;

    protected AbstractEdge(N source, N target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public N source() {
        return source;
    }

    @Override
    public N target() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractEdge<?> edge = (AbstractEdge<?>) o;
        return source.equals(edge.source) && target.equals(edge.target);
    }

    @Override
    public int hashCode() {
        return Hashes.hash(source, target);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{" + source + " -> " + target + '}';
    }
}
