

package keeno.usap.analysis.graph.cfg;

import keeno.usap.ir.IR;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.graph.Graph;

import java.util.Set;

/**
 * Representation of a control-flow graph of a method.
 *
 * @param <N> type of CFG nodes.
 */
public interface CFG<N> extends Graph<N> {

    /**
     * @return the IR of the method this CFG represents.
     */
    IR getIR();

    /**
     * @return the method this CFG represents.
     */
    JMethod getMethod();

    /**
     * @return the entry node of this CFG.
     */
    N getEntry();

    /**
     * @return the exit node of this CFG.
     */
    N getExit();

    /**
     * @return true if the given node is the entry of this CFG, otherwise false.
     */
    boolean isEntry(N node);

    /**
     * @return true if the given node is the exit of this CFG, otherwise false.
     */
    boolean isExit(N node);

    /**
     * @return a unique index for given node in this CFG.
     */
    int getIndex(N node);

    /**
     * @return the corresponding node specified by the given index.
     */
    N getNode(int index);

    /**
     * @return incoming edges of the given node.
     */
    @Override
    Set<CFGEdge<N>> getInEdgesOf(N node);

    /**
     * @return outgoing edges of the given node.
     */
    @Override
    Set<CFGEdge<N>> getOutEdgesOf(N node);
}
