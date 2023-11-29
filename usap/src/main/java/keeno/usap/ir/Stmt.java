package keeno.usap.ir;

import keeno.usap.util.Indexable;

import java.io.Serializable;

/**
 * 参考doc目录下的《中间表示》文献
 * IR中的statement
 */
public interface Stmt extends Indexable, Serializable {

    void setIndex();

    /**
     * 该statement在原始文件中的行号，如果不可用则为-1
     */
    int getLineNumber();

    void setLineNumber();
}
