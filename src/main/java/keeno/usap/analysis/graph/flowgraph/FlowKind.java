

package keeno.usap.analysis.graph.flowgraph;

/**
 * 指针流图边的类型。
 * 这里面的类型实际上是来自四条核心语句和上下文敏感算法
 * 参考《软件分析》十一、十二节
 */
public enum FlowKind {
    LOCAL_ASSIGN,
    CAST,

    INSTANCE_LOAD,
    INSTANCE_STORE,

    ARRAY_LOAD,
    ARRAY_STORE,

    STATIC_LOAD,
    STATIC_STORE,
    //传递this
    THIS_PASSING,
    PARAMETER_PASSING,
    RETURN,

    OTHER,
}
