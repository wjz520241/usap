package keeno.usap.ir.exp;

import keeno.usap.language.Type;

import java.io.Serializable;
import java.util.Set;

/**
 * 表示一个Expressions
 */
public interface Exp extends Serializable {

    Type getType();

    /**
     * @return 此表达式使用的表达式列表
     */
    default Set<RValue> getUses(){
        return Set.of();
    };
}
