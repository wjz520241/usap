

package keeno.usap.analysis.dataflow.analysis;

import keeno.usap.analysis.dataflow.fact.SetFact;
import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Copy;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.util.Indexer;
import keeno.usap.util.collection.IndexerBitSet;

/**
 * Implementation of live variable analysis.
 */
public class LiveVariable extends AnalysisDriver<Stmt, SetFact<Var>> {

    public static final String ID = "live-var";

    public LiveVariable(AnalysisConfig config) {
        super(config);
    }

    @Override
    protected Analysis makeAnalysis(CFG<Stmt> cfg) {
        return new Analysis(cfg, getOptions().getBoolean("strongly"));
    }

    private static class Analysis extends AbstractDataflowAnalysis<Stmt, SetFact<Var>> {

        /**
         * Whether enable strongly live variable analysis.
         */
        private final boolean strongly;

        /**
         * Indexer for variables in the IR.
         */
        private final Indexer<Var> varIndexer;

        private Analysis(CFG<Stmt> cfg, boolean strongly) {
            super(cfg);
            this.strongly = strongly;
            this.varIndexer = cfg.getIR().getVarIndexer();
        }

        @Override
        public boolean isForward() {
            return false;
        }

        @Override
        public SetFact<Var> newBoundaryFact() {
            return newInitialFact();
        }

        @Override
        public SetFact<Var> newInitialFact() {
            return new SetFact<>(new IndexerBitSet<>(varIndexer, false));
        }

        @Override
        public void meetInto(SetFact<Var> fact, SetFact<Var> target) {
            target.union(fact);
        }

        @Override
        public boolean transferNode(Stmt stmt, SetFact<Var> in, SetFact<Var> out) {
            SetFact<Var> oldIn = in.copy();
            in.set(out);
            // kill definition in stmt
            stmt.getDef().ifPresent(def -> {
                if (def instanceof Var) {
                    in.remove((Var) def);
                }
            });
            // generate uses in stmt
            if (strongly) {
                // only add strongly live variables
                if (stmt instanceof Copy copy) {
                    // for a Copy statement, say x = y, we consider y as
                    // strongly live only when x is also strongly live
                    Var lVar = copy.getLValue();
                    Var rVar = copy.getRValue();
                    if (out.contains(lVar)) {
                        in.add(rVar);
                    }
                } else {
                    // for non-Copy statements, all used variables
                    // are considered strongly live
                    stmt.getUses().forEach(use -> {
                        if (use instanceof Var) {
                            in.add((Var) use);
                        }
                    });
                }
            } else {
                // add all used variables
                stmt.getUses().forEach(use -> {
                    if (use instanceof Var) {
                        in.add((Var) use);
                    }
                });
            }
            return !in.equals(oldIn);
        }
    }
}
