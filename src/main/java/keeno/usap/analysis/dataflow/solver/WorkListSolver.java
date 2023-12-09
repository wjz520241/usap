

package keeno.usap.analysis.dataflow.solver;

import keeno.usap.analysis.dataflow.analysis.DataflowAnalysis;
import keeno.usap.analysis.dataflow.fact.DataflowResult;
import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.analysis.graph.cfg.CFGEdge;
import keeno.usap.util.collection.CollectionUtils;
import keeno.usap.util.collection.Sets;

import java.util.Comparator;
import java.util.NavigableSet;

/**
 * Work-list solver with optimization.
 */
class WorkListSolver<Node, Fact> extends AbstractSolver<Node, Fact> {

    @Override
    protected void initializeForward(DataflowAnalysis<Node, Fact> analysis,
                                     DataflowResult<Node, Fact> result) {
        CFG<Node> cfg = analysis.getCFG();
        // initialize entry
        Node entry = cfg.getEntry();
        Fact entryFact = analysis.newBoundaryFact();
        result.setInFact(entry, entryFact);
        result.setOutFact(entry, entryFact);
        cfg.forEach(node -> {
            // skip entry which has been initialized
            if (cfg.isEntry(node)) {
                return;
            }
            // initialize in fact
            if (cfg.getInDegreeOf(node) == 1) {
                cfg.getInEdgesOf(node).forEach(edge -> {
                    if (!analysis.needTransferEdge(edge)) {
                        result.setInFact(node,
                                getOrNewOutFact(result, analysis, edge.source()));
                    }
                });
            } else {
                result.setInFact(node, analysis.newInitialFact());
            }
            // initialize out fact
            getOrNewOutFact(result, analysis, node);
        });
    }

    private Fact getOrNewOutFact(DataflowResult<Node, Fact> result,
                                 DataflowAnalysis<Node, Fact> analysis,
                                 Node node) {
        Fact fact = result.getOutFact(node);
        if (fact == null) {
            fact = analysis.newInitialFact();
            result.setOutFact(node, fact);
        }
        return fact;
    }

    @Override
    protected void doSolveForward(DataflowAnalysis<Node, Fact> analysis,
                                  DataflowResult<Node, Fact> result) {
        CFG<Node> cfg = analysis.getCFG();
        NavigableSet<Node> workList = Sets.newOrderedSet(
                Comparator.comparingInt(cfg::getIndex));
        cfg.forEach(node -> {
            if (!cfg.isEntry(node)) {
                workList.add(node);
            }
        });
        while (!workList.isEmpty()) {
            Node node = workList.pollFirst();
            // meet incoming facts
            Fact in;
            int inDegree = cfg.getInDegreeOf(node);
            if (inDegree > 1) {
                in = result.getInFact(node);
                cfg.getInEdgesOf(node).forEach(inEdge -> {
                    Fact fact = result.getOutFact(inEdge.source());
                    if (analysis.needTransferEdge(inEdge)) {
                        fact = analysis.transferEdge(inEdge, fact);
                    }
                    analysis.meetInto(fact, in);
                });
            } else if (inDegree == 1) {
                CFGEdge<Node> inEdge = CollectionUtils.getOne(cfg.getInEdgesOf(node));
                if (analysis.needTransferEdge(inEdge)) {
                    in = analysis.transferEdge(inEdge,
                            result.getOutFact(inEdge.source()));
                    result.setInFact(node, in);
                } else {
                    in = result.getInFact(node);
                }
            } else {
                in = result.getInFact(node);
            }
            // apply node transfer function
            Fact out = result.getOutFact(node);
            boolean changed = analysis.transferNode(node, in, out);
            if (changed) {
                workList.addAll(cfg.getSuccsOf(node));
            }
        }
    }

    @Override
    protected void initializeBackward(DataflowAnalysis<Node, Fact> analysis,
                                      DataflowResult<Node, Fact> result) {
        CFG<Node> cfg = analysis.getCFG();
        // initialize exit
        Node exit = cfg.getExit();
        Fact exitFact = analysis.newBoundaryFact();
        result.setInFact(exit, exitFact);
        result.setOutFact(exit, exitFact);
        cfg.forEach(node -> {
            // skip exit which has been initialized
            if (cfg.isExit(node)) {
                return;
            }
            // initialize out fact
            if (cfg.getOutDegreeOf(node) == 1) {
                cfg.getOutEdgesOf(node).forEach(edge -> {
                    if (!analysis.needTransferEdge(edge)) {
                        result.setOutFact(node,
                                getOrNewInFact(result, analysis, edge.target()));
                    }
                });
            } else {
                result.setOutFact(node, analysis.newInitialFact());
            }
            // initialize in fact
            getOrNewInFact(result, analysis, node);
        });
    }

    private Fact getOrNewInFact(DataflowResult<Node, Fact> result,
                                DataflowAnalysis<Node, Fact> analysis,
                                Node node) {
        Fact fact = result.getInFact(node);
        if (fact == null) {
            fact = analysis.newInitialFact();
            result.setInFact(node, fact);
        }
        return fact;
    }

    @Override
    protected void doSolveBackward(DataflowAnalysis<Node, Fact> analysis,
                                   DataflowResult<Node, Fact> result) {
        CFG<Node> cfg = analysis.getCFG();
        NavigableSet<Node> workList = Sets.newOrderedSet(
                Comparator.comparingInt(n -> -cfg.getIndex(n)));
        cfg.forEach(node -> {
            if (!cfg.isExit(node)) {
                workList.add(node);
            }
        });
        while (!workList.isEmpty()) {
            Node node = workList.pollFirst();
            // meet incoming facts
            Fact out;
            int outDegree = cfg.getOutDegreeOf(node);
            if (outDegree > 1) {
                out = result.getOutFact(node);
                cfg.getOutEdgesOf(node).forEach(outEdge -> {
                    Fact fact = result.getInFact(outEdge.target());
                    if (analysis.needTransferEdge(outEdge)) {
                        fact = analysis.transferEdge(outEdge, fact);
                    }
                    analysis.meetInto(fact, out);
                });
            } else if (outDegree == 1) {
                CFGEdge<Node> outEdge = CollectionUtils.getOne(cfg.getOutEdgesOf(node));
                if (analysis.needTransferEdge(outEdge)) {
                    out = analysis.transferEdge(outEdge,
                            result.getInFact(outEdge.target()));
                    result.setOutFact(node, out);
                } else {
                    out = result.getOutFact(node);
                }
            } else {
                out = result.getOutFact(node);
            }
            // apply node transfer function
            Fact in = result.getInFact(node);
            boolean changed = analysis.transferNode(node, in, out);
            if (changed) {
                workList.addAll(cfg.getPredsOf(node));
            }
        }
    }
}
