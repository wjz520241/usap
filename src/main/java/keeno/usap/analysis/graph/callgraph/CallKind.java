

package keeno.usap.analysis.graph.callgraph;

public enum CallKind {
    // regular calls
    INTERFACE,
    VIRTUAL,
    SPECIAL,
    STATIC,
    DYNAMIC,
    /**
     * Non-regular calls, such calls are typically handled
     * by pointer analysis plugins.
     */
    OTHER,
}
