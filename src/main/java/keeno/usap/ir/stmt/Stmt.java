

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;
import keeno.usap.util.Indexable;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * IR中的statement.
 * 参考doc目录下的《中间表示》文档
 */
public interface Stmt extends Indexable, Serializable {

    /**
     * @return 该Stmt在容器IR中的索引。
     */
    @Override
    int getIndex();

    void setIndex(int index);

    /**
     * @return 该statement在原始文件中的行号，如果不可用则为-1
     */
    int getLineNumber();

    void setLineNumber(int lineNumber);

    /**
     * @return 在此statement中定义的左值，最多只能定义一个
     */
    Optional<LValue> getDef();

    /**
     * @return 此statement使用的右值表达式列表
     */
    Set<RValue> getUses();

    /**
     * 这是用来切割基本块的标志，看重写该方法的类名就明白了
     * @return 如果在执行此语句后可以继续执行下一条语句，则为true；否则为false。
     */
    boolean canFallThrough();

    <T> T accept(StmtVisitor<T> visitor);
}
