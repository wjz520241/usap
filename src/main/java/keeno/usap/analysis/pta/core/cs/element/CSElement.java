

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.analysis.pta.core.cs.context.Context;

/**
 * 上下文敏感元素。每个元素都与一个上下文相关联。
 */
public interface CSElement {

    /**
     * @return 上下文敏感元素的上下文。
     */
    Context getContext();
}
