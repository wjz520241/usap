package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;
import keeno.usap.util.Indexable;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * 参考doc目录下的《中间表示》文献
 * IR中的statement
 */
public interface Stmt extends Indexable, Serializable {

    void setIndex(int index);

    /**
     * 该statement在原始文件中的行号，如果不可用则为-1
     */
    int getLineNumber();

    void setLineNumber(int lineNumber);

    /**
     * 在此statement中定义的左值，最多只能定义一个
     */
    Optional<LValue> getDef();

    /**
     * 此statement使用的右值表达式列表
     */
    Set<RValue> getUse();
}
