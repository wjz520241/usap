

package keeno.usap.analysis.dataflow.analysis.availexp;

import keeno.usap.analysis.dataflow.analysis.AbstractDataflowAnalysis;
import keeno.usap.analysis.dataflow.analysis.AnalysisDriver;
import keeno.usap.analysis.dataflow.fact.SetFact;
import keeno.usap.analysis.dataflow.fact.ToppedSetFact;
import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.exp.BinaryExp;
import keeno.usap.ir.exp.CastExp;
import keeno.usap.ir.exp.Exp;
import keeno.usap.ir.exp.InstanceOfExp;
import keeno.usap.ir.exp.UnaryExp;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.DefinitionStmt;
import keeno.usap.ir.stmt.Stmt;

/**
 * Available expression analysis on local variables.
 * In Tai-e IR, Exp.equals(Object) test equality by object identity,
 * which cannot satisfy the requirement of available expression analysis,
 * thus we create ExpWrapper, which contains Exp and tests equality
 * (and computes hashcode) based on the content of the relevant Exps.
 *
 * @see ExpWrapper
 */
public class AvailableExpression extends AnalysisDriver<Stmt, SetFact<ExpWrapper>> {

    public static final String ID = "avail-exp";

    public AvailableExpression(AnalysisConfig config) {
        super(config);
    }

    @Override
    protected Analysis makeAnalysis(CFG<Stmt> cfg) {
        return new Analysis(cfg);
    }

    private static class Analysis extends AbstractDataflowAnalysis<Stmt, SetFact<ExpWrapper>> {

        private Analysis(CFG<Stmt> cfg) {
            super(cfg);
        }

        @Override
        public boolean isForward() {
            return true;
        }

        @Override
        public SetFact<ExpWrapper> newBoundaryFact() {
            return new ToppedSetFact<>(false);
        }

        @Override
        public SetFact<ExpWrapper> newInitialFact() {
            return new ToppedSetFact<>(true);
        }

        @Override
        public void meetInto(SetFact<ExpWrapper> fact, SetFact<ExpWrapper> target) {
            target.intersect(fact);
        }

        @Override
        public boolean transferNode(Stmt stmt, SetFact<ExpWrapper> in, SetFact<ExpWrapper> out) {
            if (((ToppedSetFact<ExpWrapper>) in).isTop()) {
                // valid data facts have not arrived yet, just skip and return
                // true to ensure that the successor Stmts will be analyzed later
                return true;
            }
            SetFact<ExpWrapper> oldOut = out.copy();
            out.set(in);
            if (stmt instanceof DefinitionStmt) {
                Exp lvalue = ((DefinitionStmt<?, ?>) stmt).getLValue();
                if (lvalue instanceof Var defVar) {
                    // kill affected expressions
                    out.removeIf(expWrapper ->
                            expWrapper.get().getUses().contains(defVar));
                }
                Exp rvalue = ((DefinitionStmt<?, ?>) stmt).getRValue();
                if (isRelevant(rvalue)) {
                    // generate available expressions
                    out.add(new ExpWrapper(rvalue));
                }
            }
            return !out.equals(oldOut);
        }

        /**
         * Checks if an expression is relevant to available expressions.
         * We only consider these expressions as available expressions.
         */
        private static boolean isRelevant(Exp exp) {
            return exp instanceof Var ||
                    exp instanceof BinaryExp ||
                    exp instanceof CastExp ||
                    exp instanceof InstanceOfExp ||
                    exp instanceof UnaryExp;
        }
    }
}
