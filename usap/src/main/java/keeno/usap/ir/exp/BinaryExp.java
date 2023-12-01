package keeno.usap.ir.exp;

import keeno.usap.language.PrimitiveType;

/**
 *同一元表达式注释
 */
public interface BinaryExp extends RValue {

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
