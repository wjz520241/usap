

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;

import javax.annotation.Nullable;

/**
 *表示Definition statement语句  exp1 = exp2
 * @param <L> 左侧表达式的类型
 * @param <R> 右侧表达式的类型
 */
public abstract class DefinitionStmt<L extends LValue, R extends RValue>
        extends AbstractStmt {

    /**
     * @return 左侧表达式。如果此Stmt是一个{@link Invoke}，它没有左侧表达式，例如o.m（…），则此方法返回null；否则，它必须返回一个非null值。
     */
    @Nullable
    public abstract L getLValue();

    /**
     * @return 右侧表达式。
     */
    public abstract R getRValue();
}
