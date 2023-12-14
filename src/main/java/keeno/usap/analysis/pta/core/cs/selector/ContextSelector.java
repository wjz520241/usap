

package keeno.usap.analysis.pta.core.cs.selector;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.language.classes.JMethod;

/**
 * 上下文敏感模型选择器。 参考《软件分析》第十一、十二节
 */
public interface ContextSelector {

    /**
     * @return 不包含任何上下文元素的空上下文。
     */
    Context getEmptyContext();

    /**
     * 为静态方法选择上下文。
     *
     * @param callSite （上下文敏感）调用点.
     * @param callee   被调用者.
     * @return 被调用者的上下文
     */
    Context selectContext(CSCallSite callSite, JMethod callee);

    /**
     * 为实例方法选择上下文。
     *
     * @param callSite （上下文敏感）调用点.
     * @param recv     被调用者的（上下文相关的）接收器对象。例如：
     *                 new a...
     *                 new c...
     *                 a.b(c);
     *                 其中被调用者b的接收对象是a
     * @param callee   被调用者.
     * @return 被调用者的上下文。
     */
    Context selectContext(CSCallSite callSite, CSObj recv, JMethod callee);

    /**
     * 为新创建的抽象对象选择堆上下文。
     *
     * @param method 包含新建对象的调用点的（上下文相关）方法。
     * @param obj    新创建的对象。
     * @return 对象的堆上下文。
     */
    Context selectHeapContext(CSMethod method, Obj obj);
}
