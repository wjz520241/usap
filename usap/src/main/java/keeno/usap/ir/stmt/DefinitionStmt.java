package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;

import javax.annotation.Nullable;

/**
 *表示Definition statement语句  exp1 = exp2
 * @param <L> 左值表达式的类型
 * @param <R> 右值表达式的类型
 */
public abstract class DefinitionStmt<L extends LValue, R extends RValue> extends AbstractStmt {
    @Nullable
    protected abstract L getLValue();

    protected abstract R getRValue();
}
