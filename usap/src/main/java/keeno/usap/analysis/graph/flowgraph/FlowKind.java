

package keeno.usap.analysis.graph.flowgraph;

public enum FlowKind {
    LOCAL_ASSIGN,
    CAST,

    INSTANCE_LOAD,
    INSTANCE_STORE,

    ARRAY_LOAD,
    ARRAY_STORE,

    STATIC_LOAD,
    STATIC_STORE,

    THIS_PASSING,
    PARAMETER_PASSING,
    RETURN,

    OTHER,
}
