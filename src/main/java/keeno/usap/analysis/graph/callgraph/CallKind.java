

package keeno.usap.analysis.graph.callgraph;

public enum CallKind {
    // regular calls
    INTERFACE,
    VIRTUAL,
    SPECIAL,
    STATIC,
    DYNAMIC,
    /**
     * 非常规调用，此类调用通常由指针分析插件处理。
     */
    OTHER,
}
