

package keeno.usap.analysis.dataflow.analysis.constprop;

import keeno.usap.analysis.dataflow.analysis.AbstractDataflowAnalysis;
import keeno.usap.analysis.dataflow.analysis.AnalysisDriver;
import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.analysis.graph.cfg.CFGEdge;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.IR;
import keeno.usap.ir.exp.ConditionExp;
import keeno.usap.ir.exp.Exp;
import keeno.usap.ir.exp.Exps;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.DefinitionStmt;
import keeno.usap.ir.stmt.If;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.ir.stmt.SwitchStmt;

/**
 * Implementation of constant propagation for int values.
 */
public class ConstantPropagation extends AnalysisDriver<Stmt, CPFact> {

    public static final String ID = "const-prop";

    public ConstantPropagation(AnalysisConfig config) {
        super(config);
    }

    @Override
    protected Analysis makeAnalysis(CFG<Stmt> cfg) {
        return new Analysis(cfg, getOptions().getBoolean("edge-refine"));
    }

    public static class Analysis extends AbstractDataflowAnalysis<Stmt, CPFact> {

        /**
         * Whether enable refinement on lattice value via edge transfer.
         */
        private final boolean edgeRefine;

        public Analysis(CFG<Stmt> cfg, boolean edgeRefine) {
            super(cfg);
            this.edgeRefine = edgeRefine;
        }

        @Override
        public boolean isForward() {
            return true;
        }

        @Override
        public CPFact newBoundaryFact() {
            return newBoundaryFact(cfg.getIR());
        }

        public CPFact newBoundaryFact(IR ir) {
            // make conservative assumption about parameters: assign NAC to them
            CPFact entryFact = newInitialFact();
            ir.getParams()
                    .stream()
                    .filter(Exps::holdsInt)
                    .forEach(p -> entryFact.update(p, Value.getNAC()));
            return entryFact;
        }

        @Override
        public CPFact newInitialFact() {
            return new CPFact();
        }

        @Override
        public void meetInto(CPFact fact, CPFact target) {
            fact.forEach((var, value) ->
                    target.update(var, meetValue(value, target.get(var))));
        }

        /**
         * Meets two Values.
         * This method computes the greatest lower bound of two Values.
         */
        public Value meetValue(Value v1, Value v2) {
            if (v1.isUndef() && v2.isConstant()) {
                return v2;
            } else if (v1.isConstant() && v2.isUndef()) {
                return v1;
            } else if (v1.isNAC() || v2.isNAC()) {
                return Value.getNAC();
            } else if (v1.equals(v2)) {
                return v1;
            } else {
                return Value.getNAC();
            }
        }

        @Override
        public boolean transferNode(Stmt stmt, CPFact in, CPFact out) {
            if (stmt instanceof DefinitionStmt) {
                Exp lvalue = ((DefinitionStmt<?, ?>) stmt).getLValue();
                if (lvalue instanceof Var lhs) {
                    Exp rhs = ((DefinitionStmt<?, ?>) stmt).getRValue();
                    boolean changed = false;
                    for (Var inVar : in.keySet()) {
                        if (!inVar.equals(lhs)) {
                            changed |= out.update(inVar, in.get(inVar));
                        }
                    }
                    return Exps.holdsInt(lhs) ?
                            out.update(lhs, Evaluator.evaluate(rhs, in)) || changed :
                            changed;
                }
            }
            return out.copyFrom(in);
        }

        @Override
        public boolean needTransferEdge(CFGEdge<Stmt> edge) {
            if (edgeRefine) {
                return edge.source() instanceof If ||
                        edge.getKind() == CFGEdge.Kind.SWITCH_CASE;
            } else {
                return false;
            }
        }

        @Override
        public CPFact transferEdge(CFGEdge<Stmt> edge, CPFact nodeFact) {
            CFGEdge.Kind kind = edge.getKind();
            if (edge.source() instanceof If) {
                ConditionExp cond = ((If) edge.source()).getCondition();
                ConditionExp.Op op = cond.getOperator();
                if ((kind == CFGEdge.Kind.IF_TRUE && op == ConditionExp.Op.EQ) ||
                        (kind == CFGEdge.Kind.IF_FALSE && op == ConditionExp.Op.NE)) {
                    // if (v1 == v2) {
                    //   ... <- v1 must equal to v2 at this branch
                    // if (v1 != v2) { ... } else {
                    //   ... <- v1 must equal to v2 at this branch
                    Var v1 = cond.getOperand1();
                    Value val1 = nodeFact.get(v1);
                    Var v2 = cond.getOperand2();
                    Value val2 = nodeFact.get(v2);
                    CPFact result = nodeFact.copy();
                    Value joined = joinValue(val1, val2);
                    result.update(v1, joined);
                    result.update(v2, joined);
                    return result;
                }
            } else if (kind == CFGEdge.Kind.SWITCH_CASE) {
                // switch (x) {
                //   case 1: ... <- x must be 1 at this branch
                Var var = ((SwitchStmt) edge.source()).getVar();
                Value val = nodeFact.get(var);
                int caseValue = edge.getCaseValue();
                CPFact result = nodeFact.copy();
                result.update(var, joinValue(val, Value.makeConstant(caseValue)));
                return result;
            }
            return nodeFact;
        }
    }

    /**
     * Joins two Values.
     * This method computes the least upper bound of two Values.
     */
    private static Value joinValue(Value v1, Value v2) {
        if (v1.isNAC() && v2.isConstant()) {
            return v2;
        } else if (v1.isConstant() && v2.isNAC()) {
            return v1;
        } else if (v1.isUndef() || v2.isUndef()) {
            return Value.getUndef();
        } else if (v1.equals(v2)) {
            return v1;
        } else {
            return Value.getUndef();
        }
    }
}
