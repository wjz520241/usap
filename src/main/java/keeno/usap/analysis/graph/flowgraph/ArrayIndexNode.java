

package keeno.usap.analysis.graph.flowgraph;

import keeno.usap.analysis.pta.core.heap.Obj;

public class ArrayIndexNode extends InstanceNode {

    ArrayIndexNode(Obj base, int index) {
        super(base, index);
    }

    @Override
    public String toString() {
        return "ArrayIndexNode{" + base + "}";
    }
}
