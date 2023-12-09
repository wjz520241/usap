

package keeno.usap.analysis.dataflow.fact;

import keeno.usap.util.collection.Maps;

import java.util.Map;

/**
 * An object which manages the data-flow facts associated with nodes.
 *
 * @param <Node> type of nodes
 * @param <Fact> type of data-flow facts
 */
public class DataflowResult<Node, Fact> implements NodeResult<Node, Fact> {

    private final Map<Node, Fact> inFacts;

    private final Map<Node, Fact> outFacts;

    public DataflowResult(Map<Node, Fact> inFacts, Map<Node, Fact> outFacts) {
        this.inFacts = inFacts;
        this.outFacts = outFacts;
    }

    public DataflowResult() {
        this(Maps.newLinkedHashMap(), Maps.newLinkedHashMap());
    }

    /**
     * @return the flowing-in fact of given node.
     */
    @Override
    public Fact getInFact(Node node) {
        return inFacts.get(node);
    }

    /**
     * Associates a data-flow fact with a node as its flowing-in fact.
     */
    public void setInFact(Node node, Fact fact) {
        inFacts.put(node, fact);
    }

    /**
     * @return the flowing-out fact of given node.
     */
    @Override
    public Fact getOutFact(Node node) {
        return outFacts.get(node);
    }

    /**
     * Associates a data-flow fact with a node as its flowing-out fact.
     */
    public void setOutFact(Node node, Fact fact) {
        outFacts.put(node, fact);
    }
}
