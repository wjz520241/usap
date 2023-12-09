

package keeno.usap.analysis.deadcode;

import keeno.usap.analysis.MethodAnalysis;
import keeno.usap.analysis.dataflow.analysis.LiveVariable;
import keeno.usap.analysis.dataflow.analysis.constprop.CPFact;
import keeno.usap.analysis.dataflow.analysis.constprop.ConstantPropagation;
import keeno.usap.analysis.dataflow.analysis.constprop.Evaluator;
import keeno.usap.analysis.dataflow.analysis.constprop.Value;
import keeno.usap.analysis.dataflow.fact.NodeResult;
import keeno.usap.analysis.dataflow.fact.SetFact;
import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.analysis.graph.cfg.CFGBuilder;
import keeno.usap.analysis.graph.cfg.CFGEdge;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.IR;
import keeno.usap.ir.exp.ArithmeticExp;
import keeno.usap.ir.exp.ArrayAccess;
import keeno.usap.ir.exp.CastExp;
import keeno.usap.ir.exp.FieldAccess;
import keeno.usap.ir.exp.NewExp;
import keeno.usap.ir.exp.RValue;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.AssignStmt;
import keeno.usap.ir.stmt.If;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.ir.stmt.SwitchStmt;
import keeno.usap.util.collection.Sets;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Queue;
import java.util.Set;

/**
 * Detects dead code in an IR.
 */
public class DeadCodeDetection extends MethodAnalysis<Set<Stmt>> {

    public static final String ID = "dead-code";

    public DeadCodeDetection(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Set<Stmt> analyze(IR ir) {
        // obtain results of pre-analyses
        CFG<Stmt> cfg = ir.getResult(CFGBuilder.ID);
        NodeResult<Stmt, CPFact> constants =
                ir.getResult(ConstantPropagation.ID);
        NodeResult<Stmt, SetFact<Var>> liveVars =
                ir.getResult(LiveVariable.ID);
        // keep statements (dead code) sorted in the resulting set
        Set<Stmt> deadCode = Sets.newOrderedSet(Comparator.comparing(Stmt::getIndex));
        // initialize graph traversal
        Set<Stmt> visited = Sets.newSet(cfg.getNumberOfNodes());
        Queue<Stmt> queue = new ArrayDeque<>();
        queue.add(cfg.getEntry());
        while (!queue.isEmpty()) {
            Stmt stmt = queue.remove();
            visited.add(stmt);
            if (isDeadAssignment(stmt, liveVars)) {
                // record dead assignment
                deadCode.add(stmt);
            }
            cfg.getOutEdgesOf(stmt)
                    .stream()
                    .filter(edge -> !isUnreachableBranch(edge, constants))
                    .map(CFGEdge::target)
                    .forEach(succ -> {
                        if (!visited.contains(succ)) {
                            queue.add(succ);
                        }
                    });
        }
        if (visited.size() < cfg.getNumberOfNodes()) {
            // this means that some nodes are not reachable during traversal
            for (Stmt s : ir) {
                if (!visited.contains(s)) {
                    deadCode.add(s);
                }
            }
        }
        return deadCode.isEmpty() ? Collections.emptySet() : deadCode;
    }

    private static boolean isDeadAssignment(
            Stmt stmt, NodeResult<Stmt, SetFact<Var>> liveVars) {
        if (stmt instanceof AssignStmt<?, ?> assign) {
            if (assign.getLValue() instanceof Var lhs) {
                return !liveVars.getOutFact(assign).contains(lhs) &&
                        hasNoSideEffect(assign.getRValue());
            }
        }
        return false;
    }

    private static boolean isUnreachableBranch(
            CFGEdge<Stmt> edge, NodeResult<Stmt, CPFact> constants) {
        Stmt src = edge.source();
        if (src instanceof If ifStmt) {
            Value cond = Evaluator.evaluate(
                    ifStmt.getCondition(), constants.getInFact(ifStmt));
            if (cond.isConstant()) {
                int v = cond.getConstant();
                return v == 1 && edge.getKind() == CFGEdge.Kind.IF_FALSE ||
                        v == 0 && edge.getKind() == CFGEdge.Kind.IF_TRUE;
            }
        } else if (src instanceof SwitchStmt switchStmt) {
            Value condV = Evaluator.evaluate(
                    switchStmt.getVar(), constants.getInFact(switchStmt));
            if (condV.isConstant()) {
                int v = condV.getConstant();
                if (edge.isSwitchCase()) {
                    return v != edge.getCaseValue();
                } else { // default case
                    // if any other case matches the case value, then
                    // default case is unreachable (dead)
                    return switchStmt.getCaseValues()
                            .stream()
                            .anyMatch(x -> x == v);
                }
            }
        }
        return false;
    }

    /**
     * @return true if given RValue has no side effect, otherwise false.
     */
    private static boolean hasNoSideEffect(RValue rvalue) {
        // new expression modifies the heap
        if (rvalue instanceof NewExp ||
                // cast may trigger ClassCastException
                rvalue instanceof CastExp ||
                // static field access may trigger class initialization
                // instance field access may trigger NPE
                rvalue instanceof FieldAccess ||
                // array access may trigger NPE
                rvalue instanceof ArrayAccess) {
            return false;
        }
        if (rvalue instanceof ArithmeticExp) {
            ArithmeticExp.Op op = ((ArithmeticExp) rvalue).getOperator();
            // may trigger DivideByZeroException
            return op != ArithmeticExp.Op.DIV && op != ArithmeticExp.Op.REM;
        }
        return true;
    }
}
