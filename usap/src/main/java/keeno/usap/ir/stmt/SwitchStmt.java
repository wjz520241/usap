

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.RValue;
import keeno.usap.ir.exp.Var;
import keeno.usap.util.collection.Pair;

import java.util.List;
import java.util.Set;

/**
 * Representation of switch statement, e.g.,
 * switch (v) {
 * case 1: ...
 * case 2: ...
 * default: ...
 * }
 */
public abstract class SwitchStmt extends JumpStmt {

    /**
     * The variable holding the condition value of the switch-statement.
     */
    protected final Var var;

    /**
     * List of jump targets of the switch-statement, one target for each case.
     */
    protected List<Stmt> targets;

    /**
     * The jump target for default case.
     */
    protected Stmt defaultTarget;

    public SwitchStmt(Var var) {
        this.var = var;
    }

    /**
     * @return the variable holding the condition value of the switch-statement.
     */
    public Var getVar() {
        return var;
    }

    /**
     * @return the i-th jump target (for i-th case) of the switch-statement.
     * The indexes start from 0. Target for default case is excluded.
     */
    public Stmt getTarget(int i) {
        return targets.get(i);
    }

    public void setTargets(List<Stmt> targets) {
        this.targets = List.copyOf(targets);
    }

    /**
     * @return all case values of the switch statement. For example,
     * for switch statement
     *
     * <pre>
     * {@code
     * switch (x) {
     *     case 1: a = 1; break;
     *     case 3: a = 3; break;
     *     default: a = 0; break;
     * }
     * }
     * </pre>
     * <p>
     * This API would return [1, 3].
     */
    public abstract List<Integer> getCaseValues();

    /**
     * @return pairs of case value and the corresponding jump target.
     * Default case is excluded. For example, for switch statement
     *
     * <pre>
     * {@code
     * switch (x) {
     *     case 1: a = 1; break;
     *     case 3: a = 3; break;
     *     default: a = 0; break;
     * }
     * }
     * </pre>
     * <p>
     * This API would return [(1, a = 1;), (3, a = 3;)].
     */
    public abstract List<Pair<Integer, Stmt>> getCaseTargets();

    /**
     * @return the jump target for default case.
     */
    public Stmt getDefaultTarget() {
        return defaultTarget;
    }

    public void setDefaultTarget(Stmt defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    @Override
    public Set<RValue> getUses() {
        return Set.of(var);
    }

    @Override
    public boolean canFallThrough() {
        return false;
    }

    @Override
    public List<Stmt> getTargets() {
        return targets;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getInsnString());
        sb.append(" (").append(var).append(") {");
        getCaseTargets().forEach(caseTarget -> {
            int caseValue = caseTarget.first();
            Stmt target = caseTarget.second();
            sb.append(caseValue).append("->").append(toString(target)).append(", ");
        });
        sb.append("default->").append(toString(defaultTarget)).append('}');
        return sb.toString();
    }

    public abstract String getInsnString();
}
