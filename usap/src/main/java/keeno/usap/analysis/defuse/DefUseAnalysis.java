

package keeno.usap.analysis.defuse;

import keeno.usap.analysis.MethodAnalysis;
import keeno.usap.analysis.dataflow.analysis.ReachingDefinition;
import keeno.usap.analysis.dataflow.fact.DataflowResult;
import keeno.usap.analysis.dataflow.fact.SetFact;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.IR;
import keeno.usap.ir.exp.RValue;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.util.collection.IndexMap;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.Sets;
import keeno.usap.util.collection.TwoKeyMultiMap;

/**
 * Computes intra-procedural def-use and use-def chains
 * based on reaching definition analysis.
 */
public class DefUseAnalysis extends MethodAnalysis<DefUse> {

    public static final String ID = "def-use";

    /**
     * Whether compute definitions, i.e., use-def chains.
     */
    private final boolean computeDefs;

    /**
     * Whether compute uses, i.e., def-use chains.
     */
    private final boolean computeUses;

    public DefUseAnalysis(AnalysisConfig config) {
        super(config);
        computeDefs = getOptions().getBoolean("compute-defs");
        computeUses = getOptions().getBoolean("compute-uses");
    }

    @Override
    public DefUse analyze(IR ir) {
        DataflowResult<Stmt, SetFact<Stmt>> rdResult = ir.getResult(ReachingDefinition.ID);
        TwoKeyMultiMap<Stmt, Var, Stmt> defs = computeDefs ?
                Maps.newTwoKeyMultiMap(new IndexMap<>(ir, ir.getStmts().size()),
                        () -> Maps.newMultiMap(Maps.newHybridMap()))
                : null;
        MultiMap<Stmt, Stmt> uses = computeUses ?
                Maps.newMultiMap(new IndexMap<>(ir, ir.getStmts().size()),
                        Sets::newHybridSet)
                : null;
        for (Stmt stmt : ir) {
            SetFact<Stmt> reachDefs = rdResult.getInFact(stmt);
            for (RValue use : stmt.getUses()) {
                if (use instanceof Var useVar) {
                    for (Stmt reachDef : reachDefs) {
                        reachDef.getDef().ifPresent(lhs -> {
                            if (lhs.equals(use)) {
                                if (computeDefs) {
                                    defs.put(stmt, useVar, reachDef);
                                }
                                if (computeUses) {
                                    uses.put(reachDef, stmt);
                                }
                            }
                        });
                    }
                }
            }
        }
        return new DefUse(defs, uses);
    }
}
