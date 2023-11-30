package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.UnaryExp;
import keeno.usap.ir.exp.Var;

/**
 * 以下类型的一元赋值语句的表示
 * 负数：x = -y
 * 数组长度：x = arr.length
 */
public class Unary extends AssignStmt<Var, UnaryExp> {
    public Unary(Var var, UnaryExp unaryExp) {
        super(var, unaryExp);
    }
}
