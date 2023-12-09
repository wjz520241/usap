

package keeno.usap.analysis.graph.flowgraph;

import keeno.usap.analysis.pta.core.heap.Obj;

public abstract class InstanceNode extends Node {

    final Obj base;

    InstanceNode(Obj base, int index) {
        super(index);
        this.base = base;
    }

    public Obj getBase() {
        return base;
    }
}
