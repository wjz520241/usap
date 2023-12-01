package keeno.usap.ir.exp;

import keeno.usap.language.PrimitiveType;

import java.util.Set;

/**
 * 因为Unary(Var var, UnaryExp unaryExp)，所以UnaryExp必定在右值
 * 注意getType方法的返回值是基本类型
 * 看看Jimple生成的中间码就明白了
 */
public interface UnaryExp extends RValue {

    Var getOperand();

    @Override
    default Set<RValue> getUses() {
        return Set.of(getOperand());
    }

    @Override
    PrimitiveType getType();
}
