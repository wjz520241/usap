

package keeno.usap.analysis.dataflow.analysis;

import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.analysis.graph.cfg.CFGEdge;

public abstract class AbstractDataflowAnalysis<Node, Fact>
        implements DataflowAnalysis<Node, Fact> {

    protected final CFG<Node> cfg;

    protected AbstractDataflowAnalysis(CFG<Node> cfg) {
        this.cfg = cfg;
    }

    /**
     * By default, a data-flow analysis does not have edge transfer, i.e.,
     * does not need to perform transfer for any edges.
     */
    @Override
    public boolean needTransferEdge(CFGEdge<Node> edge) {
        return false;
    }

    @Override
    public Fact transferEdge(CFGEdge<Node> edge, Fact nodeFact) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CFG<Node> getCFG() {
        return cfg;
    }
}
