

package keeno.usap.analysis.graph.cfg;

import keeno.usap.language.type.ClassType;
import keeno.usap.util.AnalysisException;
import keeno.usap.util.Hashes;
import keeno.usap.util.graph.AbstractEdge;

import java.util.Set;

/**
 * Represents CFG edges.
 *
 * @param <N> type of CFG nodes.
 */
public class CFGEdge<N> extends AbstractEdge<N> {

    public enum Kind {

        /**
         * Edge from entry node to real start node.
         */
        ENTRY,

        /**
         * Edge kind for fall-through to next statement.
         */
        FALL_THROUGH,

        /**
         * Edge kind for goto statements.
         */
        GOTO,

        /**
         * Edge kind for if statements when condition is true.
         */
        IF_TRUE,

        /**
         * Edge kind for if statements when condition is false.
         */
        IF_FALSE,

        /**
         * Edge kind for switch statements (explicit case).
         */
        SWITCH_CASE,

        /**
         * Edge kind for switch statements (default case).
         */
        SWITCH_DEFAULT,

        /**
         * Edge representing exceptional control flow from an
         * exception-raising node to an explicit handler for the exception.
         */
        CAUGHT_EXCEPTION,

        /**
         * Edge representing the possibility that a node raise an exception
         * that cannot be caught by the current method.
         * These edges always go to the exit node of the CFG.
         */
        UNCAUGHT_EXCEPTION,

        /**
         * Edge kind for return statements.
         * These edges always go to the exit node of the CFG.
         */
        RETURN,
    }

    private final Kind kind;

    CFGEdge(Kind kind, N source, N target) {
        super(source, target);
        this.kind = kind;
    }

    /**
     * @return the kind of the edge.
     * @see CFGEdge.Kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * @return true if this edge is a switch-case edge, otherwise false.
     */
    public boolean isSwitchCase() {
        return kind == Kind.SWITCH_CASE;
    }

    /**
     * If this edge is a switch-case edge, then returns the case value.
     * The client code should call {@link #isSwitchCase()} to check if
     * this edge is switch-case edge before calling this method.
     *
     * @throws AnalysisException if this edge is not a switch-case edge.
     */
    public int getCaseValue() {
        // SwitchCaseEdge overrides this method, thus this method
        // should NOT be reachable
        throw new AnalysisException(this + " is not a switch-case edge," +
                " please call isSwitchCase() before calling this method");
    }

    /**
     * @return true if this edge is an exceptional edge, otherwise false.
     */
    public boolean isExceptional() {
        return kind == Kind.CAUGHT_EXCEPTION ||
                kind == Kind.UNCAUGHT_EXCEPTION;
    }

    /**
     * If this edge is an exceptional edge, return the exception types along
     * with this edge, otherwise return an empty set.
     */
    public Set<ClassType> getExceptions() {
        assert isExceptional() : this + " is not an exceptional edge";
        return Set.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CFGEdge<?> edge = (CFGEdge<?>) o;
        return kind == edge.kind &&
                source.equals(edge.source) &&
                target.equals(edge.target);
    }

    @Override
    public int hashCode() {
        return Hashes.hash(kind, source, target);
    }

    @Override
    public String toString() {
        return "[" + kind + "]: " + source + " -> " + target;
    }
}
