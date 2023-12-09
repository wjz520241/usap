

package keeno.usap.analysis.bugfinder.nullpointer;

import keeno.usap.analysis.MethodAnalysis;
import keeno.usap.analysis.bugfinder.BugInstance;
import keeno.usap.analysis.bugfinder.Severity;
import keeno.usap.analysis.dataflow.fact.NodeResult;
import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.analysis.graph.cfg.CFGBuilder;
import keeno.usap.analysis.graph.cfg.CFGEdge;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.IR;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.If;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.type.NullType;
import keeno.usap.util.collection.Sets;

import java.util.Set;

public class NullPointerDetection extends MethodAnalysis<Set<BugInstance>> {

    public static String ID = "null-pointer";

    public NullPointerDetection(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Set<BugInstance> analyze(IR ir) {
        NodeResult<Stmt, IsNullFact> nullValues = ir.getResult(IsNullAnalysis.ID);
        Set<BugInstance> bugInstances = Sets.newOrderedSet();
        bugInstances.addAll(findNullDeref(ir, nullValues));
        bugInstances.addAll(findRedundantComparison(ir, nullValues));
        return bugInstances;
    }

    private Set<BugInstance> findNullDeref(IR ir, NodeResult<Stmt, IsNullFact> nullValues) {
        Set<BugInstance> nullDerefs = Sets.newHybridSet();
        CFG<Stmt> cfg = ir.getResult(CFGBuilder.ID);
        for (Stmt stmt : cfg.getNodes()) {
            Var derefVar = stmt.accept(new NPEVarVisitor());
            if (derefVar != null) {
                IsNullFact prevFact = null;
                for (CFGEdge<Stmt> inEdge : cfg.getInEdgesOf(stmt)) {
                    if (inEdge.getKind() == CFGEdge.Kind.FALL_THROUGH) {
                        prevFact = nullValues.getOutFact(inEdge.source());
                    }
                }
                if (prevFact != null && prevFact.isValid()) {
                    IsNullValue derefVarValue = prevFact.get(derefVar);
                    if (derefVarValue.isDefinitelyNull()) {
                        nullDerefs.add(new BugInstance(
                                BugType.NP_ALWAYS_NULL, Severity.BLOCKER, ir.getMethod())
                                .setSourceLine(stmt.getLineNumber()));
                    } else if (derefVarValue.isNullOnSomePath()) {
                        nullDerefs.add(new BugInstance(
                                BugType.NP_MAY_NULL, Severity.CRITICAL, ir.getMethod())
                                .setSourceLine(stmt.getLineNumber()));
                    }
                }
            }
        }
        return nullDerefs;
    }

    private Set<BugInstance> findRedundantComparison(IR ir, NodeResult<Stmt, IsNullFact> nullValues) {
        Set<BugInstance> redundantComparisons = Sets.newHybridSet();
        for (Stmt stmt : ir.getStmts()) {
            if (stmt instanceof If ifStmt) {
                IsNullFact fact = nullValues.getOutFact(stmt);
                if (!fact.isValid()) {
                    continue;
                }
                IsNullConditionDecision decision = fact.getDecision();

                if (decision != null) {
                    Var varTested = decision.getVarTested();
                    IsNullValue varTestedValue = fact.get(varTested);
                    Var var1 = ifStmt.getCondition().getOperand1();
                    Var var2 = ifStmt.getCondition().getOperand2();
                    BugType bugType = null;

                    Var anotherVar = varTested == var1 ? var2 : var1;
                    if (anotherVar.getType() instanceof NullType) {
                        if (varTestedValue.isAKaBoom()) {
                            bugType = BugType.RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE;
                        } else if (varTestedValue.isDefinitelyNotNull()) {
                            bugType = BugType.RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE;
                        } else if (varTestedValue.isDefinitelyNull()) {
                            bugType = BugType.RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE;
                        }
                    } else {
                        IsNullValue anotherVarValue = fact.get(anotherVar);
                        if (varTestedValue.isDefinitelyNull()) {
                            if (anotherVarValue.isDefinitelyNull()) {
                                bugType = BugType.RCN_REDUNDANT_COMPARISON_TWO_NULL_VALUES;
                            } else if (anotherVarValue.isDefinitelyNotNull()) {
                                bugType = BugType.RCN_REDUNDANT_COMPARISON_OF_NULL_AND_NONNULL_VALUE;
                            }
                        } else if (varTestedValue.isDefinitelyNotNull()
                                && anotherVarValue.isDefinitelyNull()) {
                            bugType = BugType.RCN_REDUNDANT_COMPARISON_OF_NULL_AND_NONNULL_VALUE;
                        }
                    }

                    if (bugType != null) {
                        redundantComparisons.add(new BugInstance(
                                bugType, Severity.MAJOR, ir.getMethod())
                                .setSourceLine(stmt.getLineNumber()));
                    }
                }
            }
        }
        return redundantComparisons;
    }

    private enum BugType implements keeno.usap.analysis.bugfinder.BugType {
        NP_ALWAYS_NULL,
        NP_MAY_NULL,
        RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE,
        RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE,
        RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE,
        RCN_REDUNDANT_COMPARISON_TWO_NULL_VALUES,
        RCN_REDUNDANT_COMPARISON_OF_NULL_AND_NONNULL_VALUE
    }
}
