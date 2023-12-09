

package keeno.usap.ir.exp;

import keeno.usap.language.type.PrimitiveType;

/**
 * 二元表达式，同一元表达式注释
 */
public interface BinaryExp extends RValue {

    /**
     * 表示二进制操作符
     */
    interface Op {
    }

    /**
     * @return 获取操作符
     */
    Op getOperator();

    /**
     * @return 获取第一个操作数
     */
    Var getOperand1();

    /**
     * @return 获取第2个操作数
     */
    Var getOperand2();

    @Override
    PrimitiveType getType();
}
