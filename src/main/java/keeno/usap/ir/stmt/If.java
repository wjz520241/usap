

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.ConditionExp;
import keeno.usap.ir.exp.RValue;
import keeno.usap.util.collection.ArraySet;

import java.util.List;
import java.util.Set;

/**
 * Representation of if statement, e.g., if a == b goto S;
 */
public class If extends JumpStmt {

    /**
     * The condition expression.
     */
    private final ConditionExp condition;

    /**
     * 条件表达式求值为true时跳转目标。
     */
    private Stmt target;

    public If(ConditionExp condition) {
        this.condition = condition;
    }

    /**
     * @return the condition expression of the if-statement.
     */
    public ConditionExp getCondition() {
        return condition;
    }

    /**
     * @return the jump target (when the condition expression is evaluated
     * to true) of the if-statement.
     */
    public Stmt getTarget() {
        return target;
    }

    public void setTarget(Stmt target) {
        this.target = target;
    }

    @Override
    public Set<RValue> getUses() {
        Set<RValue> uses = new ArraySet<>(condition.getUses());
        uses.add(condition);
        return uses;
    }

    @Override
    public List<Stmt> getTargets() {
        return List.of(target);
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format(
                "if (%s) goto %s", condition, toString(target));
    }
}
