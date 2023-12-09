

package keeno.usap.analysis.pta;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.analysis.graph.callgraph.CallGraph;
import keeno.usap.analysis.graph.callgraph.DefaultCallGraph;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.graph.flowgraph.ObjectFlowGraph;
import keeno.usap.analysis.pta.core.cs.element.ArrayIndex;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSManager;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.cs.element.InstanceField;
import keeno.usap.analysis.pta.core.cs.element.Pointer;
import keeno.usap.analysis.pta.core.cs.element.StaticField;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.core.solver.PointerFlowGraph;
import keeno.usap.analysis.pta.core.solver.PropagateTypes;
import keeno.usap.ir.exp.ArrayAccess;
import keeno.usap.ir.exp.InstanceFieldAccess;
import keeno.usap.ir.exp.StaticFieldAccess;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ArrayType;
import keeno.usap.util.AbstractResultHolder;
import keeno.usap.util.Canonicalizer;
import keeno.usap.util.Indexer;
import keeno.usap.util.collection.HybridBitSet;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.Pair;
import keeno.usap.util.collection.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class PointerAnalysisResultImpl extends AbstractResultHolder
        implements PointerAnalysisResult {

    private static final Logger logger = LogManager.getLogger(PointerAnalysisResultImpl.class);

    private final PropagateTypes propTypes;

    private final CSManager csManager;

    /**
     * Points-to set of local variables.
     */
    private final Map<Var, Set<Obj>> varPointsTo = Maps.newConcurrentMap(4096);

    /**
     * Points-to sets of instance field expressions, e.g., v.f.
     */
    private final Map<Pair<Var, JField>, Set<Obj>> ifieldPointsTo = Maps.newConcurrentMap(1024);

    /**
     * Points-to set of static field expressions, e.g., T.f.
     */
    private final Map<JField, Set<Obj>> sfieldPointsTo = Maps.newConcurrentMap(512);

    /**
     * Points-to set of array expressions, e.g., a[i].
     */
    private final Map<Var, Set<Obj>> arrayPointsTo = Maps.newConcurrentMap(1024);

    /**
     * Set of all (reachable) objects in the program.
     */
    private final Set<Obj> objects;

    /**
     * Canonicalizes (context-insensitive) points-to set.
     */
    private final Canonicalizer<Set<Obj>> canonicalizer = new Canonicalizer<>();

    /**
     * Context-sensitive call graph.
     */
    private final CallGraph<CSCallSite, CSMethod> csCallGraph;

    /**
     * Obj indexer.
     */
    private final Indexer<Obj> objIndexer;

    /**
     * Call graph (context projected out).
     */
    private CallGraph<Invoke, JMethod> callGraph;

    private final PointerFlowGraph pfg;

    /**
     * Object flow graph (context projected out).
     */
    private ObjectFlowGraph ofg;

    public PointerAnalysisResultImpl(
            PropagateTypes propTypes, CSManager csManager,
            Indexer<Obj> objIndexer, CallGraph<CSCallSite, CSMethod> csCallGraph,
            PointerFlowGraph pfg) {
        this.propTypes = propTypes;
        this.csManager = csManager;
        this.objIndexer = objIndexer;
        this.csCallGraph = csCallGraph;
        this.pfg = pfg;
        this.objects = removeContexts(getCSObjects().stream());
    }

    @Override
    public Collection<CSVar> getCSVars() {
        return csManager.getCSVars();
    }

    @Override
    public Collection<Var> getVars() {
        return csManager.getVars();
    }

    @Override
    public Collection<InstanceField> getInstanceFields() {
        return csManager.getInstanceFields();
    }

    @Override
    public Collection<ArrayIndex> getArrayIndexes() {
        return csManager.getArrayIndexes();
    }

    @Override
    public Collection<StaticField> getStaticFields() {
        return csManager.getStaticFields();
    }

    @Override
    public Collection<CSObj> getCSObjects() {
        return csManager.getObjects();
    }

    @Override
    public Collection<Obj> getObjects() {
        return objects;
    }

    @Override
    public Indexer<Obj> getObjectIndexer() {
        return objIndexer;
    }

    @Override
    public Set<Obj> getPointsToSet(Var var) {
        if (!propTypes.isAllowed(var)) {
            return Set.of();
        }
        return varPointsTo.computeIfAbsent(var, v ->
                removeContexts(csManager.getCSVarsOf(var)
                        .stream()
                        .flatMap(Pointer::objects)));
    }

    @Override
    public Set<Obj> getPointsToSet(InstanceFieldAccess access) {
        if (!propTypes.isAllowed(access)) {
            return Set.of();
        }
        Var base = access.getBase();
        JField field = access.getFieldRef().resolveNullable();
        return field != null ? getPointsToSet(base, field) : Set.of();
    }

    @Override
    public Set<Obj> getPointsToSet(Var base, JField field) {
        if (!propTypes.isAllowed(field.getType())) {
            return Set.of();
        }
        if (field.isStatic()) {
            logger.warn("{} is not an instance field", field);
            return Set.of();
        }
        // TODO - properly handle non-exist base.field
        return ifieldPointsTo.computeIfAbsent(new Pair<>(base, field), p ->
                removeContexts(csManager.getCSVarsOf(base)
                        .stream()
                        .flatMap(Pointer::objects)
                        .map(o -> csManager.getInstanceField(o, field))
                        .flatMap(InstanceField::objects)));
    }

    @Override
    public Set<Obj> getPointsToSet(Obj base, JField field) {
        if (!propTypes.isAllowed(field.getType())) {
            return Set.of();
        }
        if (field.isStatic()) {
            logger.warn("{} is not an instance field", field);
            return Set.of();
        }
        // TODO - properly handle non-exist base.field
        return removeContexts(csManager.getCSObjsOf(base)
                .stream()
                .map(o -> csManager.getInstanceField(o, field))
                .flatMap(InstanceField::objects));
    }

    @Override
    public Set<Obj> getPointsToSet(StaticFieldAccess access) {
        if (!propTypes.isAllowed(access)) {
            return Set.of();
        }
        JField field = access.getFieldRef().resolveNullable();
        return field != null ? getPointsToSet(field) : Set.of();
    }

    @Override
    public Set<Obj> getPointsToSet(JField field) {
        if (!propTypes.isAllowed(field.getType())) {
            return Set.of();
        }
        if (!field.isStatic()) {
            logger.warn("{} is not a static field", field);
            return Set.of();
        }
        return sfieldPointsTo.computeIfAbsent(field, f ->
                removeContexts(csManager.getStaticField(field).objects()));
    }

    @Override
    public Set<Obj> getPointsToSet(ArrayAccess access) {
        if (!propTypes.isAllowed(access)) {
            return Set.of();
        }
        return getPointsToSet(access.getBase(), access.getIndex());
    }

    @Override
    public Set<Obj> getPointsToSet(Var base, Var index) {
        if (base.getType() instanceof ArrayType baseType) {
            if (!propTypes.isAllowed(baseType.elementType())) {
                return Set.of();
            }
        } else {
            logger.warn("{} is not an array", base);
            return Set.of();
        }
        return arrayPointsTo.computeIfAbsent(base, b ->
                removeContexts(csManager.getCSVarsOf(b)
                        .stream()
                        .flatMap(Pointer::objects)
                        .map(csManager::getArrayIndex)
                        .flatMap(ArrayIndex::objects)));
    }

    @Override
    public Set<Obj> getPointsToSet(Obj array) {
        if (array.getType() instanceof ArrayType baseType) {
            if (!propTypes.isAllowed(baseType.elementType())) {
                return Set.of();
            }
        } else {
            logger.warn("{} is not an array", array);
            return Set.of();
        }
        return removeContexts(csManager.getCSObjsOf(array)
                .stream()
                .map(csManager::getArrayIndex)
                .flatMap(ArrayIndex::objects));
    }

    @Override
    public boolean mayAlias(Var v1, Var v2) {
        Set<Obj> s1 = getPointsToSet(v1);
        Set<Obj> s2 = getPointsToSet(v2);
        return Sets.haveOverlap(s1, s2);
    }

    @Override
    public boolean mayAlias(InstanceFieldAccess if1, InstanceFieldAccess if2) {
        return Objects.equals(
                if1.getFieldRef().resolveNullable(),
                if2.getFieldRef().resolveNullable())
                && mayAlias(if1.getBase(), if2.getBase());
    }

    @Override
    public boolean mayAlias(ArrayAccess a1, ArrayAccess a2) {
        return mayAlias(a1.getBase(), a2.getBase());
    }

    /**
     * Removes contexts of a context-sensitive points-to set and
     * returns a new resulting set.
     */
    private Set<Obj> removeContexts(Stream<CSObj> objects) {
        Set<Obj> set = new HybridBitSet<>(objIndexer, true);
        objects.map(CSObj::getObject).forEach(set::add);
        return canonicalizer.get(Collections.unmodifiableSet(set));
    }

    @Override
    public CallGraph<CSCallSite, CSMethod> getCSCallGraph() {
        return csCallGraph;
    }

    @Override
    public CallGraph<Invoke, JMethod> getCallGraph() {
        if (callGraph == null) {
            callGraph = removeContexts(csCallGraph);
        }
        return callGraph;
    }

    /**
     * Removes contexts in a context-sensitive call graph and
     * returns a new resulting call graph.
     */
    private static CallGraph<Invoke, JMethod> removeContexts(
            CallGraph<CSCallSite, CSMethod> csCallGraph) {
        DefaultCallGraph callGraph = new DefaultCallGraph();
        csCallGraph.entryMethods()
                .map(CSMethod::getMethod)
                .forEach(callGraph::addEntryMethod);
        csCallGraph.reachableMethods()
                .map(CSMethod::getMethod)
                .forEach(callGraph::addReachableMethod);
        csCallGraph.edges().forEach(edge -> {
            Invoke callSite = edge.getCallSite().getCallSite();
            JMethod callee = edge.getCallee().getMethod();
            callGraph.addEdge(new Edge<>(edge.getKind(),
                    callSite, callee));
        });
        return callGraph;
    }

    public ObjectFlowGraph getObjectFlowGraph() {
        if (ofg == null) {
            ofg = new ObjectFlowGraph(pfg, getCallGraph());
        }
        return ofg;
    }
}
