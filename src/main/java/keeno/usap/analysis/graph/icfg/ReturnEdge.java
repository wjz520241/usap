

package keeno.usap.analysis.graph.icfg;

import keeno.usap.ir.exp.Var;
import keeno.usap.language.type.ClassType;

import java.util.Collection;
import java.util.Collections;

/**
 * The edge connecting a method exit to return site of the call site.
 *
 * @param <Node> type of nodes
 */
public class ReturnEdge<Node> extends ICFGEdge<Node> {

    /**
     * The call site that corresponds to the return edge.
     */
    private final Node callSite;

    /**
     * The variables that hold return values.
     */
    private final Collection<Var> returnVars;

    /**
     * The exceptions that may be thrown out.
     */
    private final Collection<ClassType> exceptions;

    ReturnEdge(Node exit, Node retSite, Node callSite,
               Collection<Var> retVars, Collection<ClassType> exceptions) {
        super(exit, retSite);
        this.callSite = callSite;
        this.returnVars = Collections.unmodifiableCollection(retVars);
        this.exceptions = Collections.unmodifiableCollection(exceptions);
    }

    /**
     * @return the call site that corresponds to the return edge.
     */
    public Node getCallSite() {
        return callSite;
    }

    /**
     * Each method in ICFG has only one exit, but it may have multiple return
     * statements. This API returns all returned variables. E.g., for the
     * return edges starting from the exit of method:
     * <pre>
     * int foo(...) {
     *     if (...) {
     *         return x;
     *     } else {
     *         return y;
     *     }
     * }
     * </pre>
     * this API returns [x, y].
     *
     * @return the variables that hold the return values.
     */
    public Collection<Var> getReturnVars() {
        return returnVars;
    }

    /**
     * @return the exceptions that may be thrown out.
     */
    public Collection<ClassType> getExceptions() {
        return exceptions;
    }
}
