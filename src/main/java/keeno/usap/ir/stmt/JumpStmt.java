

package keeno.usap.ir.stmt;

import java.util.List;

public abstract class JumpStmt extends AbstractStmt {

    /**
     * @return 此语句的可能跳转目标。
     */
    public abstract List<Stmt> getTargets();

    /**
     * 将目标语句转换为其字符串表示形式。
     */
    public String toString(Stmt target) {
        return target == null ?
                "[unknown]" : Integer.toString(target.getIndex());
    }
}
