

package keeno.usap.analysis.graph.flowgraph;

import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.ir.exp.Var;
import keeno.usap.language.classes.JField;
import keeno.usap.util.Indexer;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.TwoKeyMap;
import keeno.usap.util.collection.Views;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class NodeManager implements Indexer<Node> {

    private int nodeCounter;

    private final List<Node> nodes = new ArrayList<>(4096);

    private final Map<Var, VarNode> var2Node = Maps.newMap(4096);

    private final TwoKeyMap<Obj, JField, InstanceFieldNode> iField2Node = Maps.newTwoKeyMap();

    private final Map<Obj, ArrayIndexNode> array2Node = Maps.newMap(1024);

    private final Map<JField, StaticFieldNode> sField2Node = Maps.newMap(1024);

    @Nullable
    public VarNode getVarNode(Var var) {
        return var2Node.get(var);
    }

    protected VarNode getOrCreateVarNode(Var var) {
        return var2Node.computeIfAbsent(var, v -> {
            VarNode node = new VarNode(v, nodeCounter++);
            nodes.add(node);
            return node;
        });
    }

    protected InstanceFieldNode getOrCreateInstanceFieldNode(Obj base, JField field) {
        return iField2Node.computeIfAbsent(base, field, (o, f) -> {
            InstanceFieldNode node = new InstanceFieldNode(o, f, nodeCounter++);
            nodes.add(node);
            return node;
        });
    }

    protected ArrayIndexNode getOrCreateArrayIndexNode(Obj array) {
        return array2Node.computeIfAbsent(array, a -> {
            ArrayIndexNode node = new ArrayIndexNode(a, nodeCounter++);
            nodes.add(node);
            return node;
        });
    }

    protected StaticFieldNode getOrCreateStaticFieldNode(JField field) {
        return sField2Node.computeIfAbsent(field, f -> {
            StaticFieldNode node = new StaticFieldNode(f, nodeCounter++);
            nodes.add(node);
            return node;
        });
    }

    public boolean hasNode(Node node) {
        return nodes.contains(node);
    }

    public Set<Node> getNodes() {
        return Views.toMappedSet(nodes, node -> node,
                o -> o instanceof Node node && hasNode(node));
    }

    @Override
    public int getIndex(Node node) {
        return node.getIndex();
    }

    @Override
    public Node getObject(int index) {
        return nodes.get(index);
    }
}
