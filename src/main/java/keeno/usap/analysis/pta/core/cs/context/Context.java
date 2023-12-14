

package keeno.usap.analysis.pta.core.cs.context;

/**
 * 上下文相关指针分析中上下文的表示。每个上下文都可以看作是零个或多个上下文元素的列表。
 */
public interface Context {

    /**
     * @return 该上下文的长度（即元素的数量）。
     */
    int getLength();

    /**
     * @return 该上下文的第i个元素。从0开始。
     */
    Object getElementAt(int i);
}
